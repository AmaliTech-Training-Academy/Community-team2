import re

import pandas as pd
from psycopg2 import sql

from utils.database import execute_with_db_retry, get_connection, get_db_config
from .base_source import DataSource


IDENTIFIER_PATTERN = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


class PostgresSource(DataSource):
    """
    Read operational data from a configured PostgreSQL source for extraction.
    """

    def __init__(self, config: dict | None = None, *, settings_key: str = "stagging_source"):
        if config is None:
            raise ValueError("PostgresSource requires a loaded config")

        self.config = config
        self.settings_key = settings_key
        source_config = config.get(settings_key)
        if not isinstance(source_config, dict):
            raise KeyError(f"Config is missing the '{settings_key}' settings block")

        db_role = source_config.get("db_role")
        if not isinstance(db_role, str) or not db_role:
            raise KeyError(f"Config is missing '{settings_key}.db_role'")

        self.schema_name = self._require_identifier(
            source_config.get("schema"),
            f"{settings_key}.schema",
        )

        tables = source_config.get("tables")
        if not isinstance(tables, dict) or not tables:
            raise KeyError(f"Config is missing '{settings_key}.tables'")

        self.db_config = get_db_config(config, db_role=db_role)
        self.users_table = self._require_table_name(tables, "users")
        self.posts_table = self._require_table_name(tables, "posts")
        self.comments_table = self._require_table_name(tables, "comments")
        categories_table = tables.get("categories")
        self.categories_table = None if categories_table is None else self._require_identifier(
            categories_table,
            f"{self.settings_key}.tables.categories",
        )

    def _connect(self):
        """
        Establish a connection to the configured PostgreSQL source.
        """

        return get_connection(self.config, db_config=self.db_config)

    def _require_identifier(self, value: str, setting_path: str) -> str:
        if not isinstance(value, str) or not IDENTIFIER_PATTERN.fullmatch(value):
            raise ValueError(f"Invalid SQL identifier for '{setting_path}': {value!r}")
        return value

    def _require_table_name(self, tables: dict, dataset_name: str) -> str:
        return self._require_identifier(
            tables.get(dataset_name),
            f"{self.settings_key}.tables.{dataset_name}",
        )

    def _read_table(self, table_name: str) -> pd.DataFrame:
        def _read_query() -> pd.DataFrame:
            with self._connect() as conn:
                query = sql.SQL("SELECT * FROM {}.{}").format(
                    sql.Identifier(self.schema_name),
                    sql.Identifier(table_name),
                )
                return pd.read_sql_query(query.as_string(conn), conn)

        return execute_with_db_retry(
            _read_query,
            config=self.config,
            operation_name=f"extract {self.schema_name}.{table_name}",
        )

    def _read_posts_with_category_lookup(self) -> pd.DataFrame:
        def _read_query() -> pd.DataFrame:
            with self._connect() as conn:
                query = sql.SQL(
                    """
                    SELECT
                        posts.id,
                        posts.user_id,
                        posts.title,
                        posts.content,
                        categories.name AS category,
                        posts.created_at
                    FROM {}.{} AS posts
                    JOIN {}.{} AS categories
                      ON posts.category_id = categories.id
                    """
                ).format(
                    sql.Identifier(self.schema_name),
                    sql.Identifier(self.posts_table),
                    sql.Identifier(self.schema_name),
                    sql.Identifier(self.categories_table),
                )
                return pd.read_sql_query(query.as_string(conn), conn)

        return execute_with_db_retry(
            _read_query,
            config=self.config,
            operation_name=f"extract {self.schema_name}.{self.posts_table} with category lookup",
        )

    def get_users(self) -> pd.DataFrame:
        return self._read_table(self.users_table)

    def get_posts(self) -> pd.DataFrame:
        if self.categories_table is not None:
            return self._read_posts_with_category_lookup()
        return self._read_table(self.posts_table)

    def get_comments(self) -> pd.DataFrame:
        return self._read_table(self.comments_table)
