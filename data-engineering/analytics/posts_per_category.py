from __future__ import annotations

from psycopg2 import sql

from analytics.materialized_views import (
    DateInput,
    build_activity_date_filter,
    fetch_kpi_rows,
    get_kpi_view_reference,
)


def fetch_posts_per_category(
    *,
    config: dict | None = None,
    start_date: DateInput = None,
    end_date: DateInput = None,
) -> list[dict]:
    resolved_config, relation, _ = get_kpi_view_reference(
        "posts_per_category",
        config=config,
    )
    where_clause, params = build_activity_date_filter(start_date, end_date)

    query = sql.SQL(
        """
        SELECT
            category,
            SUM(posts_count)::BIGINT AS posts_count
        FROM {}
        {}
        GROUP BY category
        ORDER BY posts_count DESC, category ASC
        """
    ).format(relation, where_clause)

    return fetch_kpi_rows(query, params, config=resolved_config)
