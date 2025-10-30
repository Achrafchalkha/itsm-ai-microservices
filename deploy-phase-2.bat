@echo off
REM ════════════════════════════════════════════════════════════════════════════════
REM ITSM DevSecOps - Phase 2 Deployment Script
REM ════════════════════════════════════════════════════════════════════════════════
REM
REM PURPOSE: Automate Docker build, push, and Kubernetes deployment
REM
REM USAGE: .\deploy-phase-2.bat [build|push|deploy|all]
REM
REM PREREQUISITES:
REM   - Docker installed and running
REM   - Azure CLI installed
REM   - kubectl installed
REM   - Logged into Azure: az login
REM   - Logged into ACR: az acr login --name acritsmac742
REM
REM ════════════════════════════════════════════════════════════════════════════════

setlocal enabledelayedexpansion

REM Color codes for output
set COLOR_GREEN=10
set COLOR_YELLOW=14
set COLOR_RED=12

REM Configuration
set ACR_REGISTRY=acritsmac742.azurecr.io
set RESOURCE_GROUP=rg-itsm-dev
set AKS_CLUSTER=aks-itsm-dev
set NAMESPACE=itsm
set DOCKER_TAG=latest

REM List of services
set SERVICES=auth-service user-service ticket-service assignment-service notifications-service analytics-service eureka-server

cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════════════╗
echo ║                 ITSM DevSecOps - Phase 2 Deployment Script                    ║
echo ╚════════════════════════════════════════════════════════════════════════════════╝
echo.

REM Parse command line argument
if "%1"=="" (
    echo Usage: .\deploy-phase-2.bat [build^|push^|deploy^|all]
    echo.
    echo Options:
    echo   build   - Build Docker images for all services
    echo   push    - Push images to Azure Container Registry (needs build first)
    echo   deploy  - Deploy to AKS cluster
    echo   all     - Run all steps: build, push, deploy
    echo.
    exit /b 1
)

if /i "%1"=="build" call :BUILD_IMAGES
if /i "%1"=="push" call :PUSH_IMAGES
if /i "%1"=="deploy" call :DEPLOY_AKS
if /i "%1"=="all" (
    call :BUILD_IMAGES
    if errorlevel 1 exit /b 1
    call :PUSH_IMAGES
    if errorlevel 1 exit /b 1
    call :DEPLOY_AKS
    if errorlevel 1 exit /b 1
)

exit /b 0

REM ════════════════════════════════════════════════════════════════════════════════
REM FUNCTION: Build Docker Images
REM ════════════════════════════════════════════════════════════════════════════════
:BUILD_IMAGES
echo.
echo ╔════════════════════════════════════════════════════════════════════════════════╗
echo ║                        🔨 BUILDING DOCKER IMAGES                              ║
echo ╚════════════════════════════════════════════════════════════════════════════════╝
echo.

set BUILD_COUNT=0
for %%s in (%SERVICES%) do (
    set /a BUILD_COUNT+=1
    echo [!BUILD_COUNT!/7] Building %%s...
    
    if not exist "%%s\Dockerfile" (
        echo   ❌ ERROR: Dockerfile not found at %%s\Dockerfile
        exit /b 1
    )
    
    docker build -f %%s\Dockerfile -t %ACR_REGISTRY%/itsm-%%s:%DOCKER_TAG% %%s
    if errorlevel 1 (
        echo   ❌ Build failed for %%s
        exit /b 1
    )
    echo   ✅ Built: %ACR_REGISTRY%/itsm-%%s:%DOCKER_TAG%
    echo.
)

echo ✅ All Docker images built successfully!
exit /b 0

REM ════════════════════════════════════════════════════════════════════════════════
REM FUNCTION: Push to Azure Container Registry
REM ════════════════════════════════════════════════════════════════════════════════
:PUSH_IMAGES
echo.
echo ╔════════════════════════════════════════════════════════════════════════════════╗
echo ║              📤 PUSHING IMAGES TO AZURE CONTAINER REGISTRY                    ║
echo ╚════════════════════════════════════════════════════════════════════════════════╝
echo.

