import pandas as pd
from utils.database import get_connection, get_db_config
from .base_source import DataSource


class PostgresSource(DataSource):
    """
    Read operational data from the configured PostgreSQL source for ETL extraction.
    """

    def __init__(self, config: dict | None = None):
        if config is None:
            raise ValueError("PostgresSource requires a loaded config")

        postgres_source_config = config.get("postgres_source")
        if not isinstance(postgres_source_config, dict):
            raise KeyError("Config is missing the 'postgres_source' settings block")

        db_role = postgres_source_config.get("db_role")
        if not isinstance(db_role, str) or not db_role:
            raise KeyError("Config is missing 'postgres_source.db_role'")

        tables = postgres_source_config.get("tables")
        if not isinstance(tables, dict) or not tables:
            raise KeyError("Config is missing 'postgres_source.tables'")

        self.db_config = get_db_config(config, db_role=db_role)
        self.users_table = self._require_table_name(tables, "users")
        self.posts_table = self._require_table_name(tables, "posts")
        self.comments_table = self._require_table_name(tables, "comments")

    def _connect(self):
        """
        Establish a connection to the configured PostgreSQL source.
        """

        return get_connection(db_config=self.db_config)

    def _require_table_name(self, tables: dict, dataset_name: str) -> str:
        table_name = tables.get(dataset_name)
        if not isinstance(table_name, str) or not table_name:
            raise KeyError(f"Config is missing 'postgres_source.tables.{dataset_name}'")
        return table_name

    def _read_table(self, table_name: str) -> pd.DataFrame:
        with self._connect() as conn:
            return pd.read_sql(f"SELECT * FROM {table_name}", conn)

    def get_users(self) -> pd.DataFrame:
        """
        Retrieve the users dataset from the configured PostgreSQL source.
        """

        return self._read_table(self.users_table)

    def get_posts(self) -> pd.DataFrame:
        """
        Retrieve the posts dataset from the configured PostgreSQL source.
        """

        return self._read_table(self.posts_table)

    def get_comments(self) -> pd.DataFrame:
        """
        Retrieve the comments dataset from the configured PostgreSQL source.
        """

        return self._read_table(self.comments_table)
