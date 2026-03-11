import pandas as pd
from typing import List
from utils.logging import get_logger


# -------------------------
# Logging Configuration
# -------------------------

logger = get_logger("data_validation")


def validate_schema(df: pd.DataFrame, required_columns: List[str]) -> None:
    """
    Validate that required columns exist in a dataframe.
    """

    missing_cols = [col for col in required_columns if col not in df.columns]

    if missing_cols:
        logger.error(f"Missing columns detected: {missing_cols}")
        raise ValueError(f"Missing required columns: {missing_cols}")


def validate_foreign_key(
    df: pd.DataFrame,
    column: str,
    reference_values: set,
    entity_name: str
) -> None:
    """
    Validate foreign key relationships.

    Ensures that values in a column exist in a reference dataset.
    """

    invalid_rows = df[~df[column].isin(reference_values)]

    if not invalid_rows.empty:

        logger.warning(
            f"{len(invalid_rows)} invalid {entity_name} references detected"
        )

        raise ValueError(
            f"{entity_name} contains invalid foreign key references"
        )