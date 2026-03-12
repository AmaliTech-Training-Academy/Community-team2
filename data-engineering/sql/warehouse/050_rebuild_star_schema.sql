DROP TABLE IF EXISTS {{ warehouse_schema }}.{{ warehouse_fact_comments_table }} CASCADE;
DROP TABLE IF EXISTS {{ warehouse_schema }}.{{ warehouse_fact_posts_table }} CASCADE;
DROP TABLE IF EXISTS {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} CASCADE;
DROP TABLE IF EXISTS {{ warehouse_schema }}.{{ warehouse_dim_users_table }} CASCADE;

CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_dim_users_table }} (
    user_key BIGSERIAL PRIMARY KEY,
    source_user_id BIGINT NOT NULL UNIQUE,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT,
    created_at TIMESTAMP NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

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

CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_fact_posts_table }} (
    source_post_id BIGINT PRIMARY KEY,
    post_key BIGINT NOT NULL UNIQUE REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (post_key),
    author_user_key BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_users_table }} (user_key),
    created_at TIMESTAMP NOT NULL,
    post_count INTEGER NOT NULL DEFAULT 1,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_fact_comments_table }} (
    source_comment_id BIGINT PRIMARY KEY,
    post_key BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (post_key),
    commenter_user_key BIGINT NOT NULL REFERENCES {{ warehouse_schema }}.{{ warehouse_dim_users_table }} (user_key),
    created_at TIMESTAMP NOT NULL,
    comment_count INTEGER NOT NULL DEFAULT 1,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
