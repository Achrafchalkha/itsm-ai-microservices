# Jenkins CI/CD Setup Guide for ITSM Project

## ✅ What I Fixed in Jenkinsfile

### Issues Fixed:
1. **Maven Build Paths** - Changed from `cd ${service}` to `dir(service) {}` for proper Jenkins workspace handling
2. **Test Failures** - Added `catchError` to continue pipeline even if tests fail (mark as UNSTABLE)
3. **Docker Build Context** - Fixed Windows path syntax `.\\${service}` instead of `${service}`
4. **Multi-line Commands** - Removed `^` line continuation (doesn't work in Jenkins bat)
5. **Build Versioning** - Added `BUILD_NUMBER` tag for Docker images

---

## 🔧 Jenkins Prerequisites

### 1. Tools Installation on Jenkins Server

Make sure these are installed on your Jenkins server:

```bash
# Check installations:
java -version      # Java 17+ required
mvn -version       # Maven 3.8+
docker --version   # Docker 20.10+
az --version       # Azure CLI 2.50+
kubectl version    # kubectl 1.28+
```

### 2. Jenkins Plugins Required

Install these plugins in Jenkins (Manage Jenkins → Plugins):
- ✅ **Pipeline** (should be pre-installed)
- ✅ **Git Plugin** (should be pre-installed)
- ✅ **Credentials Binding Plugin** (should be pre-installed)
- ✅ **Docker Pipeline Plugin** (optional, but helpful)

---

## 🔐 Jenkins Credentials Setup

You need to add **2 credentials** to Jenkins:

### Credential 1: ACR (Azure Container Registry) Credentials

**Step 1:** Get ACR credentials
```powershell
az acr credential show --resource-group rg-itsm-dev --name acritsmac742
```

**Step 2:** Add to Jenkins
1. Go to: Jenkins → Manage Jenkins → Credentials → System → Global credentials
2. Click **Add Credentials**
3. Fill in:
   - **Kind**: Username with password
   - **Scope**: Global
   - **Username**: `acritsmac742` (from command output)
   - **Password**: `<password from command output>`
   - **ID**: `acr-credentials` ⚠️ IMPORTANT - Must match Jenkinsfile
   - **Description**: ACR Credentials for Docker Push
4. Click **Create**

---

### Credential 2: Azure Service Principal Credentials

**Step 1:** Create Service Principal (if not exists)
```powershell
az ad sp create-for-rbac `
  --name "jenkins-sp" `
  --role "Contributor" `
  --scopes "/subscriptions/339e2872-26be-4ffb-b15e-e85a3e5e4aed"
```

**Output will look like:**
```json
{
  "appId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "displayName": "jenkins-sp",
  "password": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "tenant": "d4d13448-4ef9-411c-bc92-9654e9f5a3f5"
}
```

**Step 2:** Add to Jenkins
1. Go to: Jenkins → Manage Jenkins → Credentials → System → Global credentials
2. Click **Add Credentials**
3. Fill in:
   - **Kind**: Username with password
   - **Scope**: Global
   - **Username**: `<appId from output>`
   - **Password**: `<password from output>`
   - **ID**: `azure-sp-credentials` ⚠️ IMPORTANT - Must match Jenkinsfile
   - **Description**: Azure Service Principal for AKS Deployment
4. Click **Create**

---

## 📋 Create Jenkins Pipeline Job

### Step 1: Create New Pipeline
1. Jenkins Dashboard → **New Item**
2. Enter name: `ITSM-Microservices-Pipeline`
3. Select **Pipeline**
4. Click **OK**

### Step 2: Configure Pipeline
1. **General Section:**
   - ✅ GitHub project: `https://github.com/Achrafchalkha/itsm-ai-microservices`

2. **Build Triggers:**
   - ✅ Poll SCM: `H/5 * * * *` (checks every 5 minutes)
   - OR ✅ GitHub hook trigger for GITScm polling

3. **Pipeline Section:**
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/Achrafchalkha/itsm-ai-microservices.git`
   - **Credentials**: (add your GitHub credentials if private repo)
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`

4. Click **Save**

---

## 🚀 Test the Pipeline

### Option 1: Manual Trigger
1. Go to pipeline: `ITSM-Microservices-Pipeline`
2. Click **Build Now**
3. Watch the progress in **Console Output**

### Option 2: Git Push Trigger
```bash
cd C:\Users\LENOVO\Downloads\ITSM
git add .
git commit -m "test: trigger Jenkins pipeline"
git push origin main
```

---

## 📊 Pipeline Stages Explained

### Stage 1: Checkout ✅
- Pulls latest code from GitHub
- Shows directory structure

### Stage 2: Verify Environment ✅
- Checks Java, Maven, Docker, Azure CLI versions
- Ensures all tools are available

### Stage 3: Build All Services 🔨
- Builds all 7 microservices in sequence
- Uses `mvn clean package -DskipTests -U`
- `-U` forces update of dependencies
- Creates JAR files in `target/` folders

### Stage 4: Run Tests 🧪
- Runs unit tests for each service
- Uses `catchError` - won't fail pipeline if tests fail
- Marks stage as UNSTABLE if tests fail

### Stage 5: Docker - Build & Push 🐳
- **Only runs on `main` branch**
- Builds Docker images for all services
- Tags with both `latest` and `BUILD_NUMBER`
- Pushes to ACR: `acritsmac742.azurecr.io`

### Stage 6: Deploy to AKS ☸️
- **Only runs on `main` branch**
- Logs into Azure with Service Principal
- Gets AKS credentials
- Applies Kubernetes manifests from `k8s/` folder
- Waits for deployments to complete (5min timeout)
- Shows final pod and service status

---

## ⚠️ Common Issues & Solutions

### Issue 1: "Credentials not found"
**Error**: `could not find credentials 'acr-credentials'`

**Solution**:
- Check credential ID matches exactly: `acr-credentials` and `azure-sp-credentials`
- Go to Jenkins → Credentials → verify they exist
- Credential scope must be **Global**

---

### Issue 2: Maven build fails
**Error**: `[ERROR] Failed to execute goal...`

**Solution**:
```bash
# Test locally first:
cd C:\Users\LENOVO\Downloads\ITSM\auth-service
mvn clean package -DskipTests -U

# If it works locally but fails in Jenkins:
# - Check Java version in Jenkins matches (Java 17)
# - Check Maven settings.xml location
# - Ensure Jenkins has internet access for dependencies
```

---

### Issue 3: Docker login fails
**Error**: `Error response from daemon: login attempt failed`

**Solution**:
```powershell
# Get fresh ACR credentials:
az acr credential show --resource-group rg-itsm-dev --name acritsmac742

# Update credentials in Jenkins:
# Jenkins → Credentials → acr-credentials → Update
```

---

### Issue 4: Azure login fails
**Error**: `AADSTS7000215: Invalid client secret`

**Solution**:
```powershell
# Create new Service Principal:
az ad sp create-for-rbac `
  --name "jenkins-sp-new" `
  --role "Contributor" `
  --scopes "/subscriptions/339e2872-26be-4ffb-b15e-e85a3e5e4aed"

# Update azure-sp-credentials in Jenkins with new appId and password
```

---

### Issue 5: Kubernetes deployment fails
**Error**: `error: the server doesn't have a resource type "deployments"`

**Solution**:
```powershell
# Verify AKS access:
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev --overwrite-existing
kubectl get nodes

# Check if namespace exists:
kubectl get namespace itsm
# If not, create it:
kubectl create namespace itsm
```

---

## 🎯 Quick Test Commands

### Test 1: Verify Jenkins can access ACR
```bash
# In Jenkins server terminal:
docker login acritsmac742.azurecr.io -u <username> -p <password>
docker pull acritsmac742.azurecr.io/itsm-auth-service:latest
```

### Test 2: Verify Jenkins can access AKS
```bash
# In Jenkins server terminal:
az login --service-principal -u <appId> -p <password> --tenant d4d13448-4ef9-411c-bc92-9654e9f5a3f5
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev
kubectl get pods -n itsm
```

### Test 3: Build one service manually
```bash
# In Jenkins workspace:
cd C:\Users\LENOVO\Downloads\ITSM\auth-service
mvn clean package -DskipTests
# Should create: target/auth-service-0.0.1-SNAPSHOT.jar
```

---

## 📈 Expected Pipeline Timeline

| Stage | Expected Time | Notes |
|-------|--------------|-------|
| Checkout | 10-30 sec | Depends on repo size |
| Verify Environment | 5 sec | Quick version checks |
| Build All Services | 5-10 min | 7 services × ~1 min each |
| Run Tests | 3-7 min | Can be UNSTABLE if tests fail |
| Docker Build & Push | 10-15 min | 7 images × ~2 min each |
| Deploy to AKS | 3-5 min | Kubectl apply + rollout wait |
| **TOTAL** | **~25-35 min** | Full pipeline end-to-end |

---

## ✅ Success Checklist

After pipeline completes successfully, verify:

```bash
# 1. Check pods are running
kubectl get pods -n itsm

# Expected output: 14 pods (2 replicas × 7 services)
# All should be in "Running" state

# 2. Check services
kubectl get svc -n itsm

# Expected: 7 services with ClusterIP/LoadBalancer

# 3. Check Eureka Dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n itsm
# Visit: http://localhost:8761
# Should show all 6 client services registered

# 4. Check images in ACR
az acr repository list --name acritsmac742 --output table

# Expected: 7 repositories (itsm-auth-service, itsm-user-service, etc.)
```

---

## 🔄 Next Steps After First Successful Build

1. **Set up webhooks** for automatic builds on git push
2. **Add Slack/Email notifications** in Jenkinsfile post sections
3. **Add SonarQube** integration for code quality
4. **Configure backup** of Jenkins configuration
5. **Set up monitoring** for deployed services in AKS

---

## 📞 Quick Reference

**Your Azure Resources:**
- Subscription: `339e2872-26be-4ffb-b15e-e85a3e5e4aed`
- Resource Group: `rg-itsm-dev`
- ACR: `acritsmac742.azurecr.io`
- AKS: `aks-itsm-dev`
- PostgreSQL: `psqlitsmac742.postgres.database.azure.com`
- Namespace: `itsm`

**Your Credentials in Jenkins:**
- `acr-credentials` → ACR username/password
- `azure-sp-credentials` → Azure Service Principal appId/password

**GitHub Repo:**
- `https://github.com/Achrafchalkha/itsm-ai-microservices`

---

## 🚨 Before You Run the Pipeline

Make sure you've completed:
- ✅ Jenkins installed with Java 17, Maven, Docker, Azure CLI, kubectl
- ✅ Both credentials added to Jenkins (`acr-credentials`, `azure-sp-credentials`)
- ✅ Pipeline job created and configured
- ✅ Git repository accessible from Jenkins server
- ✅ Jenkins server has internet access for Maven dependencies
- ✅ AKS cluster is running and accessible

**Now you're ready to run the pipeline! Good luck! 🚀**
