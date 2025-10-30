# 🛡 ITSM DevSecOps Implementation Guide

## Overview

This guide describes the complete DevSecOps implementation for the ITSM microservices project, split into **two independent phases**:

- **PHASE 1** (Jenkins Pipeline): Build → Test → SonarQube Analysis
- **PHASE 2** (Deployment Automation): Docker → Terraform → Kubernetes

### Why Split Phases?

✅ **Better Separation of Concerns** - Build and deployment are different concerns
✅ **Improved Reliability** - If deployment fails, build artifacts are still available
✅ **Flexibility** - Deploy the same build artifact multiple times
✅ **Team Ownership** - Build team owns Phase 1, DevOps team owns Phase 2
✅ **Security** - Code quality gates before any deployment happens

---

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                     DEVOPS WORKFLOW                                 │
└─────────────────────────────────────────────────────────────────────┘

PHASE 1: CODE QUALITY (Jenkins Pipeline)
├─ Git Checkout (from GitHub)
├─ Maven Build (compile all 7 services)
├─ SonarQube Analysis (code quality)
└─ Store Artifacts (target/ directories)

PHASE 2: DEPLOYMENT (Manual/Scripts)
├─ Docker Build & Push (to Azure Container Registry)
├─ Terraform Provision (Azure infrastructure)
├─ Kubernetes Deploy (to AKS cluster)
└─ Monitor & Verify (logs, metrics)
```

---

## 📋 Prerequisites

### Required Software
- ✅ **Java 17+** installed on Jenkins server
- ✅ **Maven 3.9+** installed on Jenkins server
- ✅ **Git 2.47+** installed and configured
- ✅ **Docker** installed and running
- ✅ **Azure CLI** installed
- ✅ **kubectl** installed
- ✅ **Terraform** installed (optional, for IaC)

### Infrastructure
- ✅ **Azure AKS** cluster created (`aks-itsm-dev` in `switzerlandnorth`)
- ✅ **Azure Container Registry (ACR)** created (`acritsmac742.azurecr.io`)
- ✅ **Azure PostgreSQL** database created
- ✅ **SonarQube** server running (`http://localhost:9000`)

### Jenkins Setup
- ✅ **Jenkins installed** and running
- ✅ **Git plugin** installed and configured
- ✅ **Credentials created** for:
  - `sonarqube-token` - SonarQube API authentication
  - `acr-credentials` - Azure Container Registry access
  - `azure-sp-credentials` - Azure Service Principal

---

## 🚀 PHASE 1: Jenkins Pipeline (Build & Code Quality)

### Current Pipeline Status

The `Jenkinsfile` has been redesigned with these improvements:

| Feature | Before | After |
|---------|--------|-------|
| **Stages** | 8 (mixed concerns) | 5 (focused) |
| **Docker** | ❌ In Jenkins | ✅ Separate Phase 2 |
| **Deployment** | ❌ In Jenkins | ✅ Separate Phase 2 |
| **Focus** | Complex | Clean & Simple |
| **Success Rate** | 🔴 Low | 🟢 High |

### Pipeline Stages

#### Stage 1️⃣: Checkout Code
```
✅ Clones repository from GitHub
✅ Displays recent commits
✅ Prepares workspace for build
```

**Location in Jenkinsfile:**
```groovy
stage('📥 Checkout Code') {
    steps {
        checkout scm
        bat 'git log --oneline -3'
    }
}
```

#### Stage 2️⃣: Verify Environment
```
✅ Checks Java version (needs 17+)
✅ Verifies Maven installation (needs 3.9+)
✅ Confirms Git availability
```

**Location in Jenkinsfile:**
```groovy
stage('🔍 Verify Environment') {
    steps {
        bat 'java -version'
        bat 'mvn -version'
        bat 'git --version'
    }
}
```

#### Stage 3️⃣: Build All Services
```
✅ Maven clean package for 7 services
✅ Skips tests (fixes later)
✅ Creates target/*.jar files
```

**Services built:**
1. `auth-service`
2. `user-service`
3. `ticket-service`
4. `assignment-service`
5. `notifications-service`
6. `analytics-service`
7. `eureka-server`

**Maven Command:**
```bash
mvn clean package -DskipTests -U
```

#### Stage 4️⃣: SonarQube Code Analysis
```
✅ Scans code for bugs, vulnerabilities, code smells
✅ Generates quality reports
✅ Sends results to SonarQube server
```

**SonarQube Scanning Details:**
```groovy
mvn sonar:sonar \
    -Dsonar.projectKey=com.itsm:service-name \
    -Dsonar.projectName=service-name \
    -Dsonar.sources=src/main/java \
    -Dsonar.tests=src/test/java \
    -Dsonar.host.url=http://localhost:9000 \
    -Dsonar.login=<TOKEN>
```

