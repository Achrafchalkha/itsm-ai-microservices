@echo off
REM Test script to verify Terraform with credentials

setlocal enabledelayedexpansion

REM Simulate credentials (replace with actual values from Jenkins)
REM Get these from: Jenkins > Credentials > Global
REM - azure-client-id
REM - azure-client-secret
set "CLIENT_ID=YOUR_CLIENT_ID_HERE"
set "CLIENT_SECRET=YOUR_CLIENT_SECRET_HERE"

echo.
echo ===== TESTING TERRAFORM PLAN WITH CREDENTIALS =====
echo.
echo CLIENT_ID: !CLIENT_ID:~0,10!...
echo CLIENT_SECRET: !CLIENT_SECRET:~0,10!...
echo.

cd terraform

REM Method: Using TF_CLI_ARGS environment variable
echo Running: terraform plan with TF_CLI_ARGS
set "TF_CLI_ARGS=-var=client_id=!CLIENT_ID! -var=client_secret=!CLIENT_SECRET!"
terraform.exe plan -out=tfplan

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Terraform plan failed with exit code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
) else (
    echo SUCCESS: Terraform plan completed
)

echo.
echo Applying plan...
terraform.exe apply -auto-approve tfplan

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Terraform apply failed with exit code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
) else (
    echo SUCCESS: Terraform apply completed
)

echo.
echo ===== TERRAFORM COMMANDS SUCCESSFUL =====
