import os
import re
from pathlib import Path

import yaml
from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[1]
load_dotenv(PROJECT_ROOT / ".env")

ENV_PATTERN = re.compile(r"\$\{(\w+)(?::([^}]*))?\}")
SUPPORTED_DATA_SOURCES = {"csv", "postgres"}


def _coerce_placeholder_value(value: str, default: str | None):
    if default is None:
        return value

    normalized_default = default.strip().lower()
    normalized_value = value.strip().lower()

    if normalized_default in {"true", "false"}:
        return normalized_value == "true"

    if re.fullmatch(r"-?\d+", default.strip()):
        return int(value.strip())

    return value


def _resolve_env_variables(value):
    """
    Recursively replace ${ENV_VAR} or ${ENV_VAR:default}
    placeholders with environment variables.
    """

    if isinstance(value, dict):
        return {k: _resolve_env_variables(v) for k, v in value.items()}

    if isinstance(value, list):
        return [_resolve_env_variables(v) for v in value]

    if isinstance(value, str):
        match = ENV_PATTERN.fullmatch(value)

        if match:
            env_var = match.group(1)
            default = match.group(2)
            env_value = os.getenv(env_var)

            if env_value is None:
                if default is None:
                    raise ValueError(
                        f"Environment variable '{env_var}' is not set and no default provided."
                    )
                env_value = default

            return _coerce_placeholder_value(env_value, default)

    return value


def _load_yaml_file(path: Path) -> dict:
    try:
        with open(path, "r", encoding="utf-8") as file:
            config = yaml.safe_load(file)
    except yaml.YAMLError as exc:
        raise RuntimeError(f"Invalid YAML in config file {path}") from exc

    if not config:
        raise ValueError(f"Configuration file {path} is empty.")
    if not isinstance(config, dict):
        raise ValueError(f"Configuration file {path} must contain a mapping at the top level.")

    return config


def _validate_data_source(config: dict) -> dict:
    data_source = config.get("data_source")
    if not isinstance(data_source, str) or data_source not in SUPPORTED_DATA_SOURCES:
        raise ValueError(
            "Config value 'data_source' must be one of "
            f"{sorted(SUPPORTED_DATA_SOURCES)}"
        )
    return config


def load_config() -> dict:
    """Load the single pipeline configuration file and resolve env-backed values."""

    candidates = [
        PROJECT_ROOT / "configs" / "config.yml",
        PROJECT_ROOT / "configs" / "config.yaml",
        Path("/opt/airflow/data-engineering/config") / "config.yml",
        Path("/opt/airflow/data-engineering/config") / "config.yaml",
    ]

    path = next((candidate for candidate in candidates if candidate.exists()), None)
    if path is None:
        searched = ", ".join(str(candidate) for candidate in candidates)
        raise FileNotFoundError(f"Configuration file not found. Searched: {searched}")

    config = _load_yaml_file(path)
    config = _resolve_env_variables(config)
    return _validate_data_source(config)
