CREATE TABLE IF NOT EXISTS {{ replica_schema }}.{{ replica_users_table }} (
    id BIGINT PRIMARY KEY,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT,
    created_at TIMESTAMP NOT NULL
);
