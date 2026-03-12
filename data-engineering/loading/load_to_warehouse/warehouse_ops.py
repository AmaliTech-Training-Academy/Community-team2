import hashlib
import re
from pathlib import Path

import numpy as np
import pandas as pd
from psycopg2 import sql
from psycopg2.extras import execute_values

from loading.load_to_warehouse.warehouse_config import (
    WarehouseLoadSettings,
    quote_identifier,
    qualified_table_name,
)
from utils.logging import get_logger


logger = get_logger("warehouse_ops")
MIGRATION_TOKEN_PATTERN = re.compile(r"\{\{\s*([A-Za-z_][A-Za-z0-9_]*)\s*\}\}")


def list_migration_files(directory: Path) -> list[Path]:
    migration_files = sorted(path for path in directory.glob("*.sql") if path.is_file())
    if not migration_files:
        raise FileNotFoundError(f"No SQL migration files found in {directory}")
    return migration_files


def render_migration_sql(
    migration_sql: str,
    *,
    settings: WarehouseLoadSettings,
) -> str:
    """Render migration SQL by replacing tokens with actual values from settings."""
    migration_sql = migration_sql.lstrip("﻿")
    tokens = {
        "warehouse_schema": quote_identifier(settings.schema_name),
        "warehouse_dim_users_table": quote_identifier(settings.target_tables["dim_users"]),
        "warehouse_dim_posts_table": quote_identifier(settings.target_tables["dim_posts"]),
        "warehouse_fact_comments_table": quote_identifier(settings.target_tables["fact_comments"]),
        "warehouse_users_table": quote_identifier(settings.target_tables["dim_users"]),
        "warehouse_comments_table": quote_identifier(settings.target_tables["fact_comments"]),
        "warehouse_kpi_top_contributors_view": quote_identifier(
            settings.kpis.materialized_views["top_contributors"]
        ),
        "warehouse_kpi_activity_trends_view": quote_identifier(
            settings.kpis.materialized_views["activity_trends"]
        ),
        "warehouse_kpi_posts_per_category_view": quote_identifier(
            settings.kpis.materialized_views["posts_per_category"]
        ),
    }

    def replace(match: re.Match[str]) -> str:
        token = match.group(1)
        if token not in tokens:
            raise ValueError(f"Unknown warehouse DDL token: {token}")
        return tokens[token]

    return MIGRATION_TOKEN_PATTERN.sub(replace, migration_sql)


def ensure_schema(cursor, schema_name: str, *, create_if_missing: bool) -> None:
    if not create_if_missing:
        return

    cursor.execute(
        sql.SQL("CREATE SCHEMA IF NOT EXISTS {}")
        .format(sql.Identifier(schema_name))
    )


def ensure_migration_table(cursor, settings: WarehouseLoadSettings) -> None:
    cursor.execute(
        sql.SQL(
            """
            CREATE TABLE IF NOT EXISTS {}.{} (
                filename TEXT PRIMARY KEY,
                checksum TEXT NOT NULL,
                applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
            )
            """
        ).format(
            sql.Identifier(settings.schema_name),
            sql.Identifier(settings.ddl.migration_table),
        )
    )


def fetch_applied_migrations(cursor, settings: WarehouseLoadSettings) -> dict[str, str]:
    cursor.execute(
        sql.SQL("SELECT filename, checksum FROM {}.{} ORDER BY filename")
        .format(
            sql.Identifier(settings.schema_name),
            sql.Identifier(settings.ddl.migration_table),
        )
    )
    return dict(cursor.fetchall())


