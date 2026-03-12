CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_users_table }} (
    id BIGINT PRIMARY KEY,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT,
    created_at TIMESTAMP NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
