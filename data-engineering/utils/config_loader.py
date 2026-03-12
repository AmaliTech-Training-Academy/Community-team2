import copy
import os
import re
from pathlib import Path

import yaml
from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[1]
load_dotenv(PROJECT_ROOT / ".env")

ENV_PATTERN = re.compile(r"\$\{(\w+)(?::([^}]+))?\}")
SUPPORTED_DATA_SOURCES = {"csv", "postgres"}

# Singleton cache keyed by environment and data source override.
_CONFIG_CACHE: dict[tuple[str, str | None], dict] = {}


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


def _deep_merge(base, override):
    if not isinstance(base, dict) or not isinstance(override, dict):
        return copy.deepcopy(override)

    merged = copy.deepcopy(base)
    for key, value in override.items():
        if key in merged and isinstance(merged[key], dict) and isinstance(value, dict):
            merged[key] = _deep_merge(merged[key], value)
        else:
            merged[key] = copy.deepcopy(value)
    return merged


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


def _build_environment_config(raw_config: dict, env: str) -> dict:
    defaults = raw_config.get("defaults")
    environments = raw_config.get("environments")

    if isinstance(defaults, dict) and isinstance(environments, dict):
        environment_config = environments.get(env)
        if not isinstance(environment_config, dict):
            available = ", ".join(sorted(environments)) or "none"
            raise KeyError(
                f"Environment '{env}' not found in config.yml. Available environments: {available}"
            )

        resolved_config = _deep_merge(defaults, environment_config)
        resolved_config.setdefault("environment", env)
        return resolved_config

    return raw_config


def _apply_data_source_override(config: dict) -> dict:
    data_source_override = os.getenv("PIPELINE_DATA_SOURCE")
    if not data_source_override:
        return config

    normalized_data_source = data_source_override.strip().lower()
    if normalized_data_source not in SUPPORTED_DATA_SOURCES:
        raise ValueError(
            "Environment variable 'PIPELINE_DATA_SOURCE' must be one of "
            f"{sorted(SUPPORTED_DATA_SOURCES)}"
        )

    resolved_config = copy.deepcopy(config)
    resolved_config["data_source"] = normalized_data_source
    return resolved_config


def load_config():
    """
    Load pipeline configuration once per environment and data source override.
    """

    env = str(os.getenv("PIPELINE_ENV", "dev")).lower()
    data_source_override = os.getenv("PIPELINE_DATA_SOURCE")
    cache_key = (env, data_source_override)
    if cache_key in _CONFIG_CACHE:
        return _CONFIG_CACHE[cache_key]

    candidates = [
        PROJECT_ROOT / "configs" / "config.yml",
        PROJECT_ROOT / "configs" / "config.yaml",
        Path("/opt/airflow/data-engineering/config") / "config.yml",
        Path("/opt/airflow/data-engineering/config") / "config.yaml",
    ]

    path = next((candidate for candidate in candidates if candidate.exists()), None)

    if path is None:
        searched = ", ".join(str(candidate) for candidate in candidates)
        raise FileNotFoundError(
            f"Configuration file not found for PIPELINE_ENV='{env}'. "
            f"Searched: {searched}"
        )

    raw_config = _load_yaml_file(path)
    config = _build_environment_config(raw_config, env)
    config = _resolve_env_variables(config)
    config = _apply_data_source_override(config)

    _CONFIG_CACHE[cache_key] = config
    return _CONFIG_CACHE[cache_key]
