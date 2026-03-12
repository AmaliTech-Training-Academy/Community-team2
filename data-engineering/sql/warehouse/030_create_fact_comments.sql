CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_comments_table }} (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_posts_table }} (id),
    user_id BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_users_table }} (id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
