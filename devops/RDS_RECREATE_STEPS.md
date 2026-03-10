# Steps to Destroy and Recreate RDS with All 3 Databases

## 1. Destroy current RDS instance
cd devops/terraform
terraform destroy -target=module.database.null_resource.create_databases -auto-approve
terraform destroy -target=module.database.aws_db_instance.postgres -auto-approve

## 2. Recreate RDS with all 3 databases
terraform apply -auto-approve

## 3. Verify databases were created
./devops/scripts/get-db-credentials.sh
