import pandas as pd
from utils.data_validation import validate_schema
from utils.logging import get_logger
from utils.data_quality import log_basic_metrics


# -------------------------
# Logging Configuration
# -------------------------

logger = get_logger("clean_posts")


def clean_posts(posts_main_df: pd.DataFrame, config: dict, post_table: str) -> pd.DataFrame:
    """
    Clean and standardize the posts dataset.

    Transformation steps include:
    - schema validation
    - duplicate removal
    - category normalization
    - timestamp parsing
    - invalid category filtering

    Parameters
    ----------
    posts_main_df : pd.DataFrame
        Raw posts dataset extracted from the source.

    Returns
    -------
    pd.DataFrame
        Cleaned posts dataset ready for analytics processing.
    """

    try:

        logger.info("Starting posts transformation")

        posts_df = posts_main_df.copy()

        log_basic_metrics(posts_df, f"{post_table}_raw")

        required_columns = config["required_columns"][post_table]

        validate_schema(posts_df, required_columns)

        logger.info("Schema validation passed")

        # -------------------------
        # Remove duplicates
        # -------------------------

        before = len(posts_df)

        dedupe_column = config["deduplicate_on"][post_table]
        posts_df = posts_df.drop_duplicates(subset=[dedupe_column])

        after = len(posts_df)

        logger.info(f"Removed {before - after} duplicate posts")

        # -------------------------
        # Normalize categories
        # -------------------------

        posts_df["category"] = posts_df["category"].str.upper()

        # -------------------------
        # Filter invalid categories
        # -------------------------

        valid_categories = config["categories"]

        invalid = posts_df[~posts_df["category"].isin(valid_categories)]

        if not invalid.empty:
            logger.warning(f"Found {len(invalid)} posts with invalid categories")

        posts_df = posts_df[posts_df["category"].isin(valid_categories)]

        # -------------------------
        # Timestamp normalization
        # -------------------------

        timestamp_column = config["timestamp_columns"][post_table]
        posts_df[timestamp_column] = pd.to_datetime(posts_df[timestamp_column], errors="coerce")

        null_dates = posts_df[timestamp_column].isnull().sum()

        if null_dates > 0:
            logger.warning(f"{null_dates} posts have invalid timestamps")

        posts_df = posts_df.dropna(subset=[timestamp_column])

        logger.info("Posts transformation completed successfully")

        log_basic_metrics(posts_df, f"{post_table}_cleaned")

        return posts_df

    except Exception as e:

        logger.exception("Posts transformation failed")

        raise e