def apply_warehouse_ddl(connection, *, settings: WarehouseLoadSettings) -> list[str]:
    if not settings.ddl.enabled:
        logger.info("Warehouse DDL migrations are disabled in config")
        return []

    migration_files = list_migration_files(settings.ddl.migration_directory)
    applied_migrations: list[str] = []

    with connection.cursor() as cursor:
        ensure_schema(cursor, settings.schema_name, create_if_missing=True)
        ensure_migration_table(cursor, settings)
        applied_checksums = fetch_applied_migrations(cursor, settings)

        for migration_file in migration_files:
            migration_sql = migration_file.read_text(encoding="utf-8")
            checksum = hashlib.sha256(migration_sql.encode("utf-8")).hexdigest()
            applied_checksum = applied_checksums.get(migration_file.name)

            if applied_checksum is not None:
                if applied_checksum != checksum:
                    raise RuntimeError(
                        "Warehouse migration checksum mismatch for "
                        f"{migration_file.name}. Create a new migration instead of editing an applied one."
                    )
                continue

            rendered_sql = render_migration_sql(migration_sql, settings=settings)
            if rendered_sql.strip():
                logger.info("Applying warehouse migration %s", migration_file.name)
                cursor.execute(rendered_sql)

            cursor.execute(
                sql.SQL("INSERT INTO {}.{} (filename, checksum) VALUES (%s, %s)")
                .format(
                    sql.Identifier(settings.schema_name),
                    sql.Identifier(settings.ddl.migration_table),
                ),
                (migration_file.name, checksum),
            )
            applied_migrations.append(migration_file.name)

    if applied_migrations:
        logger.info("Applied warehouse migrations: %s", ", ".join(applied_migrations))
    else:
        logger.info("No pending warehouse migrations found")

    return applied_migrations


def relation_exists(cursor, schema_name: str, relation_name: str) -> bool:
    cursor.execute("SELECT to_regclass(%s)", (f"{schema_name}.{relation_name}",))
    return cursor.fetchone()[0] is not None


def ensure_target_tables_exist(cursor, *, settings: WarehouseLoadSettings) -> None:
    missing_tables = [
        table_name
        for table_name in settings.target_tables.values()
        if not relation_exists(cursor, settings.schema_name, table_name)
    ]
    if missing_tables:
        raise RuntimeError(
            "Warehouse target tables are missing after DDL application: "
            + ", ".join(missing_tables)
        )


def ensure_kpi_materialized_views_exist(cursor, *, settings: WarehouseLoadSettings) -> None:
    if not settings.kpis.enabled:
        return

    missing_views = [
        view_name
        for view_name in settings.kpis.materialized_views.values()
        if not relation_exists(cursor, settings.schema_name, view_name)
    ]
    if missing_views:
        raise RuntimeError(
            "Warehouse KPI materialized views are missing after DDL application: "
            + ", ".join(missing_views)
        )


def refresh_kpi_materialized_views(cursor, *, settings: WarehouseLoadSettings) -> list[str]:
    if not settings.kpis.enabled:
        logger.info("Warehouse KPI materialized views are disabled in config")
        return []
    if not settings.kpis.refresh_after_load:
        logger.info("Warehouse KPI materialized view refresh is disabled in config")
        return []

    ensure_kpi_materialized_views_exist(cursor, settings=settings)

    refreshed_views: list[str] = []
    for view_key, view_name in settings.kpis.materialized_views.items():
        logger.info(
            "Refreshing KPI materialized view %s",
            qualified_table_name(settings.schema_name, view_name),
        )
        cursor.execute(
            sql.SQL("REFRESH MATERIALIZED VIEW {}.{}")
            .format(sql.Identifier(settings.schema_name), sql.Identifier(view_name))
        )
        refreshed_views.append(view_key)

    return refreshed_views


def truncate_target_tables(cursor, *, settings: WarehouseLoadSettings) -> None:
    qualified_tables = sql.SQL(", ").join(
        sql.SQL("{}.{}").format(
            sql.Identifier(settings.schema_name),
            sql.Identifier(table_name),
        )
        for table_name in settings.target_tables.values()
    )
    cascade_sql = sql.SQL(" CASCADE") if settings.truncate_cascade else sql.SQL("")
    cursor.execute(sql.SQL("TRUNCATE TABLE {}{}").format(qualified_tables, cascade_sql))


def coerce_value(value):
    if pd.isna(value):
        return None
    if isinstance(value, pd.Timestamp):
        return value.to_pydatetime()
    if isinstance(value, np.generic):
        return value.item()
    return value


def iter_chunk_records(dataframe: pd.DataFrame, chunk_size: int):
    for start in range(0, len(dataframe), chunk_size):
        chunk = dataframe.iloc[start:start + chunk_size]
        yield [
            tuple(coerce_value(value) for value in row)
            for row in chunk.itertuples(index=False, name=None)
        ]


