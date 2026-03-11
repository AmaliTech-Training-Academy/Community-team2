from typing import Mapping

import pandas as pd

from loading.load_to_warehouse.star_schema import (
    build_dim_posts_frame,
    build_dim_users_frame,
    build_fact_comments_frame,
    validate_source_datasets,
)
from loading.load_to_warehouse.warehouse_config import (
    build_warehouse_load_settings,
    get_warehouse_settings,
)
from loading.load_to_warehouse.warehouse_ops import (
    apply_warehouse_ddl,
    ensure_schema,
    ensure_target_tables_exist,
    fetch_key_map,
    load_dataset,
    truncate_target_tables,
)
from utils.database import execute_with_db_retry, get_connection
from utils.logging import get_logger


logger = get_logger("load_to_warehouse")


def load_to_warehouse(datasets: Mapping[str, pd.DataFrame], config: dict) -> dict[str, int]:
    """
    Load cleaned datasets into the analytics Postgres warehouse.
    """

    warehouse_settings = get_warehouse_settings(config)
    enabled = warehouse_settings.get("enabled", False)
    if not isinstance(enabled, bool):
        raise ValueError("Config value 'warehouse.enabled' must be a boolean")
    if not enabled:
        logger.info("Warehouse loading is disabled in config")
        return {}

    validate_source_datasets(datasets)
    settings = build_warehouse_load_settings(config)

    def _load_warehouse() -> tuple[dict[str, int], list[str]]:
        load_summary: dict[str, int] = {}
        with get_connection(config, db_role=settings.db_role) as connection:
            applied_migrations = apply_warehouse_ddl(connection, settings=settings)

            with connection.cursor() as cursor:
                ensure_schema(
                    cursor,
                    settings.schema_name,
                    create_if_missing=settings.create_schema_if_missing,
                )
                ensure_target_tables_exist(cursor, settings=settings)

                if settings.load_mode == "truncate":
                    truncate_target_tables(cursor, settings=settings)
                    logger.info(
                        "Truncated warehouse tables before load: %s",
                        ", ".join(settings.target_tables.values()),
                    )

                # Load dimensions first so comment facts can resolve warehouse surrogate keys.
                dim_users_df = build_dim_users_frame(datasets["users"])
                load_summary["dim_users"] = load_dataset(
                    cursor,
                    settings=settings,
                    table_key="dim_users",
                    dataset_df=dim_users_df,
                )

                user_key_map = fetch_key_map(
                    cursor,
                    settings=settings,
                    table_key="dim_users",
                    source_key_column="source_user_id",
                    surrogate_key_column="user_key",
                    source_ids=dim_users_df["source_user_id"].tolist(),
                )

                dim_posts_df = build_dim_posts_frame(datasets["posts"], user_key_map)
                load_summary["dim_posts"] = load_dataset(
                    cursor,
                    settings=settings,
                    table_key="dim_posts",
                    dataset_df=dim_posts_df,
                )

                post_key_map = fetch_key_map(
                    cursor,
                    settings=settings,
                    table_key="dim_posts",
                    source_key_column="source_post_id",
                    surrogate_key_column="post_key",
                    source_ids=dim_posts_df["source_post_id"].tolist(),
                )

                fact_comments_df = build_fact_comments_frame(
                    datasets["comments"],
                    user_key_map,
                    post_key_map,
                )
                load_summary["fact_comments"] = load_dataset(
                    cursor,
                    settings=settings,
                    table_key="fact_comments",
                    dataset_df=fact_comments_df,
                )

        return load_summary, applied_migrations

    load_summary, applied_migrations = execute_with_db_retry(
        _load_warehouse,
        config=config,
        operation_name="warehouse load",
    )
    logger.info(
        "Warehouse loading completed successfully: %s%s",
        load_summary,
        f" | applied migrations: {', '.join(applied_migrations)}" if applied_migrations else "",
    )
    return load_summary
