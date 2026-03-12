CREATE INDEX IF NOT EXISTS idx_dim_posts_author_user_key
ON {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (author_user_key);

CREATE INDEX IF NOT EXISTS idx_dim_posts_category
ON {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (category);

CREATE INDEX IF NOT EXISTS idx_dim_posts_created_at
ON {{ warehouse_schema }}.{{ warehouse_dim_posts_table }} (created_at);

CREATE INDEX IF NOT EXISTS idx_fact_posts_author_user_key
ON {{ warehouse_schema }}.{{ warehouse_fact_posts_table }} (author_user_key);

CREATE INDEX IF NOT EXISTS idx_fact_posts_created_at
ON {{ warehouse_schema }}.{{ warehouse_fact_posts_table }} (created_at);

CREATE INDEX IF NOT EXISTS idx_fact_comments_post_key
ON {{ warehouse_schema }}.{{ warehouse_fact_comments_table }} (post_key);

CREATE INDEX IF NOT EXISTS idx_fact_comments_commenter_user_key
ON {{ warehouse_schema }}.{{ warehouse_fact_comments_table }} (commenter_user_key);

CREATE INDEX IF NOT EXISTS idx_fact_comments_created_at
ON {{ warehouse_schema }}.{{ warehouse_fact_comments_table }} (created_at);
