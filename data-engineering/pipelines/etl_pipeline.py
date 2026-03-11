from extraction.extract import extract_data
from loading.load_to_warehouse.load_to_analytics import load_to_warehouse
from pipelines.replica_sync_pipeline import run_replica_sync
from transformations.clean_comments import clean_comments
from transformations.clean_posts import clean_posts
from transformations.clean_users import clean_users
from utils.config_loader import load_config
from utils.logging import get_logger


logger = get_logger("etl_pipeline")

user_table = "users"
post_table = "posts"
comment_table = "comments"


def _load_pipeline_config() -> dict:
    """
    Load pipeline configuration at runtime so imports do not execute I/O.
    """

    try:
        return load_config()
    except Exception:
        logger.exception("Failed to load configuration")
        raise


def run_pipeline():
    """
    Run the end-to-end data flow: sync backend to replica, transform replica data,
    and load analytics tables.
    """

    config = _load_pipeline_config()
    replica_sync_counts = run_replica_sync(config)
    users_raw, posts_raw, comments_raw = extract_data(config)

    users_clean = clean_users(users_raw, config, user_table)
    posts_clean = clean_posts(posts_raw, config, post_table)
    comments_clean = clean_comments(
        comments_raw,
        valid_post_ids=set(posts_clean["id"]),
        valid_user_ids=set(users_clean["id"]),
        config=config,
        comment_table=comment_table,
    )

    transformed_counts = {
        "users": len(users_clean),
        "posts": len(posts_clean),
        "comments": len(comments_clean),
    }

    warehouse_counts = load_to_warehouse(
        {
            "users": users_clean,
            "posts": posts_clean,
            "comments": comments_clean,
        },
        config,
    )

    result = {
        "replica_sync": replica_sync_counts,
        **transformed_counts,
        "warehouse_load": warehouse_counts,
    }
    logger.info("Pipeline executed successfully: %s", result)
    return result


if __name__ == "__main__":
    run_pipeline()
