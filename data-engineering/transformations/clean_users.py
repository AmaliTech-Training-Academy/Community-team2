import pandas as pd

from utils.data_validation import validate_schema
from utils.data_quality import log_basic_metrics
from utils.logging import get_logger

# -------------------------
# Logging Configuration
# -------------------------

logger = get_logger("clean_users")

user_table = "users"


def clean_users(users_main_df: pd.DataFrame, config: dict) -> pd.DataFrame:
    """
    Clean and validate the users dataset.

    Transformation steps include:
    - schema validation
    - duplicate removal
    - timestamp normalization
    - data quality metric logging
    """

    try:
        logger.info("Starting users transformation")

        users_df = users_main_df.copy()
        if "username" not in users_df.columns and "name" in users_df.columns:
            logger.info("Renaming legacy 'name' column to 'username'")
            users_df = users_df.rename(columns={"name": "username"})

        log_basic_metrics(users_df, f"{user_table}_raw")

        required_columns = config["required_columns"][user_table]
        validate_schema(users_df, required_columns)

        logger.info("Schema validation passed")

        # -------------------------
        # Remove duplicate users
        # -------------------------
        dedupe_column = config["deduplicate_on"][user_table]
        before = len(users_df)
        users_df = users_df.drop_duplicates(subset=[dedupe_column])
        after = len(users_df)

        logger.info(f"Removed {before - after} duplicate users")

        if "role" in users_df.columns:
            users_df["role"] = users_df["role"].astype(str).str.lower()

        # -------------------------
        # Normalize timestamps
        # -------------------------
        timestamp_column = config["timestamp_columns"][user_table]
        users_df[timestamp_column] = pd.to_datetime(
            users_df[timestamp_column],
            errors="coerce"
        )

        # -------------------------
        # Fill missing values in timestamp column with max timestamp
        # or current pipeline run time
        # -------------------------
        missing_dates = users_df[timestamp_column].isnull().sum()
        if missing_dates > 0:
            fallback_timestamp = users_df[timestamp_column].max()
            if pd.isna(fallback_timestamp):
                fallback_timestamp = pd.Timestamp.utcnow()

            logger.warning(
                f"{missing_dates} users missing {timestamp_column}. "
                f"Filling with {fallback_timestamp}"
            )
            users_df[timestamp_column] = users_df[timestamp_column].fillna(
                fallback_timestamp
            )

        log_basic_metrics(users_df, f"{user_table}_clean")
        logger.info("Users transformation completed successfully")
        return users_df

    except Exception:
        logger.exception("Users transformation failed")
        raise
