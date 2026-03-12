import pandas as pd
from typing import Set
from utils.data_validation import validate_schema, validate_foreign_key
from utils.data_quality import log_basic_metrics, strip_string_values
from utils.logging import get_logger

# -------------------------
# Logging Configuration
# -------------------------

logger = get_logger("clean_comments")

def clean_comments(
    comments_main_df: pd.DataFrame,
    valid_post_ids: Set[int],
    valid_user_ids: Set[int],
    config: dict,
    comment_table: str
) -> pd.DataFrame:
    """
    Clean and validate the comments dataset.

    Transformation steps include:
    - schema validation
    - duplicate removal
    - timestamp normalization
    - foreign key validation for post_id and user_id
    - data quality metric logging

    Parameters
    ----------
    comments_df : pd.DataFrame
        Raw comments dataset.
    valid_post_ids : Set[int]
        Set of valid post IDs from the posts dataset.
    valid_user_ids : Set[int]
        Set of valid user IDs from the users dataset.

    Returns
    -------
    pd.DataFrame
        Cleaned comments dataset ready for analytics.
    """

    try:
        logger.info("Starting comments transformation")

        comments_df = strip_string_values(comments_main_df.copy())
        reference_sets = {
            "posts": valid_post_ids,
            "users": valid_user_ids,
        } # HAS TO BE DYNAMIC

        log_basic_metrics(comments_df, f"{comment_table}_raw")

        required_columns = config["required_columns"][comment_table]
        validate_schema(comments_df, required_columns)

        logger.info("Schema validation passed")

        # -------------------------
        # Remove duplicate comments
        # -------------------------
        before = len(comments_df)
        dedupe_column = config["deduplicate_on"][comment_table]
        comments_df = comments_df.drop_duplicates(subset=[dedupe_column])
        after = len(comments_df)

        logger.info(f"Removed {before - after} duplicate comments")

        # -------------------------
        # Normalize timestamps
        # -------------------------
        timestamp_column = config["timestamp_columns"][comment_table]
        comments_df[timestamp_column] = pd.to_datetime(
            comments_df[timestamp_column],
            errors="coerce"
        )

        null_dates = comments_df[timestamp_column].isnull().sum()
        if null_dates > 0:
            logger.warning(f"{null_dates} comments have invalid timestamps")

        comments_df = comments_df.dropna(subset=[timestamp_column])

        # -------------------------
        # Foreign key validation
        # -------------------------
        foreign_keys = config.get("foreign_keys", {}).get(comment_table, {})
        for column, reference_table in foreign_keys.items():
            if column not in comments_df.columns:
                logger.error(
                    f"Foreign key column '{column}' missing from comments dataset"
                )
                raise ValueError(f"Missing foreign key column: {column}")

            reference_values = reference_sets.get(reference_table)
            if reference_values is None:
                logger.error(
                    "No reference values configured for %s.%s -> %s",
                    comment_table,
                    column,
                    reference_table,
                )
                raise ValueError(
                    f"Missing reference values for {comment_table}.{column} -> {reference_table}"
                )

            validate_foreign_key(
                comments_df,
                column=column,
                reference_values=reference_values,
                entity_name=f"{comment_table}.{column}"
            )

            logger.info("Foreign key validation passed for %s", column)

        log_basic_metrics(comments_df, f"{comment_table}_clean")
        logger.info("Comments transformation completed successfully")
        return comments_df

    except Exception:
        logger.exception("Comments transformation failed")
        raise
