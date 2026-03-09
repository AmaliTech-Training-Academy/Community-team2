import os
from pathlib import Path

import yaml
from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[1]
load_dotenv(PROJECT_ROOT / ".env")

def load_config():
    
    """
        Load pipeline configuration based on the current environment.

        The environment is determined by the PIPELINE_ENV environment variable.
        If not set, the pipeline defaults to the development configuration.

        Returns
        -------
        dict
            Parsed YAML configuration for the current environment.
    """

    env = os.getenv("PIPELINE_ENV", "dev")

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

    with open(path, "r", encoding="utf-8") as file:
        config = yaml.safe_load(file)

    return config
