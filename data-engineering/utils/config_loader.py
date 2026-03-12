import os
import re
from pathlib import Path

import yaml
from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[1]
load_dotenv(PROJECT_ROOT / ".env")

ENV_PATTERN = re.compile(r"\$\{(\w+)(?::([^}]+))?\}")

# Singleton cache
_CONFIG_CACHE = None


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

            env_value = os.getenv(env_var, default)

            if env_value is None:
                raise ValueError(
                    f"Environment variable '{env_var}' is not set and no default provided."
                )

            return env_value

    return value


def load_config():
    """
    Load pipeline configuration once and cache it.
    """

    global _CONFIG_CACHE

    if _CONFIG_CACHE is not None:
        return _CONFIG_CACHE

    env = str(os.getenv("PIPELINE_ENV", "dev")).lower()

    candidates = [
        PROJECT_ROOT / "configs" / f"{env}.yml",
        PROJECT_ROOT / "configs" / f"{env}.yaml",
        Path("/opt/airflow/data-engineering/config") / f"{env}.yml",
        Path("/opt/airflow/data-engineering/config") / f"{env}.yaml",
    ]

    path = next((candidate for candidate in candidates if candidate.exists()), None)

    if path is None:
        searched = ", ".join(str(candidate) for candidate in candidates)
        raise FileNotFoundError(
            f"Configuration file not found for PIPELINE_ENV='{env}'. "
            f"Searched: {searched}"
        )

    try:
        with open(path, "r", encoding="utf-8") as file:
            config = yaml.safe_load(file)
    except yaml.YAMLError as e:
        raise RuntimeError(f"Invalid YAML in config file {path}") from e

    config = _resolve_env_variables(config)

    if not config:
        raise ValueError(f"Configuration file {path} is empty.")

    # Cache the config
    _CONFIG_CACHE = config

    return _CONFIG_CACHE