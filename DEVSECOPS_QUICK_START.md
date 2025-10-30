# 🚀 ITSM DevSecOps - Quick Start Guide

Complete end-to-end DevSecOps pipeline setup for 7 Spring Boot microservices.

---

## 📋 Your DevSecOps Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEVOPS PIPELINE FLOW                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  GitHub Repo → Jenkins → Build → SonarQube → Docker → ACR → AKS
│     (Main)     (CI/CD)   Maven  (Quality)   (Images) (Push) (K8s)
│                                                                 │
│  Phase 1: CODE QUALITY (Jenkins)                               │
│  Phase 2: DEPLOYMENT (Terraform/Docker/kubectl)                │
│  Phase 3: SECURITY SCANNING (Trivy/Snyk)                       │
│  Phase 4: MONITORING (Prometheus/Grafana) - TBD                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ Phase 1: Code Quality & Build (Jenkins)

### Prerequisites
- ✅ Java 17+: `java -version`
- ✅ Maven 3.9+: `mvn -version`
- ✅ Jenkins running: `http://localhost:8080`
- ✅ SonarQube running: `http://localhost:9000`
- ✅ Git repository cloned locally

### Start SonarQube (First Time Only)

```powershell
# Run SonarQube in Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Wait 2-3 minutes for startup
# Access: http://localhost:9000
# Login: admin / admin (then change password)
```

### Generate SonarQube Token

```powershell
# 1. Open browser: http://localhost:9000
# 2. Login as admin
# 3. Click avatar → My Account → Security → Generate Tokens
# 4. Create token name: "jenkins-token"
# 5. Copy the token value
```

### Add Credentials to Jenkins

```powershell
# 1. Open Jenkins: http://localhost:8080
# 2. Manage Jenkins → Manage Credentials
# 3. Add these credentials:
#    - sonarqube-token (Secret text) ← Already added
#    - acr-credentials (Username/Password) ← Already added
#    - azure-sp-credentials (Secret file) ← Already added
```

### Run Jenkins Pipeline

```powershell
# 1. Open Jenkins: http://localhost:8080/job/ITSM-Build/
# 2. Click "Build Now"
# 3. Monitor build progress:
#    - Stage 1: Checkout (30 sec)
#    - Stage 2: Verify Environment (10 sec)
#    - Stage 3: Build All Services (2-3 minutes)
#    - Stage 4: SonarQube Analysis (1-2 minutes)
#    - Stage 5: Summary (10 sec)
#
# 4. Expected Result: ✅ SUCCESS
```

### View Code Quality Results

```powershell
# Navigate to SonarQube: http://localhost:9000/projects
# See metrics for each service:
#   - Reliability (bugs)
#   - Security (vulnerabilities)
#   - Maintainability (code smells)
#   - Coverage gaps
```

---

## 🐳 Phase 2: Docker & Deployment

### Build & Push Docker Images to ACR

```powershell
# 1. Login to Azure
az login
az account set --subscription "YOUR_SUBSCRIPTION_ID"

# 2. Login to ACR
az acr login --name acritsmac742

# 3. Build all Docker images
$services = @('auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server')

foreach ($service in $services) {
    Write-Host "🐳 Building $service..."
    docker build -f "$service\Dockerfile" -t acritsmac742.azurecr.io/itsm-$service`:latest $service
    docker push acritsmac742.azurecr.io/itsm-$service`:latest
    Write-Host "✅ Pushed $service"
}

# 4. Verify images in ACR
az acr repository list --name acritsmac742
```

### Deploy to Azure AKS

```powershell
# 1. Get AKS credentials
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev

# 2. Create namespace
kubectl create namespace itsm

# 3. Create image pull secret
kubectl create secret docker-registry acr-secret `
  --docker-server=acritsmac742.azurecr.io `
  --docker-username=USERNAME `
  --docker-password=PASSWORD `
  --docker-email=admin@itsm.local `
  -n itsm

# 4. Deploy services
kubectl apply -f k8s/ -n itsm

# 5. Verify
kubectl get pods -n itsm
kubectl get svc -n itsm
```

---

## 🛡️ Phase 3: Security Scanning (Optional)

### Trivy: Image Vulnerability Scanning

```powershell
# Install Trivy
choco install trivy

# Scan images
trivy image acritsmac742.azurecr.io/itsm-auth-service:latest
trivy image acritsmac742.azurecr.io/itsm-user-service:latest
# ... etc
```

### Snyk: Dependency Vulnerability Scanning

```powershell
# Install Snyk
npm install -g snyk

# Scan each service
cd auth-service
snyk test
cd ..
```

---

## 🧪 Testing Services

### Port Forward to Local Machine

```powershell
# In separate terminals:
kubectl port-forward -n itsm svc/auth-service 8081:8081
kubectl port-forward -n itsm svc/user-service 8082:8082
kubectl port-forward -n itsm svc/eureka-server 8761:8761
```

### Health Checks

```powershell
# Test each service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8761/actuator/health
```

### View Eureka Dashboard

```powershell
# Open browser: http://localhost:8761
# Should show all 7 services registered
```

### Run Postman Tests

```powershell
# Use provided Postman collections:
# - Auth-Service-Postman-Collection.json
# - Admin-Testing-Postman-Collection.json
# - ITSM-UserService-Postman.json
```

---

## 📊 Monitoring & Logs

### View Service Logs

```powershell
# Real-time logs
kubectl logs -n itsm -f deployment/auth-service

# Last 100 lines
kubectl logs -n itsm deployment/auth-service --tail=100

