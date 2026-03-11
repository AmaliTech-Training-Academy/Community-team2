from dataclasses import dataclass
from pathlib import Path
import re

from utils.settings import PROJECT_ROOT


IDENTIFIER_PATTERN = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")
VALID_LOAD_MODES = {"append", "truncate", "incremental"}
WAREHOUSE_TABLE_KEYS = (
    "dim_users",
    "dim_posts",
    "fact_comments",
)


@dataclass(frozen=True)
class WarehouseDdlSettings:
    enabled: bool
    migration_table: str
    migration_directory: Path


@dataclass(frozen=True)
class WarehouseLoadSettings:
    enabled: bool
    db_role: str
    schema_name: str
    load_mode: str
    truncate_cascade: bool
    chunk_size: int
    create_schema_if_missing: bool
    target_tables: dict[str, str]
    incremental_keys: dict[str, tuple[str, ...]]
    ddl: WarehouseDdlSettings


def get_warehouse_settings(config: dict) -> dict:
    warehouse_settings = config.get("warehouse")
    if not isinstance(warehouse_settings, dict):
        raise KeyError("Config is missing the 'warehouse' settings block")
    return warehouse_settings


def get_warehouse_ddl_settings(warehouse_settings: dict) -> dict:
    ddl_settings = warehouse_settings.get("ddl")
    if not isinstance(ddl_settings, dict):
        raise KeyError("Config is missing 'warehouse.ddl'")
    return ddl_settings


def validate_identifier(identifier: str, setting_path: str) -> str:
    if not isinstance(identifier, str) or not IDENTIFIER_PATTERN.fullmatch(identifier):
        raise ValueError(f"Invalid SQL identifier for '{setting_path}': {identifier!r}")
    return identifier


def quote_identifier(identifier: str) -> str:
    return f'"{identifier}"'


def qualified_table_name(schema_name: str, table_name: str) -> str:
    return f"{quote_identifier(schema_name)}.{quote_identifier(table_name)}"


def resolve_target_tables(warehouse_settings: dict) -> dict[str, str]:
    configured_tables = warehouse_settings.get("tables")
    if not isinstance(configured_tables, dict) or not configured_tables:
        raise KeyError("Config is missing 'warehouse.tables'")

    resolved_tables: dict[str, str] = {}
    for table_key in WAREHOUSE_TABLE_KEYS:
        table_name = configured_tables.get(table_key)
        resolved_tables[table_key] = validate_identifier(
            table_name,
            f"warehouse.tables.{table_key}",
        )

    return resolved_tables


def resolve_incremental_keys(warehouse_settings: dict) -> dict[str, tuple[str, ...]]:
    configured_keys = warehouse_settings.get("incremental_keys")
    if not isinstance(configured_keys, dict) or not configured_keys:
        raise KeyError("Config is missing 'warehouse.incremental_keys'")

    resolved_keys: dict[str, tuple[str, ...]] = {}
    for table_key in WAREHOUSE_TABLE_KEYS:
        key_columns = configured_keys.get(table_key)
        if not isinstance(key_columns, list) or not key_columns:
            raise KeyError(f"Config is missing 'warehouse.incremental_keys.{table_key}'")

        resolved_keys[table_key] = tuple(
            validate_identifier(
                column_name,
                f"warehouse.incremental_keys.{table_key}",
            )
            for column_name in key_columns
        )

    return resolved_keys


def resolve_ddl_directory(ddl_settings: dict) -> Path:
    directory = ddl_settings.get("directory")
    if not isinstance(directory, str) or not directory.strip():
        raise KeyError("Config is missing 'warehouse.ddl.directory'")

    path = Path(directory)
    if not path.is_absolute():
        path = PROJECT_ROOT / path
    path = path.resolve()

    if not path.exists() or not path.is_dir():
        raise FileNotFoundError(f"Warehouse DDL directory not found: {path}")

    return path


def build_warehouse_load_settings(config: dict) -> WarehouseLoadSettings:
    warehouse_settings = get_warehouse_settings(config)

    enabled = warehouse_settings.get("enabled", False)
    if not isinstance(enabled, bool):
        raise ValueError("Config value 'warehouse.enabled' must be a boolean")

    db_role = warehouse_settings.get("db_role")
    if not isinstance(db_role, str) or not db_role:
        raise KeyError("Config is missing 'warehouse.db_role'")

    schema_name = validate_identifier(
        warehouse_settings.get("schema"),
        "warehouse.schema",
    )

    load_mode = warehouse_settings.get("load_mode")
    if not isinstance(load_mode, str) or load_mode.lower() not in VALID_LOAD_MODES:
        raise ValueError(
            f"Config value 'warehouse.load_mode' must be one of {sorted(VALID_LOAD_MODES)}"
        )
    load_mode = load_mode.lower()

    truncate_cascade = warehouse_settings.get("truncate_cascade", True)
    if not isinstance(truncate_cascade, bool):
        raise ValueError("Config value 'warehouse.truncate_cascade' must be a boolean")

    chunk_size = warehouse_settings.get("chunk_size")
    if not isinstance(chunk_size, int) or chunk_size <= 0:
        raise ValueError("Config value 'warehouse.chunk_size' must be a positive integer")

    create_schema_if_missing = warehouse_settings.get("create_schema_if_missing")
    if not isinstance(create_schema_if_missing, bool):
        raise ValueError(
            "Config value 'warehouse.create_schema_if_missing' must be a boolean"
        )

    target_tables = resolve_target_tables(warehouse_settings)
    incremental_keys = resolve_incremental_keys(warehouse_settings)
    ddl_settings = get_warehouse_ddl_settings(warehouse_settings)

    ddl_enabled = ddl_settings.get("enabled", False)
    if not isinstance(ddl_enabled, bool):
        raise ValueError("Config value 'warehouse.ddl.enabled' must be a boolean")

    migration_table = validate_identifier(
        ddl_settings.get("migration_table"),
        "warehouse.ddl.migration_table",
    )
    migration_directory = resolve_ddl_directory(ddl_settings)

    return WarehouseLoadSettings(
        enabled=enabled,
        db_role=db_role,
        schema_name=schema_name,
        load_mode=load_mode,
        truncate_cascade=truncate_cascade,
        chunk_size=chunk_size,
        create_schema_if_missing=create_schema_if_missing,
        target_tables=target_tables,
        incremental_keys=incremental_keys,
        ddl=WarehouseDdlSettings(
            enabled=ddl_enabled,
            migration_table=migration_table,
            migration_directory=migration_directory,
        ),
    )
