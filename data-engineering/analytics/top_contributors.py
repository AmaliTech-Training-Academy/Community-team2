from __future__ import annotations

from psycopg2 import sql

from analytics.materialized_views import (
    DateInput,
    build_activity_date_filter,
    fetch_kpi_rows,
    get_kpi_view_reference,
)


def fetch_top_contributors(
    *,
    config: dict | None = None,
    limit: int = 10,
    start_date: DateInput = None,
    end_date: DateInput = None,
) -> list[dict]:
    if not isinstance(limit, int) or limit <= 0:
        raise ValueError("'limit' must be a positive integer")

    resolved_config, relation, _ = get_kpi_view_reference(
        "top_contributors",
        config=config,
    )
    where_clause, params = build_activity_date_filter(start_date, end_date)
    params.append(limit)

    query = sql.SQL(
        """
        SELECT
            source_user_id,
            full_name,
            role,
            SUM(post_count)::BIGINT AS post_count,
            SUM(comment_count)::BIGINT AS comment_count,
            SUM(total_contributions)::BIGINT AS total_contributions
        FROM {}
        {}
        GROUP BY source_user_id, full_name, role
        ORDER BY total_contributions DESC, post_count DESC, comment_count DESC, full_name ASC
        LIMIT %s
        """
    ).format(relation, where_clause)

    return fetch_kpi_rows(query, params, config=resolved_config)
