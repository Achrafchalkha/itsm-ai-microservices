@echo off
echo Creating Azure Service Principal for Terraform...
echo.

REM Create service principal
az ad sp create-for-rbac --name "sp-itsm-terraform" --role="Contributor" --scopes="/subscriptions/339e2872-26be-4ffb-b15e-e85a3e5e4aed" --query "{clientId:appId, clientSecret:password, tenantId:tenant, subscriptionId:'339e2872-26be-4ffb-b15e-e85a3e5e4aed'}" -o json

echo.
echo ========================================
echo IMPORTANT: Save the output above!
echo ========================================
echo.
echo Add these to Jenkins credentials:
echo 1. Go to Jenkins: Manage Jenkins ^> Credentials ^> Global
echo 2. Add Secret Text - ID: azure-client-id
echo 3. Add Secret Text - ID: azure-client-secret
echo.
pause
