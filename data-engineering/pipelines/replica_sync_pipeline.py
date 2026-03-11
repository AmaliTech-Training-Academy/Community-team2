from extraction.extract import extract_data
from loading.load_to_stagging.load_to_stagging import load_to_stagging
from utils.logging import get_logger


logger = get_logger("replica_sync_pipeline")


def run_replica_sync(config: dict | None = None) -> dict[str, int]:
    """Run the replica sync pipeline: extract data from the backend source and load into staging tables.
    """
    resolved_config = config
    sync_settings = resolved_config.get("replica_sync")
    if not isinstance(sync_settings, dict):
        raise KeyError("Config is missing the 'replica_sync' settings block")

    enabled = sync_settings.get("enabled", False)
    if not isinstance(enabled, bool):
        raise ValueError("Config value 'replica_sync.enabled' must be a boolean")
    if not enabled:
        logger.info("Replica sync is disabled in config")
        return {}

    users_raw, posts_raw, comments_raw = extract_data(
        resolved_config,
        source_settings_key="backend_source",
        force_data_source="postgres",
    )

    result = load_to_stagging(
        {
            "users": users_raw,
            "posts": posts_raw,
            "comments": comments_raw,
        },
        resolved_config,
    )
    logger.info("Replica sync pipeline executed successfully: %s", result)
    return result
