CREATE INDEX IF NOT EXISTS idx_warehouse_posts_user_id
ON {{ warehouse_schema }}.{{ warehouse_posts_table }} (user_id);

CREATE INDEX IF NOT EXISTS idx_warehouse_posts_category
ON {{ warehouse_schema }}.{{ warehouse_posts_table }} (category);

CREATE INDEX IF NOT EXISTS idx_warehouse_posts_created_at
ON {{ warehouse_schema }}.{{ warehouse_posts_table }} (created_at);

CREATE INDEX IF NOT EXISTS idx_warehouse_comments_post_id
ON {{ warehouse_schema }}.{{ warehouse_comments_table }} (post_id);

CREATE INDEX IF NOT EXISTS idx_warehouse_comments_user_id
ON {{ warehouse_schema }}.{{ warehouse_comments_table }} (user_id);

CREATE INDEX IF NOT EXISTS idx_warehouse_comments_created_at
ON {{ warehouse_schema }}.{{ warehouse_comments_table }} (created_at);
