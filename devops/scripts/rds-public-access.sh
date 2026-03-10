#!/bin/bash
set -e

REGION="eu-west-1"
DB_INSTANCE="communityboard-db"
ACTION=${1:-enable}

if [ "$ACTION" == "enable" ]; then
  echo "🌐 Making RDS publicly accessible..."
  aws rds modify-db-instance \
    --db-instance-identifier $DB_INSTANCE \
    --publicly-accessible \
    --apply-immediately \
    --region $REGION
  echo "✅ RDS is now publicly accessible"
  echo "📍 Endpoint: communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432"
  
elif [ "$ACTION" == "disable" ]; then
  echo "🔒 Making RDS private..."
  aws rds modify-db-instance \
    --db-instance-identifier $DB_INSTANCE \
    --no-publicly-accessible \
    --apply-immediately \
    --region $REGION
  echo "✅ RDS is now private"
  
else
  echo "Usage: $0 [enable|disable]"
  exit 1
fi
