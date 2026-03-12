from sources.csv_source import CSVSource
from sources.postgres_source import PostgresSource

from utils.logging import get_logger


logger = get_logger("extract_data")


def _read_source_datasets(source):
    users = source.get_users()
    posts = source.get_posts()
    comments = source.get_comments()
    return users, posts, comments


def extract_data(
    config: dict,
    *,
    source_settings_key: str = "stagging_source",
    force_data_source: str | None = None,
):
    """
    Ingest raw datasets from the configured data source.
    """

    logger.info("Starting data ingestion")

    data_source = force_data_source or config["data_source"]
    if data_source == "csv":
        source = CSVSource(config)
    elif data_source == "postgres":
        source = PostgresSource(config, settings_key=source_settings_key)
    else:
        raise ValueError(f"Unsupported data source: {data_source}")

    logger.info("Data ingestion completed")
    return _read_source_datasets(source)
