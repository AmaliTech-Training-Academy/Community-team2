import hashlib
import re

import pandas as pd
from psycopg2 import sql
from psycopg2.extras import execute_values

from loading.load_to_stagging.replica_config import ReplicaLoadSettings
from loading.load_to_warehouse.warehouse_config import qualified_table_name
from loading.load_to_warehouse.warehouse_ops import (
    coerce_value,
    ensure_migration_table,
    ensure_schema,
    fetch_applied_migrations,
    iter_chunk_records,
    list_migration_files,
)
from utils.logging import get_logger


logger = get_logger("replica_ops")
MIGRATION_TOKEN_PATTERN = re.compile(r"\{\{\s*([A-Za-z_][A-Za-z0-9_]*)\s*\}\}")


def render_replica_sql(sql_text: str, *, settings: ReplicaLoadSettings) -> str:
    sql_text = sql_text.lstrip("\ufeff")
    tokens = {
        "replica_schema": f'"{settings.schema_name}"',
        "replica_users_table": f'"{settings.target_tables["users"]}"',
        "replica_posts_table": f'"{settings.target_tables["posts"]}"',
        "replica_comments_table": f'"{settings.target_tables["comments"]}"',
    }

    def replace(match: re.Match[str]) -> str:
        token = match.group(1)
        if token not in tokens:
            raise ValueError(f"Unknown replica SQL token: {token}")
        return tokens[token]

    return MIGRATION_TOKEN_PATTERN.sub(replace, sql_text)


def apply_replica_ddl(connection, *, settings: ReplicaLoadSettings) -> list[str]:
    if not settings.ddl.enabled:
        logger.info("Replica DDL migrations are disabled in config")
        return []

    migration_files = list_migration_files(settings.ddl.migration_directory)
    applied_migrations: list[str] = []

    with connection.cursor() as cursor:
        ensure_schema(
            cursor,
            settings.schema_name,
            create_if_missing=settings.create_schema_if_missing,
        )
        ensure_migration_table(cursor, settings)
        applied_checksums = fetch_applied_migrations(cursor, settings)

        for migration_file in migration_files:
            sql_text = migration_file.read_text(encoding="utf-8")
            checksum = hashlib.sha256(sql_text.encode("utf-8")).hexdigest()
            applied_checksum = applied_checksums.get(migration_file.name)

            if applied_checksum is not None:
                if applied_checksum != checksum:
                    raise RuntimeError(
                        "Replica migration checksum mismatch for "
                        f"{migration_file.name}. Create a new migration instead of editing an applied one."
                    )
                continue

            rendered_sql = render_replica_sql(sql_text, settings=settings)
            if rendered_sql.strip():
                logger.info("Applying replica migration %s", migration_file.name)
                cursor.execute(rendered_sql)

            cursor.execute(
                sql.SQL("INSERT INTO {}.{} (filename, checksum) VALUES (%s, %s)").format(
                    sql.Identifier(settings.schema_name),
                    sql.Identifier(settings.ddl.migration_table),
                ),
                (migration_file.name, checksum),
            )
            applied_migrations.append(migration_file.name)

    if applied_migrations:
        logger.info("Applied replica migrations: %s", ", ".join(applied_migrations))
    else:
        logger.info("No pending replica migrations found")

    return applied_migrations


def table_exists(cursor, schema_name: str, table_name: str) -> bool:
    cursor.execute("SELECT to_regclass(%s)", (f"{schema_name}.{table_name}",))
    return cursor.fetchone()[0] is not None


def ensure_target_tables_exist(cursor, *, settings: ReplicaLoadSettings) -> None:
    missing_tables = [
        table_name
        for table_name in settings.target_tables.values()
        if not table_exists(cursor, settings.schema_name, table_name)
    ]
    if missing_tables:
        raise RuntimeError(
            "Replica target tables are missing after DDL application: "
            + ", ".join(missing_tables)
        )


def truncate_target_tables(cursor, *, settings: ReplicaLoadSettings) -> None:
    qualified_tables = sql.SQL(", ").join(
        sql.SQL("{}.{}").format(
            sql.Identifier(settings.schema_name),
            sql.Identifier(table_name),
        )
        for table_name in settings.target_tables.values()
    )
    cascade_sql = sql.SQL(" CASCADE") if settings.truncate_cascade else sql.SQL("")
    cursor.execute(sql.SQL("TRUNCATE TABLE {}{}").format(qualified_tables, cascade_sql))


def load_dataset(
    cursor,
    *,
    settings: ReplicaLoadSettings,
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
    settings: ReplicaLoadSettings,
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
        "Loaded %s rows into replica table %s",
        inserted_rows,
        qualified_table_name(settings.schema_name, table_name),
    )
    return inserted_rows


def load_dataset_incremental(
    cursor,
    *,
    settings: ReplicaLoadSettings,
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
        "Incrementally loaded %s rows into replica table %s using conflict keys (%s)",
        processed_rows,
        qualified_table_name(settings.schema_name, table_name),
        ", ".join(conflict_columns),
    )
    return processed_rows