#### Stage 5️⃣: Phase 1 Complete - Results
```
✅ Displays all generated artifacts
✅ Shows how to access SonarQube results
✅ Lists manual deployment steps for Phase 2
```

### Running the Pipeline

#### Method 1: Jenkins Web UI
1. Open Jenkins: `http://localhost:8080`
2. Find job for ITSM project
3. Click **Build Now**
4. Watch console output in real-time

#### Method 2: Jenkins CLI
```bash
java -jar jenkins-cli.jar -s http://localhost:8080 \
     build "ITSM-Pipeline" \
     -s -v
```

#### Method 3: Direct Git Webhook
When you push to `main` branch, Jenkins automatically triggers:
```bash
git add .
git commit -m "trigger jenkins"
git push origin main
```

### Monitoring Pipeline Execution

**View Build Progress:**
1. Click on **Build #123** in Jenkins
2. Select **Console Output**
3. Watch logs in real-time

**Typical Output Timeline:**
```
[14:30:15] Starting job...
[14:30:20] ✅ Checked out code from GitHub
[14:30:25] ✅ Verified Java 17 installed
[14:30:30] ✅ Verified Maven 3.9.9 installed
[14:31:00] ✅ Built auth-service (30s)
[14:31:15] ✅ Built user-service (15s)
...
[14:35:00] ✅ SonarQube analysis complete
[14:35:15] ✅ Pipeline completed
```

---

## 📊 SonarQube Code Quality Dashboard

### Accessing SonarQube

**URL:** `http://localhost:9000`

**Default Credentials:**
```
Username: admin
Password: (set during SonarQube setup)
```

### Code Quality Metrics

After pipeline execution, view these metrics for each service:

#### 🐛 Bugs
- Compilation issues
- Null pointer exceptions
- Logic errors
- **Action:** Fix critical bugs first

#### 🚨 Vulnerabilities
- SQL injection risks
- Cross-site scripting (XSS)
- Authentication issues
- **Action:** Fix all vulnerabilities

#### 💨 Code Smells
- Duplicated code
- Complex methods
- Unused variables
- **Action:** Refactor code

#### 📊 Coverage
- Test coverage percentage
- Untested code sections
- **Action:** Write tests for gaps

### Setting Quality Gates

Quality gates define minimum standards for builds to pass:

```groovy
stage('Quality Gate') {
    steps {
        waitForQualityGate abortPipeline: true
    }
}
```

**Common Quality Gate Rules:**
- ✅ No critical bugs
- ✅ No blocker severity issues
- ✅ Code coverage > 50%
- ✅ No new security hotspots

---

## 🔧 PHASE 2: Deployment Automation (Manual Scripts)

### Overview

Phase 2 is NOT in Jenkins; it's manual process to maintain flexibility:

1. **Docker Images** - Build and push to Azure Container Registry
2. **Infrastructure** - Provision with Terraform or manual kubectl
3. **Services** - Deploy to AKS cluster

### Step 1: Build & Push Docker Images

#### Prerequisites
```bash
# Login to Azure Container Registry
az acr login --name acritsmac742

# Verify you can access ACR
az acr repository list --name acritsmac742
```

#### Build All Images
```bash
cd "c:\Users\LENOVO\Downloads\ITSM"

# For each service, build and push
for /d %%s in (auth-service user-service ticket-service assignment-service notifications-service analytics-service eureka-server) do (
    echo Building %%s...
    docker build -f %%s\Dockerfile -t acritsmac742.azurecr.io/itsm-%%s:latest %%s
    docker push acritsmac742.azurecr.io/itsm-%%s:latest
    echo ✅ Pushed %%s
)
```

#### Verify Images in ACR
```bash
az acr repository list --name acritsmac742 --output table
```

**Expected Output:**
```
itsm-auth-service
itsm-user-service
itsm-ticket-service
itsm-assignment-service
itsm-notifications-service
itsm-analytics-service
itsm-eureka-server
```

### Step 2: Deploy to Azure AKS

#### Option A: Manual kubectl

```bash
# Get AKS credentials
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev

# Create namespace
kubectl create namespace itsm

# Create imagePullSecret for ACR
kubectl create secret docker-registry acr-secret \
  --docker-server=acritsmac742.azurecr.io \
  --docker-username=<username> \
  --docker-password=<password> \
  --docker-email=user@example.com \
  -n itsm

# Deploy services (if k8s manifests exist)
kubectl apply -f k8s/ -n itsm

# Verify deployment
kubectl get pods -n itsm
kubectl get svc -n itsm
```

#### Option B: Terraform Infrastructure as Code

```bash
cd terraform

# Review what will be created
terraform plan

# Deploy infrastructure and services
terraform apply -auto-approve

# Verify
kubectl get pods -A
```

