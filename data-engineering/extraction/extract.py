from sources.csv_source import CSVSource
from sources.postgres_source import PostgresSource

from utils.logging import get_logger


# -------------------------
# Logging Configuration
# -------------------------
logger = get_logger("extract_data")

def extract_data(config: dict):
    """
    Ingest raw datasets from the configured data source.
    """

    logger.info("Starting data ingestion")

    data_source = config["data_source"]
    if data_source == "csv":
        source = CSVSource(config)
    elif data_source == "postgres":
        source = PostgresSource(config)
    else:
        raise ValueError(f"Unsupported data source: {data_source}")

    users = source.get_users()
    posts = source.get_posts()
    comments = source.get_comments()

    logger.info("Data ingestion completed")
    return users, posts, comments