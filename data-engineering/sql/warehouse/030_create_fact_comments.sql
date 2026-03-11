CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_fact_comments_table }} (
    source_comment_id BIGINT PRIMARY KEY,
    post_key BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (post_key),
    commenter_user_key BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_users_table }} (user_key),
    created_at TIMESTAMP NOT NULL,
    comment_count INTEGER NOT NULL DEFAULT 1,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
