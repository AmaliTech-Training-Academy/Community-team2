import pandas as pd


class CSVSource:
    """
    Data source implementation that reads data from CSV files.

    This source is primarily used in development environments
    where the backend database is not yet available.
    """

    def __init__(self, config):
        """
        Initialize CSV source using configuration paths.

        Parameters
        ----------
        config : dict
            Pipeline configuration containing CSV file paths.
        """
        self.users_path = config["csv_paths"]["users"]
        self.posts_path = config["csv_paths"]["posts"]
        self.comments_path = config["csv_paths"]["comments"]

    def get_users(self):
        """
        Load users dataset.

        Returns
        -------
        pandas.DataFrame
            DataFrame containing users data.
        """
        return pd.read_csv(self.users_path)

    def get_posts(self):
        """
        Load posts dataset.

        Returns
        -------
        pandas.DataFrame
            DataFrame containing posts data.
        """
        return pd.read_csv(self.posts_path)

    def get_comments(self):
        """
        Load comments dataset.

        Returns
        -------
        pandas.DataFrame
            DataFrame containing comments data.
        """
        return pd.read_csv(self.comments_path)