echo Verifying ACR login...
az acr login --name %ACR_REGISTRY:~0,-14% --expose-token >nul 2>&1
if errorlevel 1 (
    echo ❌ ERROR: Not logged into ACR
    echo   Run: az acr login --name acritsmac742
    exit /b 1
)
echo ✅ ACR login verified
echo.

set PUSH_COUNT=0
for %%s in (%SERVICES%) do (
    set /a PUSH_COUNT+=1
    echo [!PUSH_COUNT!/7] Pushing %%s...
    
    docker push %ACR_REGISTRY%/itsm-%%s:%DOCKER_TAG%
    if errorlevel 1 (
        echo   ❌ Push failed for %%s
        exit /b 1
    )
    echo   ✅ Pushed: %ACR_REGISTRY%/itsm-%%s:%DOCKER_TAG%
    echo.
)

echo ✅ All images pushed to ACR successfully!
echo.
echo Verifying in ACR:
az acr repository list --name %ACR_REGISTRY:~0,-14% --output table
exit /b 0

REM ════════════════════════════════════════════════════════════════════════════════
REM FUNCTION: Deploy to Azure AKS
REM ════════════════════════════════════════════════════════════════════════════════
:DEPLOY_AKS
echo.
echo ╔════════════════════════════════════════════════════════════════════════════════╗
echo ║                  🚀 DEPLOYING TO AZURE AKS CLUSTER                            ║
echo ╚════════════════════════════════════════════════════════════════════════════════╝
echo.

REM Get AKS credentials
echo Step 1: Getting AKS credentials...
az aks get-credentials --resource-group %RESOURCE_GROUP% --name %AKS_CLUSTER%
if errorlevel 1 (
    echo ❌ ERROR: Failed to get AKS credentials
    exit /b 1
)
echo ✅ AKS credentials obtained
echo.

REM Create namespace if not exists
echo Step 2: Ensuring namespace exists...
kubectl get namespace %NAMESPACE% >nul 2>&1
if errorlevel 1 (
    echo   Creating namespace %NAMESPACE%...
    kubectl create namespace %NAMESPACE%
)
echo ✅ Namespace ready
echo.

REM Create ACR secret for image pulls
echo Step 3: Creating image pull secret...
kubectl delete secret acr-secret -n %NAMESPACE% 2>nul
kubectl create secret docker-registry acr-secret ^
  --docker-server=%ACR_REGISTRY% ^
  --docker-username=acritsmac742 ^
  --docker-password=<PASSWORD> ^
  --docker-email=admin@itsm.local ^
  -n %NAMESPACE%
if errorlevel 1 (
    echo ⚠️  WARNING: Secret may not have been created properly
)
echo.

REM Apply Kubernetes manifests
echo Step 4: Deploying services to AKS...
if exist "k8s" (
    echo   Applying k8s manifests from k8s/ directory...
    kubectl apply -f k8s/ -n %NAMESPACE%
    if errorlevel 1 (
        echo ⚠️  WARNING: Some k8s manifests may have failed
    )
) else (
    echo ⚠️  INFO: No k8s/ directory found. Skipping manifest apply.
    echo   (Create k8s/ directory with deployment YAML files if needed)
)
echo.

REM Display deployment status
echo Step 5: Deployment Status
echo ────────────────────────────────────────────────────────────────────
kubectl get pods -n %NAMESPACE%
echo.
echo Service Endpoints:
echo ────────────────────────────────────────────────────────────────────
kubectl get svc -n %NAMESPACE%
echo.

echo ✅ Deployment complete!
echo.
echo 📊 Next Steps:
echo   1. Monitor pods:     kubectl get pods -n %NAMESPACE% -w
echo   2. View logs:        kubectl logs -n %NAMESPACE% -f deployment/auth-service
echo   3. Port forward:     kubectl port-forward -n %NAMESPACE% svc/auth-service 8081:8081
echo   4. Describe pod:     kubectl describe pod -n %NAMESPACE% ^<pod-name^>
echo.

exit /b 0

REM End of script
:EOF
endlocal
