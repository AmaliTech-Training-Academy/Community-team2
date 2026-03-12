CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_posts_table }} (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_users_table }} (id),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