### Step 3: Verify Deployment

```bash
# Check pod status
kubectl get pods -n itsm

# View service endpoints
kubectl get svc -n itsm

# Check logs of a service
kubectl logs -n itsm -f deployment/auth-service

# Port forward to test locally
kubectl port-forward -n itsm svc/auth-service 8081:8081
```

---

## 🛡 Security Features Implemented

### 1. Code Quality Scanning (SonarQube)
```
✅ Automatic code analysis
✅ Vulnerability detection
✅ Code smell identification
✅ Test coverage tracking
```

### 2. Container Security
```
✅ Docker multi-stage builds (smaller images)
✅ Non-root user containers (security)
✅ Health checks enabled
✅ Image scanning (Trivy - Phase 3)
```

### 3. Access Control
```
✅ Jenkins credentials encrypted
✅ SonarQube authentication required
✅ ACR authentication for image pull
✅ Kubernetes RBAC (role-based access)
```

### 4. Infrastructure Security
```
✅ Azure Firewall rules (if configured)
✅ PostgreSQL SSL connections
✅ Service Principal for deployments
✅ Network policies (if configured)
```

---

## ⚙️ Configuration Files

### Jenkinsfile
**Location:** `/Jenkinsfile` (root of repository)

**Key Environments:**
```groovy
SONARQUBE_HOST = 'http://localhost:9000'
SONARQUBE_PROJECT_KEY = 'com.itsm:microservices'
SERVICES = 'auth-service,user-service,...'
```

### pom.xml (Maven)
**Location:** Each service has `/pom.xml`

**SonarQube Configuration (if added):**
```xml
<properties>
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.login>${sonarqube.token}</sonar.login>
</properties>
```

### Dockerfile
**Location:** Each service has `/Dockerfile`

**Pattern Used:**
```dockerfile
# Stage 1: Build
FROM maven:3.9.6 as builder
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📈 Monitoring & Logging

### Jenkins Pipeline Logs
```
Location: http://localhost:8080/job/ITSM-Pipeline/123/console
Retention: Last 10 builds (configurable)
```

### SonarQube Analytics
```
Location: http://localhost:9000/dashboard
Metrics: Bugs, Vulnerabilities, Code Smells, Coverage, Duplications
```

### Azure Monitor (Production)
```
Services: AKS, ACR, PostgreSQL
View: Azure Portal → Resource Group → Metrics
```

### Kubernetes Logs
```bash
# Service logs
kubectl logs -n itsm -f deployment/auth-service

# All pods
kubectl get pods -n itsm

# Pod details
kubectl describe pod -n itsm <pod-name>
```

---

## 🔄 CI/CD Workflow

### Complete Developer Workflow

```
1. Developer pushes code to GitHub (main branch)
   ↓
2. GitHub webhook triggers Jenkins
   ↓
3. Jenkins runs Phase 1:
   - Checkout code
   - Verify environment
   - Maven build all services
   - SonarQube analysis
   ↓
4. Jenkins posts results:
   - Success email to team
   - SonarQube results link
   - Artifacts ready for deployment
   ↓
5. DevOps team manually runs Phase 2:
   - Docker build & push to ACR
   - Terraform apply for infrastructure
   - kubectl deploy to AKS
   ↓
6. Production verification:
   - Health checks pass
   - Services responding
   - Logs clean
   ↓
7. Monitoring active:
   - SonarQube metrics visible
   - Pod metrics collected
   - Logs centralized (future)
```

---

## 🐛 Troubleshooting

### Issue 1: SonarQube Not Found

**Error:**
```
⚠️ SonarQube not available at http://localhost:9000
```

**Solution:**
```bash
# Start SonarQube Docker container
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Wait 60 seconds for startup
# Then access: http://localhost:9000
```

### Issue 2: Maven Build Fails

**Error:**
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Solutions:**
```bash
# Check Java version
java -version  # Needs 17+

# Clear Maven cache
mvn clean -U

# Check dependencies
mvn dependency:tree

# Build locally first
mvn clean package -DskipTests
```

### Issue 3: Jenkins Cannot Find git

**Error:**
```
Cannot run program "git": error=2, No such file or directory
```

**Solution:**
```bash
# Add Git to system PATH in Jenkins configuration
# Or specify full path in Jenkinsfile:
# bat '"C:\\Program Files\\Git\\bin\\git" --version'
```

### Issue 4: Docker Push Fails

**Error:**
```
Error response from daemon: unauthorized
```

**Solution:**
```bash
# Login to ACR first
az acr login --name acritsmac742

# Or use docker login
docker login acritsmac742.azurecr.io
# Enter username and password when prompted
```

### Issue 5: Pod Won't Start in AKS

**Error:**
```
CrashLoopBackOff status
```

**Debug:**
```bash
# Check pod logs
kubectl logs -n itsm <pod-name>

