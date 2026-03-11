from dataclasses import dataclass

from loading.load_to_warehouse.warehouse_config import (
    WarehouseDdlSettings,
    resolve_ddl_directory,
    validate_identifier,
)


VALID_LOAD_MODES = {"append", "truncate", "incremental"}
REPLICA_TABLE_KEYS = ("users", "posts", "comments")


@dataclass(frozen=True)
class ReplicaLoadSettings:
    enabled: bool
    db_role: str
    schema_name: str
    load_mode: str
    truncate_cascade: bool
    chunk_size: int
    create_schema_if_missing: bool
    target_tables: dict[str, str]
    required_columns: dict[str, tuple[str, ...]]
    incremental_keys: dict[str, tuple[str, ...]]
    ddl: WarehouseDdlSettings


def get_stagging_source_settings(config: dict) -> dict:
    source_settings = config.get("stagging_source")
    if not isinstance(source_settings, dict):
        raise KeyError("Config is missing the 'stagging_source' settings block")
    return source_settings


def get_replica_sync_settings(config: dict) -> dict:
    sync_settings = config.get("replica_sync")
    if not isinstance(sync_settings, dict):
        raise KeyError("Config is missing the 'replica_sync' settings block")
    return sync_settings


def resolve_target_tables(source_settings: dict) -> dict[str, str]:
    configured_tables = source_settings.get("tables")
    if not isinstance(configured_tables, dict) or not configured_tables:
        raise KeyError("Config is missing 'stagging_source.tables'")

    resolved_tables: dict[str, str] = {}
    for table_key in REPLICA_TABLE_KEYS:
        table_name = configured_tables.get(table_key)
        resolved_tables[table_key] = validate_identifier(
            table_name,
            f"stagging_source.tables.{table_key}",
        )

    return resolved_tables


def resolve_required_columns(config: dict) -> dict[str, tuple[str, ...]]:
    configured_columns = config.get("required_columns")
    if not isinstance(configured_columns, dict) or not configured_columns:
        raise KeyError("Config is missing 'required_columns'")

    resolved_columns: dict[str, tuple[str, ...]] = {}
    for table_key in REPLICA_TABLE_KEYS:
        column_names = configured_columns.get(table_key)
        if not isinstance(column_names, list) or not column_names:
            raise KeyError(f"Config is missing 'required_columns.{table_key}'")

        resolved_columns[table_key] = tuple(
            validate_identifier(column_name, f"required_columns.{table_key}")
            for column_name in column_names
        )

    return resolved_columns


def resolve_incremental_keys(sync_settings: dict) -> dict[str, tuple[str, ...]]:
    configured_keys = sync_settings.get("incremental_keys")
    if not isinstance(configured_keys, dict) or not configured_keys:
        raise KeyError("Config is missing 'replica_sync.incremental_keys'")

    resolved_keys: dict[str, tuple[str, ...]] = {}
    for table_key in REPLICA_TABLE_KEYS:
        key_columns = configured_keys.get(table_key)
        if not isinstance(key_columns, list) or not key_columns:
            raise KeyError(f"Config is missing 'replica_sync.incremental_keys.{table_key}'")

        resolved_keys[table_key] = tuple(
            validate_identifier(column_name, f"replica_sync.incremental_keys.{table_key}")
            for column_name in key_columns
        )

    return resolved_keys


def build_replica_load_settings(config: dict) -> ReplicaLoadSettings:
    source_settings = get_stagging_source_settings(config)
    sync_settings = get_replica_sync_settings(config)

    enabled = sync_settings.get("enabled", False)
    if not isinstance(enabled, bool):
        raise ValueError("Config value 'replica_sync.enabled' must be a boolean")

    db_role = source_settings.get("db_role")
    if not isinstance(db_role, str) or not db_role:
        raise KeyError("Config is missing 'stagging_source.db_role'")

    schema_name = validate_identifier(source_settings.get("schema"), "stagging_source.schema")

    load_mode = sync_settings.get("load_mode")
    if not isinstance(load_mode, str) or load_mode.lower() not in VALID_LOAD_MODES:
        raise ValueError(
            f"Config value 'replica_sync.load_mode' must be one of {sorted(VALID_LOAD_MODES)}"
        )
    load_mode = load_mode.lower()

    truncate_cascade = sync_settings.get("truncate_cascade", True)
    if not isinstance(truncate_cascade, bool):
        raise ValueError("Config value 'replica_sync.truncate_cascade' must be a boolean")

    chunk_size = sync_settings.get("chunk_size")
    if not isinstance(chunk_size, int) or chunk_size <= 0:
        raise ValueError("Config value 'replica_sync.chunk_size' must be a positive integer")

    create_schema_if_missing = sync_settings.get("create_schema_if_missing")
    if not isinstance(create_schema_if_missing, bool):
        raise ValueError(
            "Config value 'replica_sync.create_schema_if_missing' must be a boolean"
        )

    ddl_settings = source_settings.get("ddl")
    if not isinstance(ddl_settings, dict):
        raise KeyError("Config is missing 'stagging_source.ddl'")

    ddl_enabled = ddl_settings.get("enabled", False)
    if not isinstance(ddl_enabled, bool):
        raise ValueError("Config value 'stagging_source.ddl.enabled' must be a boolean")

    migration_table = validate_identifier(
        ddl_settings.get("migration_table"),
        "stagging_source.ddl.migration_table",
    )

    return ReplicaLoadSettings(
        enabled=enabled,
        db_role=db_role,
        schema_name=schema_name,
        load_mode=load_mode,
        truncate_cascade=truncate_cascade,
        chunk_size=chunk_size,
        create_schema_if_missing=create_schema_if_missing,
        target_tables=resolve_target_tables(source_settings),
        required_columns=resolve_required_columns(config),
        incremental_keys=resolve_incremental_keys(sync_settings),
        ddl=WarehouseDdlSettings(
            enabled=ddl_enabled,
            migration_table=migration_table,
            migration_directory=resolve_ddl_directory(ddl_settings),
        ),
    )
