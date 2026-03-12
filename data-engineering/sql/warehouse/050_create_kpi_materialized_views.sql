CREATE MATERIALIZED VIEW IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_kpi_top_contributors_view }} AS
WITH post_activity AS (
    SELECT
        DATE(created_at) AS activity_date,
        author_user_key AS user_key,
        COUNT(*)::BIGINT AS post_count,
        0::BIGINT AS comment_count
    FROM {{ warehouse_schema }}.{{ warehouse_dim_posts_table }}
    GROUP BY DATE(created_at), author_user_key
),
comment_activity AS (
    SELECT
        DATE(created_at) AS activity_date,
        commenter_user_key AS user_key,
        0::BIGINT AS post_count,
        SUM(comment_count)::BIGINT AS comment_count
    FROM {{ warehouse_schema }}.{{ warehouse_fact_comments_table }}
    GROUP BY DATE(created_at), commenter_user_key
),
combined_activity AS (
    SELECT * FROM post_activity
    UNION ALL
    SELECT * FROM comment_activity
)
SELECT
    activity.activity_date,
    users.user_key,
    users.source_user_id,
    users.full_name,
    users.role,
    SUM(activity.post_count)::BIGINT AS post_count,
    SUM(activity.comment_count)::BIGINT AS comment_count,
    SUM(activity.post_count + activity.comment_count)::BIGINT AS total_contributions
FROM combined_activity AS activity
JOIN {{ warehouse_schema }}.{{ warehouse_dim_users_table }} AS users
    ON users.user_key = activity.user_key
GROUP BY
    activity.activity_date,
    users.user_key,
    users.source_user_id,
    users.full_name,
    users.role
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_kpi_activity_trends_view }} AS
WITH daily_posts AS (
    SELECT
        DATE(created_at) AS activity_date,
        COUNT(*)::BIGINT AS posts_count
    FROM {{ warehouse_schema }}.{{ warehouse_dim_posts_table }}
    GROUP BY DATE(created_at)
),
daily_comments AS (
    SELECT
        DATE(created_at) AS activity_date,
        SUM(comment_count)::BIGINT AS comments_count
    FROM {{ warehouse_schema }}.{{ warehouse_fact_comments_table }}
    GROUP BY DATE(created_at)
),
activity_days AS (
    SELECT activity_date FROM daily_posts
    UNION
    SELECT activity_date FROM daily_comments
)
SELECT
    activity_days.activity_date,
    COALESCE(daily_posts.posts_count, 0)::BIGINT AS posts_count,
    COALESCE(daily_comments.comments_count, 0)::BIGINT AS comments_count,
    (
        COALESCE(daily_posts.posts_count, 0)
        + COALESCE(daily_comments.comments_count, 0)
    )::BIGINT AS total_activity_count
FROM activity_days
LEFT JOIN daily_posts
    ON daily_posts.activity_date = activity_days.activity_date
LEFT JOIN daily_comments
    ON daily_comments.activity_date = activity_days.activity_date
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_kpi_posts_per_category_view }} AS
SELECT
    DATE(created_at) AS activity_date,
    category,
    COUNT(*)::BIGINT AS posts_count
FROM {{ warehouse_schema }}.{{ warehouse_dim_posts_table }}
GROUP BY DATE(created_at), category
WITH NO DATA;
