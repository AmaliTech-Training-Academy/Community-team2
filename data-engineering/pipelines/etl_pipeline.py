from sources.csv_source import CSVSource
from sources.postgres_source import PostgresSource
from transformations.clean_comments import clean_comments
from transformations.clean_posts import clean_posts
from transformations.clean_users import clean_users
from utils.config_loader import load_config
from utils.logging import get_logger


logger = get_logger("etl_pipeline")


def _load_pipeline_config() -> dict:
    """
    Load pipeline configuration at runtime so imports do not execute I/O.
    """

    try:
        return load_config()
    except Exception:
        logger.exception("Failed to load configuration")
        raise


def ingest_data(config: dict):
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


def run_pipeline():
    """
    Run the ETL cleaning workflow end to end.
    """

    config = _load_pipeline_config()
    users_raw, posts_raw, comments_raw = ingest_data(config)

    users_clean = clean_users(users_raw, config)
    posts_clean = clean_posts(posts_raw, config)
    comments_clean = clean_comments(
        comments_raw,
        valid_post_ids=set(posts_clean["id"]),
        valid_user_ids=set(users_clean["id"]),
        config=config,
    )

    result = {
        "users": len(users_clean),
        "posts": len(posts_clean),
        "comments": len(comments_clean),
    }
    logger.info("Pipeline executed successfully: %s", result)
    return result


if __name__ == "__main__":
    run_pipeline()
