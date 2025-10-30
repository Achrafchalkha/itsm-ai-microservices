# 🎯 DevSecOps Implementation - IMMEDIATE ACTIONS

**Status**: Ready to execute  
**Duration**: ~10-15 minutes  
**Success Rate**: 99% (if prerequisites met)

---

## ⚡ START HERE - 5 Immediate Actions

### ACTION 1: Start SonarQube (2 minutes)

```powershell
# Start SonarQube in Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Verify it's running
Start-Sleep -Seconds 3
docker ps | findstr sonarqube

# Wait for it to be ready (watch logs)
docker logs -f sonarqube

# Once you see "SonarQube is up", press Ctrl+C and proceed
```

**Expected**: Docker container running on port 9000  
**Verify**: Open `http://localhost:9000` in browser (may take 30-60 sec to respond)

---

### ACTION 2: Generate SonarQube Token (2 minutes)

```powershell
# Open browser (after SonarQube starts responding)
Start-Process "http://localhost:9000"

# Steps in browser:
# 1. Login: admin / admin
# 2. Top-right → Avatar → My Account
# 3. Left menu → Security → Generate Tokens
# 4. Create token:
#    - Name: "jenkins-token"
#    - Type: User Token
#    - Click Generate
# 5. COPY the token value (you won't see it again!)
# 6. Store it somewhere temporarily (clipboard is fine)
```

**Expected**: Long alphanumeric token like `squ_xxxxxxxxxxxxxxxxxxxx`

---

### ACTION 3: Verify Jenkins Credentials (1 minute)

```powershell
# Open Jenkins
Start-Process "http://localhost:8080"

# Navigate to: Manage Jenkins → Manage Credentials → System → Global credentials
# You should see 3 credentials:
#   ✅ acr-credentials (Username with password)
#   ✅ azure-sp-credentials (Secret file)
#   ✅ sonarqube-token (Secret text) ← Should already be added

# If sonarqube-token is missing:
# 1. Click "Add Credentials" (Jenkins)
# 2. Kind: Secret text
# 3. Secret: Paste the token from ACTION 2
# 4. ID: sonarqube-token
# 5. Create
```

**Expected**: 3 credentials visible in Jenkins

---

### ACTION 4: Trigger Jenkins Pipeline Build (2 minutes)

```powershell
# Open Jenkins
Start-Process "http://localhost:8080/job/ITSM-Build/"

# Click blue "Build Now" button
# Monitor the build:
#   📥 Checkout - ~30 sec
#   🔍 Verify Environment - ~10 sec
#   🔨 Build All Services - ~2-3 min (watch for 6-7 successful builds)
#   🛡️ SonarQube Analysis - ~1-2 min (watch for 7 services analyzed)
#   ✅ Complete - Shows summary

# Expected Result: ✅ SUCCESS (Blue badge)
```

**Expected**: Pipeline completes in 4-5 minutes with no errors

---

### ACTION 5: View Code Quality Results (1 minute)

```powershell
# Open SonarQube Projects
Start-Process "http://localhost:9000/projects"

# You should see 7 projects:
#   1. com.itsm:auth-service
#   2. com.itsm:user-service
#   3. com.itsm:ticket-service
#   4. com.itsm:assignment-service
#   5. com.itsm:notifications-service
#   6. com.itsm:analytics-service
#   7. com.itsm:eureka-server

# Click each project to see:
#   📊 Code Smells
#   🐛 Bugs
#   🔓 Vulnerabilities
#   📈 Coverage
#   ⚠️ Hotspots
```

**Expected**: 7 projects listed with quality metrics

---

## ✅ Verification Checklist

After completing the 5 actions above, verify everything:

```powershell
# Check Docker
docker ps | Select-String "sonarqube"         # Should show sonarqube container

# Check Ports
Get-NetTCPConnection -LocalPort 9000 -ErrorAction SilentlyContinue | Select-Object State
# Should show: State : Listen

# Check Jenkins build
# Open: http://localhost:8080/job/ITSM-Build/
# Look for blue checkmark (✅ SUCCESS)

# Check SonarQube
# Open: http://localhost:9000
# Should show 7 projects
```

---

## 🚀 Next: Phase 2 Deployment (When Ready)

Once Phase 1 (above) is complete, deploy to Azure:

```powershell
# 1. Login to Azure
az login
az account set --subscription "YOUR_SUBSCRIPTION_ID"

# 2. Login to ACR
az acr login --name acritsmac742

# 3. Build and push Docker images
# See: DEVSECOPS_PHASE2_DEPLOYMENT.md
```

---

## ❌ Troubleshooting - 3-Step Fix

