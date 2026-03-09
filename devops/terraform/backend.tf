# Terraform State Configuration (Optional - for team collaboration)

# Uncomment and configure for remote state storage

# terraform {
#   backend "s3" {
#     bucket         = "communityboard-terraform-state"
#     key            = "terraform.tfstate"
#     region         = "us-east-1"
#     encrypt        = true
#     dynamodb_table = "terraform-state-lock"
#   }
# }

# To enable remote state:
# 1. Create S3 bucket: aws s3 mb s3://communityboard-terraform-state
# 2. Create DynamoDB table: 
#    aws dynamodb create-table \
#      --table-name terraform-state-lock \
#      --attribute-definitions AttributeName=LockID,AttributeType=S \
#      --key-schema AttributeName=LockID,KeyType=HASH \
#      --billing-mode PAY_PER_REQUEST
# 3. Uncomment the backend configuration above
# 4. Run: terraform init -migrate-state
