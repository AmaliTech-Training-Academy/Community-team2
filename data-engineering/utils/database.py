import psycopg2
from utils.config_loader import load_config


def _get_database_settings(config: dict) -> dict:
    database_settings = config.get("database")
    if not isinstance(database_settings, dict):
        raise KeyError("Config is missing the 'database' settings block")
    return database_settings


def _get_role_keys(database_settings: dict) -> dict:
    role_keys = database_settings.get("role_keys")
    if not isinstance(role_keys, dict) or not role_keys:
        raise KeyError("Config is missing 'database.role_keys'")
    return role_keys


def _get_required_fields(database_settings: dict) -> list[str]:
    required_fields = database_settings.get("required_fields")
    if not isinstance(required_fields, list) or not required_fields:
        raise KeyError("Config is missing 'database.required_fields'")
    return required_fields


def get_db_config(
    config: dict | None = None,
    *,
    db_role: str | None = None,
    db_config_key: str | None = None,
) -> dict:
    """
    Resolve a database connection config from the loaded pipeline config.
    """

    if db_role is not None and db_config_key is not None:
        raise ValueError("Provide either db_role or db_config_key, not both")
    if db_role is None and db_config_key is None:
        raise ValueError("Provide db_role or db_config_key")

    resolved_config = config or load_config()
    database_settings = _get_database_settings(resolved_config)
    role_keys = _get_role_keys(database_settings)
    required_fields = _get_required_fields(database_settings)

    selected_key = db_config_key or role_keys.get(db_role)
    if selected_key is None:
        raise ValueError(f"Unsupported db_role: {db_role}")

    db_config = resolved_config.get(selected_key)
    if not isinstance(db_config, dict):
        raise KeyError(f"Database config '{selected_key}' not found in pipeline config")

    missing_fields = [
        field
        for field in required_fields
        if field not in db_config or db_config[field] in (None, "")
    ]
    if missing_fields:
        raise KeyError(
            f"Database config '{selected_key}' is missing required fields: {missing_fields}"
        )

    return {field: db_config[field] for field in required_fields}


def get_connection(
    config: dict | None = None,
    *,
    db_role: str | None = None,
    db_config_key: str | None = None,
    db_config: dict | None = None,
):
    """
    Open a PostgreSQL connection using an explicit config dict or a config role.
    """

    resolved_db_config = db_config or get_db_config(
        config,
        db_role=db_role,
        db_config_key=db_config_key,
    )

    return psycopg2.connect(**resolved_db_config)
