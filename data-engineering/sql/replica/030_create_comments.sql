CREATE TABLE IF NOT EXISTS {{ replica_schema }}.{{ replica_comments_table }} (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES {{ replica_schema }}.{{ replica_posts_table }} (id),
    user_id BIGINT NOT NULL REFERENCES {{ replica_schema }}.{{ replica_users_table }} (id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
