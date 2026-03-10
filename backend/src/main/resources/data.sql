INSERT INTO users (id, email, username, password, role, created_at, provider) VALUES
  (1, 'admin@amalitech.com', 'Admin User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW(), 'LOCAL'),
  (2, 'user@amalitech.com', 'Test User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MEMBER', NOW(), 'LOCAL')
ON CONFLICT (id) DO NOTHING;
