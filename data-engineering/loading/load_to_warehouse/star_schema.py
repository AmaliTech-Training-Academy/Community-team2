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
            "Missing cleaned datasets required for warehouse load: "
            + ", ".join(missing_datasets)
        )

    for dataset_name in REQUIRED_SOURCE_DATASETS:
        dataset_df = datasets[dataset_name]
        if not isinstance(dataset_df, pd.DataFrame):
            raise TypeError(f"Dataset '{dataset_name}' must be a pandas DataFrame")


def build_dim_users_frame(users_df: pd.DataFrame) -> pd.DataFrame:
    dim_users_df = users_df.copy()
    if "role" not in dim_users_df.columns:
        dim_users_df["role"] = "unknown"
    else:
        normalized_role = dim_users_df["role"].astype("string").str.strip().str.lower()
        dim_users_df["role"] = normalized_role.mask(
            normalized_role.isna() | normalized_role.eq(""),
            "unknown",
        )

    dim_users_df = dim_users_df.loc[
        :,
        ["id", "full_name", "email", "role", "created_at"],
    ].rename(columns={"id": "source_user_id"})

    return dim_users_df.sort_values("source_user_id").reset_index(drop=True)


def map_surrogate_keys(
    source_series: pd.Series,
    key_map: Mapping[int, int],
    *,
    source_label: str,
    surrogate_label: str,
) -> pd.Series:
    mapped_series = source_series.map(key_map)
    missing_ids = sorted(
        {
            int(source_id)
            for source_id in source_series[mapped_series.isna()].dropna().tolist()
        }
    )
    if missing_ids:
        preview = ", ".join(str(source_id) for source_id in missing_ids[:10])
        suffix = "..." if len(missing_ids) > 10 else ""
        raise KeyError(
            f"Could not resolve {surrogate_label} for {source_label}: {preview}{suffix}"
        )

    return mapped_series.astype("int64")


def build_dim_posts_frame(
    posts_df: pd.DataFrame,
    user_key_map: Mapping[int, int],
) -> pd.DataFrame:
    dim_posts_df = posts_df.copy()
    dim_posts_df["author_user_key"] = map_surrogate_keys(
        dim_posts_df["user_id"],
        user_key_map,
        source_label="post.user_id",
        surrogate_label="author_user_key",
    )

    dim_posts_df = dim_posts_df.loc[
        :,
        [
            "id",
            "author_user_key",
            "title",
            "content",
            "category",
            "created_at",
        ],
    ].rename(columns={"id": "source_post_id"})

    return dim_posts_df.sort_values("source_post_id").reset_index(drop=True)


def build_fact_comments_frame(
    comments_df: pd.DataFrame,
    user_key_map: Mapping[int, int],
    post_key_map: Mapping[int, int],
) -> pd.DataFrame:
    fact_comments_df = comments_df.copy()
    fact_comments_df["post_key"] = map_surrogate_keys(
        fact_comments_df["post_id"],
        post_key_map,
        source_label="comment.post_id",
        surrogate_label="post_key",
    )
    fact_comments_df["commenter_user_key"] = map_surrogate_keys(
        fact_comments_df["user_id"],
        user_key_map,
        source_label="comment.user_id",
        surrogate_label="commenter_user_key",
    )
    fact_comments_df["comment_count"] = 1

    fact_comments_df = fact_comments_df.loc[
        :,
        ["id", "post_key", "commenter_user_key", "created_at", "comment_count"],
    ].rename(columns={"id": "source_comment_id"})

    return fact_comments_df.sort_values("source_comment_id").reset_index(drop=True)
