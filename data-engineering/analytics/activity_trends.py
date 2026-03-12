from __future__ import annotations

from psycopg2 import sql

from analytics.materialized_views import (
    DateInput,
    build_activity_date_filter,
    fetch_kpi_rows,
    get_kpi_view_reference,
)


def fetch_activity_trends(
    *,
    config: dict | None = None,
    start_date: DateInput = None,
    end_date: DateInput = None,
) -> list[dict]:
    resolved_config, relation, _ = get_kpi_view_reference(
        "activity_trends",
        config=config,
    )
    where_clause, params = build_activity_date_filter(start_date, end_date)

    query = sql.SQL(
        """
        SELECT
            activity_date,
            posts_count,
            comments_count,
            total_activity_count
        FROM {}
        {}
        ORDER BY activity_date ASC
        """
    ).format(relation, where_clause)

    return fetch_kpi_rows(query, params, config=resolved_config)
