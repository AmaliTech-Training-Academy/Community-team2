# Database Access for Development

## Option 1: Public Access (Easiest for Development)

**Enable public access:**
```bash
bash devops/scripts/rds-public-access.sh enable
```

**Connect directly:**
```bash
psql -h communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com \
     -p 5432 -U postgres -d communityboard
```

Password: `postgres`

**Disable when done:**
```bash
bash devops/scripts/rds-public-access.sh disable
```

## Option 2: Port Forwarding via ECS Exec (Private Subnet)

**Terminal 1 - Start tunnel:**
```bash
bash devops/scripts/db-access.sh
```

**Terminal 2 - Connect:**
```bash
psql -h localhost -p 5433 -U postgres -d communityboard
```

## Common Commands

```sql
\l                    -- List databases
\c communityboard     -- Connect to database
\dt                   -- List tables
SELECT * FROM users;  -- View users
SELECT * FROM posts;  -- View posts
```