# All pods
kubectl get pods -n itsm
kubectl logs -n itsm deployment/user-service
kubectl logs -n itsm deployment/ticket-service
kubectl logs -n itsm deployment/assignment-service
kubectl logs -n itsm deployment/notifications-service
kubectl logs -n itsm deployment/analytics-service
kubectl logs -n itsm deployment/eureka-server
```

### Check Pod Status

```powershell
# Detailed pod info
kubectl describe pod <POD_NAME> -n itsm

# Events
kubectl get events -n itsm --sort-by='.lastTimestamp'

# Resource usage
kubectl top pods -n itsm
```

---

## 🔧 Service URLs

| Service | Type | URL |
|---------|------|-----|
| **Eureka Server** | Discovery | http://localhost:8761 |
| **Auth Service** | Microservice | http://localhost:8081 |
| **User Service** | Microservice | http://localhost:8082 |
| **Ticket Service** | Microservice | http://localhost:8083 |
| **Assignment Service** | Microservice | http://localhost:8084 |
| **Notifications Service** | Microservice | http://localhost:8085 |
| **Analytics Service** | Microservice | http://localhost:8086 |
| **SonarQube** | Code Quality | http://localhost:9000 |
| **Jenkins** | CI/CD | http://localhost:8080 |

---

## 🔑 Credentials Reference

| Component | Username | Password / Key |
|-----------|----------|-----------------|
| **SonarQube** | admin | (Set during setup) |
| **PostgreSQL** | postgres | Itsm2025Spring |
| **ACR** | acritsmac742 | (Use `az acr credential show`) |
| **Azure** | (Use `az login`) | (Interactive) |

---

## ❌ Troubleshooting

### SonarQube not running?
```powershell
docker start sonarqube
# or
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
```

### Jenkins build failing?
```powershell
# Check Java version
java -version  # Should be 17+

# Check Maven
mvn -version  # Should be 3.9+

# Rebuild with verbose output
cd auth-service
mvn clean package -X
```

### Pod not starting in AKS?
```powershell
kubectl describe pod <POD_NAME> -n itsm
kubectl logs <POD_NAME> -n itsm
# Check image pull secret, resources, etc.
```

### Database connection errors?
```powershell
# Verify PostgreSQL is running
az postgres server show --resource-group rg-itsm-dev --name psqlitsmac742

# Check connection string in pod
kubectl exec -it <POD_NAME> -n itsm -- env | grep DATABASE
```

---

## 📈 Performance Metrics

### Expected Build Times
| Stage | Time |
|-------|------|
| Checkout | ~30 sec |
| Verify Environment | ~10 sec |
| Build 7 Services | ~2-3 min |
| SonarQube Analysis | ~1-2 min |
| **Total** | ~**4-5 min** |

### Expected Deployment Times
| Step | Time |
|------|------|
| Docker Build (7 images) | ~3-5 min |
| Push to ACR | ~1-2 min |
| kubectl deploy | ~2-3 min |
| Pods Ready | ~1-2 min |
| **Total** | ~**7-12 min** |

---

## ✨ Next Steps

1. **✅ Phase 1**: Run Jenkins pipeline → Check SonarQube results
2. **✅ Phase 2**: Build Docker images → Deploy to AKS
3. **🟡 Phase 3**: Run Trivy/Snyk scans → Review security findings
4. **⏳ Phase 4**: Set up Prometheus/Grafana → Monitor metrics

---

## 📚 Complete File Locations

```
ITSM/
├── Jenkinsfile                          ← CI/CD Pipeline (Phase 1)
├── DEVSECOPS_PHASE2_DEPLOYMENT.md       ← Deployment Guide (Phase 2)
├── terraform/                           ← Infrastructure as Code
├── kubernetes/                          ← K8s manifests (if present)
├── docker/                              ← Dockerfiles (7 services)
├── auth-service/
│   ├── pom.xml                          ← Updated with SonarQube plugin
│   ├── Dockerfile
│   └── src/
├── user-service/                        ← 6 more services...
├── ticket-service/
├── assignment-service/
├── notifications-service/
├── analytics-service/
├── eureka-server/
└── ... other files
```

---

## 🎯 Success Checklist

### Phase 1: Code Quality (Jenkins)
- [ ] SonarQube running on localhost:9000
- [ ] Jenkins pipeline created and tested
- [ ] All 7 services build successfully
- [ ] SonarQube analysis complete
- [ ] No critical code quality issues

### Phase 2: Deployment
- [ ] Docker images built locally
- [ ] Images pushed to ACR
- [ ] AKS namespace created
- [ ] All 7 pods running in AKS
- [ ] Services discoverable via Eureka
- [ ] Database connections working
- [ ] Health checks passing

### Phase 3: Security
- [ ] Trivy scan completed
- [ ] No critical vulnerabilities
- [ ] Snyk dependency scan passed
- [ ] Security issues addressed

### Phase 4: Monitoring
- [ ] Prometheus scraping metrics
- [ ] Grafana dashboards displaying data
- [ ] Alerts configured
- [ ] Logs aggregated (ELK stack)

---

## 📞 Support

For issues or questions:
1. Check `DEVSECOPS_PHASE2_DEPLOYMENT.md` for detailed deployment steps
2. Review `README.md` for general project info
3. Check Jenkins/SonarQube logs for build errors
4. Use `kubectl logs` for runtime errors

---

**Last Updated**: $(date)
**Repository**: https://github.com/Achrafchalkha/itsm-ai-microservices
**Region**: switzerlandnorth
**Environment**: Development (dev)