def load_dataset(
    cursor,
    *,
    settings: WarehouseLoadSettings,
    table_key: str,
    dataset_df: pd.DataFrame,
) -> int:
    if dataset_df.empty:
        logger.info(
            "Dataset '%s' is empty; nothing to load into %s",
            table_key,
            qualified_table_name(settings.schema_name, settings.target_tables[table_key]),
        )
        return 0

    if settings.load_mode == "incremental":
        return load_dataset_incremental(
            cursor,
            settings=settings,
            table_key=table_key,
            dataset_df=dataset_df,
        )

    return load_dataset_insert_only(
        cursor,
        settings=settings,
        table_key=table_key,
        dataset_df=dataset_df,
    )


def load_dataset_insert_only(
    cursor,
    *,
    settings: WarehouseLoadSettings,
    table_key: str,
    dataset_df: pd.DataFrame,
) -> int:
    table_name = settings.target_tables[table_key]
    column_names = list(dataset_df.columns)
    insert_statement = sql.SQL("INSERT INTO {}.{} ({}) VALUES %s").format(
        sql.Identifier(settings.schema_name),
        sql.Identifier(table_name),
        sql.SQL(", ").join(sql.Identifier(column_name) for column_name in column_names),
    )
    insert_query = insert_statement.as_string(cursor)

    inserted_rows = 0
    for records in iter_chunk_records(dataset_df, settings.chunk_size):
        if not records:
            continue
        execute_values(cursor, insert_query, records, page_size=settings.chunk_size)
        inserted_rows += len(records)

    logger.info(
        "Loaded %s rows into warehouse table %s",
        inserted_rows,
        qualified_table_name(settings.schema_name, table_name),
    )
    return inserted_rows


def load_dataset_incremental(
    cursor,
    *,
    settings: WarehouseLoadSettings,
    table_key: str,
    dataset_df: pd.DataFrame,
) -> int:
    table_name = settings.target_tables[table_key]
    conflict_columns = settings.incremental_keys[table_key]
    column_names = list(dataset_df.columns)
    update_columns = [
        column_name
        for column_name in column_names
        if column_name not in conflict_columns
    ]

    if not update_columns:
        raise ValueError(
            f"Dataset '{table_key}' does not contain update columns beyond its incremental keys"
        )

    update_assignments = [
        sql.SQL("{} = EXCLUDED.{}").format(
            sql.Identifier(column_name),
            sql.Identifier(column_name),
        )
        for column_name in update_columns
    ]
    update_assignments.append(
        sql.SQL("{} = NOW()").format(sql.Identifier("loaded_at"))
    )

    insert_statement = sql.SQL(
        "INSERT INTO {}.{} ({}) VALUES %s ON CONFLICT ({}) DO UPDATE SET {}"
    ).format(
        sql.Identifier(settings.schema_name),
        sql.Identifier(table_name),
        sql.SQL(", ").join(sql.Identifier(column_name) for column_name in column_names),
        sql.SQL(", ").join(sql.Identifier(column_name) for column_name in conflict_columns),
        sql.SQL(", ").join(update_assignments),
    )
    insert_query = insert_statement.as_string(cursor)

    processed_rows = 0
    for records in iter_chunk_records(dataset_df, settings.chunk_size):
        if not records:
            continue
        execute_values(cursor, insert_query, records, page_size=settings.chunk_size)
        processed_rows += len(records)

    logger.info(
        "Incrementally loaded %s rows into warehouse table %s using conflict keys (%s)",
        processed_rows,
        qualified_table_name(settings.schema_name, table_name),
        ", ".join(conflict_columns),
    )
    return processed_rows


def fetch_key_map(
    cursor,
    *,
    settings: WarehouseLoadSettings,
    table_key: str,
    source_key_column: str,
    surrogate_key_column: str,
    source_ids,
) -> dict[int, int]:
    normalized_ids = []
    seen_ids = set()
    for source_id in source_ids:
        value = coerce_value(source_id)
        if value is None or value in seen_ids:
            continue
        seen_ids.add(value)
        normalized_ids.append(value)

    if not normalized_ids:
        return {}

    cursor.execute(
        sql.SQL("SELECT {}, {} FROM {}.{} WHERE {} = ANY(%s)").format(
            sql.Identifier(source_key_column),
            sql.Identifier(surrogate_key_column),
            sql.Identifier(settings.schema_name),
            sql.Identifier(settings.target_tables[table_key]),
            sql.Identifier(source_key_column),
        ),
        (normalized_ids,),
    )

    return {source_id: surrogate_id for source_id, surrogate_id in cursor.fetchall()}