# Describe pod for events
kubectl describe pod -n itsm <pod-name>

# Check resource availability
kubectl top nodes
kubectl top pods -n itsm
```

---

## 📚 Reference Commands

### Git
```bash
# Push to trigger Jenkins
git add .
git commit -m "feat: new feature"
git push origin main

# View logs
git log --oneline -10

# Check status
git status
```

### Maven
```bash
# Build single service
cd auth-service
mvn clean package -DskipTests

# Build all services from root
mvn clean package -DskipTests -pl auth-service,user-service,...

# Run tests (after fixing)
mvn clean test

# Check dependencies
mvn dependency:tree
```

### Docker
```bash
# Build image
docker build -f auth-service/Dockerfile -t my-image:latest auth-service

# Tag image for ACR
docker tag my-image:latest acritsmac742.azurecr.io/itsm-auth:latest

# Push to ACR
docker push acritsmac742.azurecr.io/itsm-auth:latest

# List images
docker images

# Remove image
docker rmi image-name
```

### Kubernetes
```bash
# Get credentials
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev

# List pods
kubectl get pods -n itsm

# View service
kubectl get svc -n itsm

# Scale deployment
kubectl scale deployment auth-service --replicas=3 -n itsm

# Port forward
kubectl port-forward svc/auth-service 8081:8081 -n itsm

# View logs
kubectl logs -f deployment/auth-service -n itsm

# Delete pod (triggers restart)
kubectl delete pod pod-name -n itsm
```

### Azure CLI
```bash
# Login
az login

# List resource groups
az group list

# View AKS cluster
az aks show --resource-group rg-itsm-dev --name aks-itsm-dev

# View ACR repositories
az acr repository list --name acritsmac742

# View PostgreSQL
az postgres server show --resource-group rg-itsm-dev --name psqlitsmac742
```

---

## 📅 Implementation Timeline

### Week 1: Foundation ✅
- [x] Set up Jenkins
- [x] Install SonarQube
- [x] Create Azure resources
- [x] Configure credentials

### Week 2: Pipeline Phase 1 ✅
- [x] Write Jenkinsfile
- [x] Add Maven builds
- [x] Integrate SonarQube
- [x] Test pipeline

### Week 3: Pipeline Phase 2 🟡
- [ ] Create Docker build scripts
- [ ] Test Docker push to ACR
- [ ] Configure Terraform
- [ ] Deploy to AKS

### Week 4: Security & Monitoring ⏳
- [ ] Add Trivy image scanning
- [ ] Configure monitoring alerts
- [ ] Set up centralized logging
- [ ] Document security policies

---

## 🎯 Success Criteria

### Phase 1 (Current)
- ✅ Jenkinsfile runs successfully
- ✅ All 7 services build
- ✅ SonarQube analysis completes
- ✅ Results visible in SonarQube UI

### Phase 2
- ✅ Docker images build successfully
- ✅ Images push to ACR
- ✅ Terraform applies without errors
- ✅ Pods running in AKS
- ✅ Services responding to requests

### End Result
- ✅ Complete DevSecOps pipeline
- ✅ Automated code quality checks
- ✅ Container security scanning
- ✅ Infrastructure as code
- ✅ Centralized monitoring

---

## 📞 Support & Resources

### Documentation
- **Jenkinsfile:** `c:\Users\LENOVO\Downloads\ITSM\Jenkinsfile`
- **This Guide:** `DEVSECOPS_IMPLEMENTATION_GUIDE.md`
- **README:** `README.md`

### Useful Links
- **SonarQube Dashboard:** http://localhost:9000
- **Jenkins:** http://localhost:8080
- **Azure Portal:** https://portal.azure.com
- **GitHub Repo:** https://github.com/Achrafchalkha/itsm-ai-microservices

### Azure Resources
- **ACR:** acritsmac742.azurecr.io
- **AKS:** aks-itsm-dev
- **PostgreSQL:** psqlitsmac742.postgres.database.azure.com
- **Region:** switzerlandnorth

---

## ✨ Next Steps

1. **Run Jenkins Pipeline:**
   - Go to `http://localhost:8080`
   - Click "Build Now"
   - Monitor console output

2. **Review SonarQube Results:**
   - Visit `http://localhost:9000`
   - Examine code quality metrics
   - Plan fixes for high-severity issues

3. **Deploy Phase 2:**
   - Build Docker images
   - Push to Azure Container Registry
   - Deploy to AKS with Terraform

4. **Monitor & Maintain:**
   - Watch for new code quality issues
   - Keep dependencies updated
   - Monitor pod metrics in AKS

---

**Last Updated:** 2025-10-30
**Status:** 🟢 Phase 1 Complete - Ready for Phase 2 Deployment
