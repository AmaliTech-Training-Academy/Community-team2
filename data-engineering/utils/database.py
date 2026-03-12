from collections.abc import Callable
from dataclasses import dataclass
import time

import psycopg2
from psycopg2 import InterfaceError, OperationalError

from utils.config_loader import load_config
from utils.logging import get_logger


logger = get_logger("database")
RETRYABLE_DB_ERRORS = (OperationalError, InterfaceError)


@dataclass(frozen=True)
class DatabaseRetrySettings:
    max_attempts: int
    initial_delay_seconds: float
    backoff_multiplier: float


@dataclass(frozen=True)
class DatabaseRuntimeSettings:
    connect_timeout_seconds: int
    statement_timeout_ms: int
    retry: DatabaseRetrySettings


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


def _resolve_config(config: dict | None = None) -> dict:
    return config or load_config()


def _build_retry_settings(database_settings: dict) -> DatabaseRetrySettings:
    retry_settings = database_settings.get("retry", {})
    if not isinstance(retry_settings, dict):
        raise ValueError("Config value 'database.retry' must be a mapping")

    max_attempts = retry_settings.get("max_attempts", 3)
    if not isinstance(max_attempts, int) or max_attempts < 1:
        raise ValueError("Config value 'database.retry.max_attempts' must be >= 1")

    initial_delay_seconds = retry_settings.get("initial_delay_seconds", 1.0)
    if not isinstance(initial_delay_seconds, (int, float)) or initial_delay_seconds < 0:
        raise ValueError(
            "Config value 'database.retry.initial_delay_seconds' must be >= 0"
        )

    backoff_multiplier = retry_settings.get("backoff_multiplier", 2.0)
    if not isinstance(backoff_multiplier, (int, float)) or backoff_multiplier < 1:
        raise ValueError(
            "Config value 'database.retry.backoff_multiplier' must be >= 1"
        )

    return DatabaseRetrySettings(
        max_attempts=max_attempts,
        initial_delay_seconds=float(initial_delay_seconds),
        backoff_multiplier=float(backoff_multiplier),
    )


def _build_runtime_settings(config: dict | None = None) -> DatabaseRuntimeSettings:
    database_settings = _get_database_settings(_resolve_config(config))

    connect_timeout_seconds = database_settings.get("connect_timeout_seconds", 10)
    if not isinstance(connect_timeout_seconds, int) or connect_timeout_seconds <= 0:
        raise ValueError(
            "Config value 'database.connect_timeout_seconds' must be a positive integer"
        )

    statement_timeout_ms = database_settings.get("statement_timeout_ms", 300000)
    if not isinstance(statement_timeout_ms, int) or statement_timeout_ms <= 0:
        raise ValueError(
            "Config value 'database.statement_timeout_ms' must be a positive integer"
        )

    return DatabaseRuntimeSettings(
        connect_timeout_seconds=connect_timeout_seconds,
        statement_timeout_ms=statement_timeout_ms,
        retry=_build_retry_settings(database_settings),
    )


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

    resolved_config = _resolve_config(config)
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

    resolved_db_config = {field: db_config[field] for field in required_fields}
    if "options" in db_config and db_config["options"]:
        resolved_db_config["options"] = db_config["options"]

    return resolved_db_config


def _resolve_db_config(
    config: dict | None = None,
    *,
    db_role: str | None = None,
    db_config_key: str | None = None,
    db_config: dict | None = None,
) -> dict:
    if db_config is not None:
        return dict(db_config)

    return get_db_config(
        config,
        db_role=db_role,
        db_config_key=db_config_key,
    )


def _normalize_port(port_value):
    if isinstance(port_value, str) and port_value.isdigit():
        return int(port_value)
    return port_value


def _append_statement_timeout_option(options: str | None, statement_timeout_ms: int) -> str:
    normalized_options = (options or "").strip()
    if "statement_timeout=" in normalized_options:
        return normalized_options

    timeout_option = f"-c statement_timeout={statement_timeout_ms}"
    return f"{normalized_options} {timeout_option}".strip()


