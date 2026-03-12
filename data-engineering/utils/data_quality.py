import pandas as pd

from utils.logging import get_logger

# -------------------------
# Logging Configuration
# -------------------------

logger = get_logger("data_quality")


def log_row_count(df: pd.DataFrame, dataset_name: str) -> None:
    """
    Log the number of rows in a dataset.
    """

    logger.info(f"{dataset_name} row count: {len(df)}")


def log_null_counts(df: pd.DataFrame, dataset_name: str) -> None:
    """
    Log null value counts per column.
    """

    null_counts = df.isnull().sum()

    for column, count in null_counts.items():

        if count > 0:
            logger.warning(
                f"{dataset_name} column '{column}' contains {count} null values"
            )


def log_duplicate_count(df: pd.DataFrame, dataset_name: str, subset=None) -> None:
    """
    Log duplicate row counts.
    """

    duplicate_count = df.duplicated(subset=subset).sum()

    if duplicate_count > 0:

        logger.warning(
            f"{dataset_name} contains {duplicate_count} duplicate rows"
        )


def log_basic_metrics(df: pd.DataFrame, dataset_name: str) -> None:
    """
    Log basic dataset quality metrics.
    """

    log_row_count(df, dataset_name)
    log_null_counts(df, dataset_name)
    log_duplicate_count(df, dataset_name)



def strip_string_values(dataframe: pd.DataFrame) -> pd.DataFrame:
    normalized_df = dataframe.copy()
    string_columns = [
        column_name
        for column_name in normalized_df.columns
        if pd.api.types.is_object_dtype(normalized_df[column_name])
        or pd.api.types.is_string_dtype(normalized_df[column_name])
    ]

    for column_name in string_columns:
        normalized_df[column_name] = normalized_df[column_name].map(
            lambda value: value.strip() if isinstance(value, str) else value
        )

    return normalized_df
