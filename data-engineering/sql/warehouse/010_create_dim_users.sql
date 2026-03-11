CREATE TABLE IF NOT EXISTS {{ warehouse_schema }}.{{ warehouse_dim_users_table }} (
    user_key BIGSERIAL PRIMARY KEY,
    source_user_id BIGINT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT,
    created_at TIMESTAMP NOT NULL,
    loaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
