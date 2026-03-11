CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (
    post_key BIGSERIAL PRIMARY KEY,
    source_post_id BIGINT NOT NULL UNIQUE,
    author_user_key BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_users_table }} (user_key),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