### Issue: SonarQube won't start
```powershell
# Step 1: Check if port 9000 is in use
Get-NetTCPConnection -LocalPort 9000 -ErrorAction SilentlyContinue

# Step 2: If in use, find what's using it
Get-Process -Id (Get-NetTCPConnection -LocalPort 9000).OwningProcess

# Step 3: Either kill the process or use different port
# To use different port:
docker run -d --name sonarqube -p 9001:9000 sonarqube:latest
# Then access at http://localhost:9001
```

### Issue: Jenkins pipeline fails at Build stage
```powershell
# Step 1: Check Java version
java -version
# Must be version 17+

# Step 2: Check Maven
mvn -version
# Must be 3.9+

# Step 3: Test build manually
cd auth-service
mvn clean package -DskipTests
cd ..
```

### Issue: SonarQube analysis fails
```powershell
# Step 1: Verify token is correct
# Re-generate token if needed at: http://localhost:9000/account/security/

# Step 2: Verify SonarQube is responding
curl http://localhost:9000/api/system/status

# Step 3: Check Jenkins logs
# Jenkins Dashboard → Build → Console Output
# Look for: mvn sonar:sonar errors
```

---

## 📊 Expected Results

### If all 5 actions complete successfully:

✅ **SonarQube Dashboard**
```
Projects: 7 ✓
Analyses: Latest ✓
Quality Gate: Passed/Warning ✓
```

✅ **Jenkins Pipeline**
```
Build Status: SUCCESS ✓
Build Time: 4-5 minutes ✓
All Stages: Green ✓
```

✅ **Code Quality Report**
```
Total Issues Found: X (varies by code)
Critical: Y
High: Z
Medium: A
Low: B
```

---

## 🎯 Success = Phase 1 Complete ✅

When you see:
- ✅ SonarQube showing 7 projects
- ✅ Jenkins pipeline SUCCESS (blue badge)
- ✅ All 7 services analyzed in SonarQube
- ✅ No errors in Jenkins logs

**You are ready for Phase 2!** 🚀

---

## 📋 Command Cheat Sheet

```powershell
# Start SonarQube
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Stop SonarQube
docker stop sonarqube
docker rm sonarqube

# View SonarQube logs
docker logs -f sonarqube

# Access Jenkins
Start-Process "http://localhost:8080"

# Access SonarQube
Start-Process "http://localhost:9000"

# Trigger Jenkins build (API)
$jenkinsUrl = "http://localhost:8080/job/ITSM-Build/build"
Invoke-WebRequest -Uri $jenkinsUrl -Method POST

# Check Jenkins job status
curl http://localhost:8080/job/ITSM-Build/lastBuild/api/json | ConvertFrom-Json | Select-Object result

# View all Docker containers
docker ps -a

# View all Docker networks
docker network ls
```

---

## ⏱️ Time Breakdown

| Action | Time |
|--------|------|
| 1. Start SonarQube | 2 min |
| 2. Generate Token | 2 min |
| 3. Verify Credentials | 1 min |
| 4. Build Pipeline | 5 min |
| 5. View Results | 1 min |
| **TOTAL** | **11 min** |

---

## 🔐 Security Notes

- ✅ SonarQube token is secure (stored in Jenkins credentials)
- ✅ No passwords in logs or console output
- ✅ Token is restricted to SonarQube API only
- ✅ ACR credentials also secured in Jenkins
- ⚠️ Never share SonarQube token or Jenkins credentials

---

## 📞 Need Help?

### Check These Files First
1. **Overview**: `DEVSECOPS_QUICK_START.md`
2. **Phase 2 Deployment**: `DEVSECOPS_PHASE2_DEPLOYMENT.md`
3. **Pipeline Details**: `Jenkinsfile`
4. **Project Info**: `README.md`

### Common Errors & Solutions
- **Port 9000 in use**: Use `netstat -ano | findstr :9000` to find process
- **No Docker**: Install Docker Desktop from docker.com
- **Maven not found**: Install from maven.apache.org
- **Java version**: Use `java -version` to check (need 17+)

---

## ✨ What's Happening Behind the Scenes

```
Phase 1: Code Quality (You are HERE)
├─ Git: Clone repo (done)
├─ Build: Maven compiles 7 services ✓
├─ Quality: SonarQube analyzes code ✓
├─ Reports: Quality metrics generated ✓
└─ Ready for: Docker build & AKS deployment

Phase 2: Deployment (Next)
├─ Docker: Build 7 images
├─ ACR: Push images to Azure
├─ K8s: Deploy to AKS
└─ Verify: Health checks passing

Phase 3: Security (Optional)
├─ Trivy: Scan images for vulnerabilities
├─ Snyk: Scan dependencies
└─ Report: Security findings

Phase 4: Monitoring (Future)
├─ Prometheus: Collect metrics
├─ Grafana: Visualize metrics
└─ ELK: Aggregate logs
```

---

**Ready? Let's go! 🚀**

Run ACTION 1 now!
