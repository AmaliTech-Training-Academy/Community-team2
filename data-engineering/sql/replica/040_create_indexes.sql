CREATE INDEX IF NOT EXISTS idx_replica_users_email
ON {{ replica_schema }}.{{ replica_users_table }} (email);

CREATE INDEX IF NOT EXISTS idx_replica_posts_user_id
ON {{ replica_schema }}.{{ replica_posts_table }} (user_id);

CREATE INDEX IF NOT EXISTS idx_replica_posts_category
ON {{ replica_schema }}.{{ replica_posts_table }} (category);

CREATE INDEX IF NOT EXISTS idx_replica_posts_created_at
ON {{ replica_schema }}.{{ replica_posts_table }} (created_at);

CREATE INDEX IF NOT EXISTS idx_replica_comments_post_id
ON {{ replica_schema }}.{{ replica_comments_table }} (post_id);

CREATE INDEX IF NOT EXISTS idx_replica_comments_user_id
ON {{ replica_schema }}.{{ replica_comments_table }} (user_id);

CREATE INDEX IF NOT EXISTS idx_replica_comments_created_at
ON {{ replica_schema }}.{{ replica_comments_table }} (created_at);
