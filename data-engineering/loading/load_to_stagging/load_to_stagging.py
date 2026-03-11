from collections.abc import Mapping

import pandas as pd

from loading.load_to_stagging.replica_config import (
    build_replica_load_settings,
    get_replica_sync_settings,
)
from loading.load_to_stagging.replica_ops import (
    apply_replica_ddl,
    ensure_target_tables_exist,
    load_dataset,
    truncate_target_tables,
)
from loading.load_to_stagging.source_shape import (
    build_comments_frame,
    build_posts_frame,
    build_users_frame,
    validate_source_datasets,
)
from loading.load_to_warehouse.warehouse_ops import ensure_schema
from utils.database import execute_with_db_retry, get_connection
from utils.logging import get_logger


logger = get_logger("load_to_stagging")


def load_to_stagging(datasets: Mapping[str, pd.DataFrame], config: dict) -> dict[str, int]:
    """Load raw datasets into the staging schema for replica sync."""
    sync_settings = get_replica_sync_settings(config)
    enabled = sync_settings.get("enabled", False)
    if not isinstance(enabled, bool):
        raise ValueError("Config value 'replica_sync.enabled' must be a boolean")
    if not enabled:
        logger.info("Replica sync is disabled in config")
        return {}

    validate_source_datasets(datasets)
    settings = build_replica_load_settings(config)

    def _load_replica() -> tuple[dict[str, int], list[str]]:
        load_summary: dict[str, int] = {}
        with get_connection(config, db_role=settings.db_role) as connection:
            applied_migrations = apply_replica_ddl(connection, settings=settings)

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
                        "Truncated replica tables before sync: %s",
                        ", ".join(settings.target_tables.values()),
                    )

                # Load raw tables in FK order so parent rows exist before dependent rows.
                users_df = build_users_frame(
                    datasets["users"],
                    settings.required_columns["users"],
                )
                load_summary["users"] = load_dataset(
                    cursor,
                    settings=settings,
                    table_key="users",
                    dataset_df=users_df,
                )

                posts_df = build_posts_frame(
                    datasets["posts"],
                    settings.required_columns["posts"],
                )
                load_summary["posts"] = load_dataset(
                    cursor,
                    settings=settings,
                    table_key="posts",
                    dataset_df=posts_df,
                )

                comments_df = build_comments_frame(
                    datasets["comments"],
                    settings.required_columns["comments"],
                )
                load_summary["comments"] = load_dataset(
                    cursor,
                    settings=settings,
                    table_key="comments",
                    dataset_df=comments_df,
                )

        return load_summary, applied_migrations

    load_summary, applied_migrations = execute_with_db_retry(
        _load_replica,
        config=config,
        operation_name="replica sync load",
    )
    logger.info(
        "Replica sync completed successfully: %s%s",
        load_summary,
        f" | applied migrations: {', '.join(applied_migrations)}" if applied_migrations else "",
    )
    return load_summary
