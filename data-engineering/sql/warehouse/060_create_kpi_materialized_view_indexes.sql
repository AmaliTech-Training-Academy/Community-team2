CREATE UNIQUE INDEX IF NOT EXISTS idx_kpi_top_contributors_mv_activity_user
ON {{ warehouse_schema }}.{{ warehouse_kpi_top_contributors_view }} (activity_date, user_key);

CREATE UNIQUE INDEX IF NOT EXISTS idx_kpi_activity_trends_mv_activity_date
ON {{ warehouse_schema }}.{{ warehouse_kpi_activity_trends_view }} (activity_date);

CREATE UNIQUE INDEX IF NOT EXISTS idx_kpi_posts_per_category_mv_activity_category
ON {{ warehouse_schema }}.{{ warehouse_kpi_posts_per_category_view }} (activity_date, category);
