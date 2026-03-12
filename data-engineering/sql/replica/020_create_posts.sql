CREATE TABLE IF NOT EXISTS {{ replica_schema }}.{{ replica_posts_table }} (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES {{ replica_schema }}.{{ replica_users_table }} (id),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
