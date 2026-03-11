ALTER TABLE {{ replica_schema }}.{{ replica_users_table }}
RENAME COLUMN username TO full_name;
