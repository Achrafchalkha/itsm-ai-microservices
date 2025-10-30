# DevSecOps Phase 2: Docker & Kubernetes Deployment

After Jenkins successfully builds & analyzes code with SonarQube, execute this Phase 2 to deploy to Azure.

---

## ✅ Prerequisites

- ✅ All 7 services built successfully (from Jenkins Phase 1)
- ✅ Docker images exist locally (built during Phase 1)
- ✅ Azure CLI installed: `az --version`
- ✅ kubectl installed: `kubectl version --client`
- ✅ Docker Desktop running
- ✅ ACR credentials available: `acritsmac742.azurecr.io`

---

## 🔄 Phase 2: Deployment Steps

### STEP 1: Build & Push Docker Images to ACR

```powershell
# 1. Login to Azure ACR
az acr login --name acritsmac742

# 2. Build and push all 7 Docker images
$services = @('auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server')

foreach ($service in $services) {
    Write-Host "🐳 Building and pushing $service..."
    docker build -f "$service\Dockerfile" -t acritsmac742.azurecr.io/itsm-$service`:latest $service
    docker push acritsmac742.azurecr.io/itsm-$service`:latest
    Write-Host "✅ Pushed $service"
}
```

### STEP 2: Deploy to Azure AKS

#### Option A: Manual Deployment with kubectl

```powershell
# 1. Get AKS credentials
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev

# 2. Create ITSM namespace
kubectl create namespace itsm

# 3. Create image pull secret for ACR
$acr_username = "acritsmac742"  # Replace with actual username
$acr_password = "PASSWORD"       # Replace with actual ACR password

kubectl create secret docker-registry acr-secret `
  --docker-server=acritsmac742.azurecr.io `
  --docker-username=$acr_username `
  --docker-password=$acr_password `
  --docker-email=admin@itsm.local `
  -n itsm

# 4. Deploy services using kubectl
kubectl apply -f k8s/ -n itsm

# 5. Verify deployment
kubectl get pods -n itsm
kubectl get svc -n itsm
```

#### Option B: Terraform Deployment (Infrastructure as Code)

```powershell
# Navigate to Terraform directory
cd terraform

# Plan deployment
terraform plan -out=tfplan

# Apply deployment
terraform apply tfplan

# Verify deployment
terraform output
```

---

## 📊 Verification

### Check Deployment Status

```powershell
# Get all pods
kubectl get pods -n itsm -w

# Get all services
kubectl get svc -n itsm

# Check service details
kubectl describe svc auth-service -n itsm

# View pod logs
kubectl logs -n itsm deployment/auth-service
```

### Port Forward to Local Machine

```powershell
# Forward auth-service
kubectl port-forward -n itsm svc/auth-service 8081:8081

# Forward user-service
kubectl port-forward -n itsm svc/user-service 8082:8082

# Forward eureka-server
kubectl port-forward -n itsm svc/eureka-server 8761:8761
```

### Test Services

```powershell
# Health check
curl http://localhost:8081/actuator/health

# Eureka discovery
curl http://localhost:8761

# View logs in real-time
kubectl logs -n itsm -f deployment/auth-service
```

---

## 🔑 Important Credentials & URLs

### Azure Resources
| Resource | URL/Value |
|----------|-----------|
| ACR | acritsmac742.azurecr.io |
| AKS Cluster | aks-itsm-dev (rg-itsm-dev) |
| PostgreSQL | psqlitsmac742.postgres.database.azure.com |
| DB Password | Itsm2025Spring |

### Service Endpoints (Local)
| Service | Port | URL |
|---------|------|-----|
| Eureka Server | 8761 | http://localhost:8761 |
| Auth Service | 8081 | http://localhost:8081 |
| User Service | 8082 | http://localhost:8082 |
| Ticket Service | 8083 | http://localhost:8083 |
| Assignment Service | 8084 | http://localhost:8084 |
| Notifications Service | 8085 | http://localhost:8085 |
| Analytics Service | 8086 | http://localhost:8086 |

### Code Quality
| Tool | URL |
|------|-----|
| SonarQube | http://localhost:9000 |

---

## 🛡️ Phase 3: Security Scanning (Optional - After Deployment)

### Trivy: Scan Docker Images for Vulnerabilities

```powershell
# Install Trivy
choco install trivy

# Scan each Docker image
$services = @('auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server')

foreach ($service in $services) {
    trivy image "acritsmac742.azurecr.io/itsm-$service`:latest"
}
```

### Snyk: Scan Dependencies

```powershell
# Install Snyk
npm install -g snyk

# Authenticate
snyk auth

# Test each service
foreach ($service in $services) {
    cd $service
    snyk test
    cd ..
}
```

---

## ❌ Troubleshooting

### Pod not starting?
```powershell
kubectl describe pod <POD_NAME> -n itsm
kubectl logs <POD_NAME> -n itsm
```

### Image pull errors?
```powershell
# Verify image exists in ACR
az acr repository list --name acritsmac742

# Check image pull secret
kubectl get secrets -n itsm
```

### Network issues?
```powershell
# Test connectivity between pods
kubectl exec -it <POD_NAME> -n itsm -- bash
curl http://service-name:8080
```

### Scale deployment
```powershell
kubectl scale deployment auth-service --replicas=3 -n itsm
```

---

## 🎯 Success Criteria

- ✅ All Docker images pushed to ACR
- ✅ All 7 pods running in AKS namespace
- ✅ Services discoverable via Eureka
- ✅ Database connections working
- ✅ Health checks passing
- ✅ No critical security vulnerabilities (Trivy)

---

## 📚 Related Documentation

- **Phase 1**: Code Quality & Build - See `Jenkinsfile`
- **Infrastructure**: Terraform code in `terraform/` directory
- **API Testing**: Postman collections in root directory
- **DevSecOps Overview**: See `README.md`
