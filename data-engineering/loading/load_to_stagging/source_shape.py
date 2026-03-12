from collections.abc import Mapping

import pandas as pd


REQUIRED_SOURCE_DATASETS = ("users", "posts", "comments")


def validate_source_datasets(datasets: Mapping[str, pd.DataFrame]) -> None:
    missing_datasets = [
        dataset_name
        for dataset_name in REQUIRED_SOURCE_DATASETS
        if dataset_name not in datasets
    ]
    if missing_datasets:
        raise KeyError(
            "Missing raw datasets required for replica sync: "
            + ", ".join(missing_datasets)
        )

    for dataset_name in REQUIRED_SOURCE_DATASETS:
        dataset_df = datasets[dataset_name]
        if not isinstance(dataset_df, pd.DataFrame):
            raise TypeError(f"Dataset '{dataset_name}' must be a pandas DataFrame")


def _require_columns(
    dataset_df: pd.DataFrame,
    dataset_name: str,
    columns: tuple[str, ...],
) -> pd.DataFrame:
    missing_columns = [column_name for column_name in columns if column_name not in dataset_df.columns]
    if missing_columns:
        raise KeyError(
            f"Raw dataset '{dataset_name}' is missing required columns: {missing_columns}"
        )
    return dataset_df.loc[:, list(columns)]


def build_users_frame(users_df: pd.DataFrame, required_columns: tuple[str, ...]) -> pd.DataFrame:
    replica_users_df = users_df.copy()
    if "username" in required_columns and "username" not in replica_users_df.columns and "name" in replica_users_df.columns:
        replica_users_df = replica_users_df.rename(columns={"name": "username"})
    if "role" in required_columns and "role" not in replica_users_df.columns:
        replica_users_df["role"] = None

    replica_users_df = _require_columns(replica_users_df, "users", required_columns)
    return replica_users_df.sort_values("id").reset_index(drop=True)


def build_posts_frame(posts_df: pd.DataFrame, required_columns: tuple[str, ...]) -> pd.DataFrame:
    replica_posts_df = _require_columns(posts_df.copy(), "posts", required_columns)
    return replica_posts_df.sort_values("id").reset_index(drop=True)


def build_comments_frame(comments_df: pd.DataFrame, required_columns: tuple[str, ...]) -> pd.DataFrame:
    replica_comments_df = _require_columns(comments_df.copy(), "comments", required_columns)
    return replica_comments_df.sort_values("id").reset_index(drop=True)
