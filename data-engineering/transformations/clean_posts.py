import pandas as pd


VALID_CATEGORIES = ["NEWS", "EVENT", "DISCUSSION", "ALERT"]


def clean_posts(posts: pd.DataFrame) -> pd.DataFrame:
    """
    Clean and standardize the posts dataset.

    The transformation ensures category consistency, removes
    duplicate records, and prepares timestamps for analytics.

    Parameters
    ----------
    posts : pandas.DataFrame
        Raw posts dataset.

    Returns
    -------
    pandas.DataFrame
        Cleaned posts dataset ready for analytics processing.
    """

    posts = posts.copy()

    # Standardize category values
    posts["category"] = posts["category"].str.upper()

    # Filter invalid categories
    posts = posts[posts["category"].isin(VALID_CATEGORIES)]

    # Remove duplicate posts
    posts = posts.drop_duplicates()

    # Convert timestamp column
    posts["created_at"] = pd.to_datetime(posts["created_at"])

    return posts