def _build_connection_kwargs(
    resolved_db_config: dict,
    runtime_settings: DatabaseRuntimeSettings,
) -> dict:
    connection_kwargs = dict(resolved_db_config)
    connection_kwargs["port"] = _normalize_port(connection_kwargs.get("port"))
    connection_kwargs.setdefault("connect_timeout", runtime_settings.connect_timeout_seconds)
    connection_kwargs["options"] = _append_statement_timeout_option(
        connection_kwargs.get("options"),
        runtime_settings.statement_timeout_ms,
    )
    return connection_kwargs


def execute_with_db_retry(
    operation: Callable[[], object],
    *,
    config: dict | None = None,
    operation_name: str = "database operation",
):
    runtime_settings = _build_runtime_settings(config)
    delay_seconds = runtime_settings.retry.initial_delay_seconds

    # Retry only transient connectivity failures; data and contract errors should fail fast.
    for attempt in range(1, runtime_settings.retry.max_attempts + 1):
        try:
            return operation()
        except RETRYABLE_DB_ERRORS as exc:
            if attempt >= runtime_settings.retry.max_attempts:
                logger.exception(
                    "Database operation failed after %s attempts: %s",
                    attempt,
                    operation_name,
                )
                raise

            logger.warning(
                "Retryable database error during %s (attempt %s/%s): %s",
                operation_name,
                attempt,
                runtime_settings.retry.max_attempts,
                exc,
            )
            if delay_seconds > 0:
                time.sleep(delay_seconds)
            delay_seconds *= runtime_settings.retry.backoff_multiplier


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

    resolved_config = _resolve_config(config)
    resolved_db_config = _resolve_db_config(
        resolved_config,
        db_role=db_role,
        db_config_key=db_config_key,
        db_config=db_config,
    )
    runtime_settings = _build_runtime_settings(resolved_config)
    connection_kwargs = _build_connection_kwargs(resolved_db_config, runtime_settings)

    def _open_connection():
        return psycopg2.connect(**connection_kwargs)

    return execute_with_db_retry(
        _open_connection,
        config=resolved_config,
        operation_name=f"connect to {resolved_db_config.get('host')}:{resolved_db_config.get('port')}",
    )


def get_sqlalchemy_url(
    config: dict | None = None,
    *,
    db_role: str | None = None,
    db_config_key: str | None = None,
    db_config: dict | None = None,
):
    """
    Build a SQLAlchemy URL for a PostgreSQL database from pipeline config.
    """

    from sqlalchemy.engine import URL

    resolved_db_config = _resolve_db_config(
        _resolve_config(config),
        db_role=db_role,
        db_config_key=db_config_key,
        db_config=db_config,
    )

    return URL.create(
        "postgresql+psycopg2",
        username=resolved_db_config["user"],
        password=resolved_db_config["password"],
        host=resolved_db_config["host"],
        port=_normalize_port(resolved_db_config["port"]),
        database=resolved_db_config["database"],
    )


def get_sqlalchemy_engine(
    config: dict | None = None,
    *,
    db_role: str | None = None,
    db_config_key: str | None = None,
    db_config: dict | None = None,
    **engine_kwargs,
):
    """
    Create a SQLAlchemy engine for a PostgreSQL database from pipeline config.
    """

    from sqlalchemy import create_engine

    resolved_config = _resolve_config(config)
    resolved_db_config = _resolve_db_config(
        resolved_config,
        db_role=db_role,
        db_config_key=db_config_key,
        db_config=db_config,
    )
    runtime_settings = _build_runtime_settings(resolved_config)

    default_engine_kwargs = {
        "pool_pre_ping": True,
        "future": True,
    }
    default_engine_kwargs.update(engine_kwargs)

    connect_args = dict(default_engine_kwargs.get("connect_args", {}))
    connect_args.setdefault("connect_timeout", runtime_settings.connect_timeout_seconds)
    connect_args["options"] = _append_statement_timeout_option(
        connect_args.get("options") or resolved_db_config.get("options"),
        runtime_settings.statement_timeout_ms,
    )
    default_engine_kwargs["connect_args"] = connect_args

    return create_engine(
        get_sqlalchemy_url(
            resolved_config,
            db_role=db_role,
            db_config_key=db_config_key,
            db_config=resolved_db_config,
        ),
        **default_engine_kwargs,
    )
