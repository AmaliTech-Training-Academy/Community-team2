import pandas as pd
import psycopg2
from config import DB_CONFIG
from .base_source import DataSource


class PostgresSource(DataSource):
    """
    Data source implementation for retrieving data from a PostgreSQL database.

    This class connects to the CommunityBoard PostgreSQL database and
    extracts data required for the ETL pipeline. It implements the
    DataSource interface, allowing the pipeline to switch between
    different data sources (e.g., CSV or PostgreSQL) without modifying
    downstream processing logic.
    """

    def _connect(self):
        """
        Establish a connection to the PostgreSQL database.

        Returns
        -------
        psycopg2.extensions.connection
            Active connection object to the PostgreSQL database.
        """
        return psycopg2.connect(**DB_CONFIG)

    def get_users(self) -> pd.DataFrame:
        """
        Retrieve the users dataset from the PostgreSQL database.

        Returns
        -------
        pandas.DataFrame
            DataFrame containing all records from the users table.
        """

        with self._connect() as conn:
            return pd.read_sql("SELECT * FROM users", conn)

    def get_posts(self) -> pd.DataFrame:
        """
        Retrieve the posts dataset from the PostgreSQL database.

        Returns
        -------
        pandas.DataFrame
            DataFrame containing all records from the posts table.
        """

        with self._connect() as conn:
            return pd.read_sql("SELECT * FROM posts", conn)

    def get_comments(self) -> pd.DataFrame:
        """
        Retrieve the comments dataset from the PostgreSQL database.

        Returns
        -------
        pandas.DataFrame
            DataFrame containing all records from the comments table.
        """

        with self._connect() as conn:
            return pd.read_sql("SELECT * FROM comments", conn)