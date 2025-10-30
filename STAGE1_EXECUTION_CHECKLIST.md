# ✅ Stage 1 Pipeline - Execution Checklist

**Goal**: Verify everything is ready, then run the pipeline  
**Expected Duration**: 4-6 minutes to complete  
**Status**: Ready to execute

---

## 📋 Pre-Execution Checklist (5 minutes)

### **Environment Verification**

- [ ] **Java 17 installed**
  ```powershell
  java -version
  # Expected: openjdk version "17.x.x"
  ```

- [ ] **Maven 3.9 installed**
  ```powershell
  mvn -version
  # Expected: Apache Maven 3.9.x
  ```

- [ ] **Git installed and working**
  ```powershell
  git --version
  git log --oneline -1
  # Expected: Latest commit shown
  ```

- [ ] **SonarQube container running**
  ```powershell
  docker ps | findstr sonarqube
  curl http://localhost:9000
  # Expected: SonarQube page loads
  ```

- [ ] **SonarQube accessible**
  ```
  Browser: http://localhost:9000
  Expected: SonarQube login page
  ```

### **Jenkins Configuration**

- [ ] **Jenkins running**
  ```
  URL: http://localhost:8080
  Expected: Jenkins dashboard loads
  ```

- [ ] **ITSM-Build job exists**
  ```
  Dashboard → ITSM-Build
  Expected: Job page loads with "Build Now" button
  ```

- [ ] **sonarqube-token credential added**
  ```
  Jenkins → Manage Jenkins → Manage Credentials
  System → Global credentials
  Look for: sonarqube-token (Secret text)
  Expected: Credential listed
  ```

- [ ] **acr-credentials configured**
  ```
  Same location as above
  Look for: acr-credentials (Username with password)
  Expected: Credential listed
  ```

- [ ] **azure-sp-credentials configured**
  ```
  Same location as above
  Look for: azure-sp-credentials (Secret file)
  Expected: Credential listed
  ```

### **Repository Status**

- [ ] **Git repository cloned**
  ```powershell
  git status
  # Expected: On branch main
  ```

- [ ] **Latest code pulled**
  ```powershell
  git log --oneline -1
  # Expected: Recent commit shown
  ```

- [ ] **Jenkinsfile present**
  ```powershell
  Test-Path "Jenkinsfile"
  # Expected: True
  ```

- [ ] **All 7 pom.xml files updated**
  ```powershell
  Select-String "sonar-maven-plugin" auth-service/pom.xml
  # Expected: Plugin found
  ```

- [ ] **All 7 Dockerfiles present**
  ```powershell
  Get-ChildItem -Recurse -Filter "Dockerfile" | Measure-Object
  # Expected: 7 Dockerfiles found
  ```

---

## 🚀 Execution Steps

### **Step 1: Open Jenkins** (30 sec)
```
1. Open browser: http://localhost:8080
2. Look for: Dashboard with jobs listed
3. Find: ITSM-Build job
```

### **Step 2: Navigate to Job** (30 sec)
```
1. Click on: ITSM-Build
2. Expected: Job page opens
3. Look for: "Build Now" button (blue)
```

### **Step 3: Start Build** (10 sec)
```
1. Click: "Build Now" button
2. Expected: Build #N starts
3. New line appears: "Started by user..."
```

### **Step 4: Watch Build Progress** (5 minutes)
```
1. Click: Latest build (#N)
2. View: Console Output
3. Watch stages complete:
   - Stage 1: Checkout & Verify (1 min)
   - Stage 2: Build & Test (2-3 min)
   - Stage 3: SonarQube (1-2 min)
   - Stage 4: Summary (30 sec)
```

### **Step 5: Verify Success** (1 min)
```
1. Build badge: Should be BLUE (✅ SUCCESS)
2. Console: Should end with "✅ SUCCESS"
3. No red errors
4. All 7 services listed as built
```

### **Step 6: View Results** (1 min)
```
1. Open SonarQube: http://localhost:9000
2. Go to: Projects
3. Expected: 7 projects listed
4. Click each to see metrics
```

---

## 🎯 What to Expect During Build

### **Stage 1: Checkout & Verify** (1 minute)
```
╔════════════════════════════════════════════════════════════════════╗
║         📥 STAGE 1: CHECKOUT & VERIFY ENVIRONMENT                 ║
╚════════════════════════════════════════════════════════════════════╝
  ✅ Repository checked out from GitHub
  [git log output showing 5 recent commits]
  ════════════════════════════════════════════════════════════════════
  Java Version:
  ════════════════════════════════════════════════════════════════════
  openjdk version "17.0.12" 2024-07-16
  
  Maven Version:
  Apache Maven 3.9.9
```

### **Stage 2: Build & Test** (2-3 minutes)
```
╔════════════════════════════════════════════════════════════════════╗
║        🔨 STAGE 2: BUILD & TEST ALL 7 SERVICES WITH MAVEN         ║
╚════════════════════════════════════════════════════════════════════╝
  ┌─────────────────────────────────────────────────────────
  │ 📦 Building auth-service...
  └─────────────────────────────────────────────────────────
  [Maven output...]
  ✅ SUCCESS: auth-service built and JAR created
  
  ┌─────────────────────────────────────────────────────────
  │ 📦 Building user-service...
  └─────────────────────────────────────────────────────────
  [Maven output...]
  ✅ SUCCESS: user-service built and JAR created
  
  [... 5 more services ...]
  
  ════════════════════════════════════════════════════════════════════
  📊 BUILD SUMMARY
  ════════════════════════════════════════════════════════════════════
    ✅ Successful builds: 7/7
    ❌ Failed builds: 0/7
    ✅ ALL 7 SERVICES BUILT SUCCESSFULLY!
```

### **Stage 3: SonarQube Analysis** (1-2 minutes)
```
╔════════════════════════════════════════════════════════════════════╗
║      🛡️ STAGE 3: SONARQUBE CODE QUALITY ANALYSIS                  ║
╚════════════════════════════════════════════════════════════════════╝
  ┌─────────────────────────────────────────────────────────
  │ ▶️ Analyzing auth-service...
  └─────────────────────────────────────────────────────────
  [Maven sonar:sonar output...]
  ✅ SUCCESS: auth-service analysis sent to SonarQube
  
  ┌─────────────────────────────────────────────────────────
  │ ▶️ Analyzing user-service...
  └─────────────────────────────────────────────────────────
  [Maven sonar:sonar output...]
  ✅ SUCCESS: user-service analysis sent to SonarQube
  
  [... 5 more services ...]
  
  ════════════════════════════════════════════════════════════════════
  📊 ANALYSIS SUMMARY
  ════════════════════════════════════════════════════════════════════
    ✅ Successful analyses: 7/7
    ❌ Failed analyses: 0/7
```

### **Stage 4: Results & Summary** (30 seconds)
```
╔════════════════════════════════════════════════════════════════════╗
║         ✅ STAGE 1: BUILD & TEST COMPLETE - SUCCESS!              ║
╚════════════════════════════════════════════════════════════════════╝

📋 PIPELINE EXECUTED SUCCESSFULLY:

✅ Stage 1: Checkout & Verify
✅ Stage 2: Build & Test (Maven)
   ├─ auth-service .................. BUILT ✓
   ├─ user-service .................. BUILT ✓
   ├─ ticket-service ................ BUILT ✓
   ├─ assignment-service ............ BUILT ✓
   ├─ notifications-service ......... BUILT ✓
   ├─ analytics-service ............ BUILT ✓
   └─ eureka-server ................ BUILT ✓

✅ Stage 3: SonarQube Analysis
   └─ 7 projects analyzed

🔗 SonarQube Dashboard: http://localhost:9000/projects
```

---

## ✅ Success Indicators

The build was **SUCCESSFUL** if you see:

- ✅ Build badge is **BLUE** (not red)
- ✅ Console ends with: "✅ SUCCESS"
- ✅ All 7 services show: "BUILT ✓"
- ✅ All 7 services analyzed in SonarQube
- ✅ SonarQube shows 7 projects
- ✅ No error messages in console
- ✅ 7 JAR files created in target/ directories

---

## ❌ If Build Fails

### **Quick Diagnostics**

| Error | Cause | Fix |
|-------|-------|-----|
| "java: command not found" | Java not installed | Install Java 17 |
| "mvn: command not found" | Maven not installed | Install Maven 3.9 |
| Build ERROR | Compilation failed | Check pom.xml, run locally |
| "Connection refused 9000" | SonarQube not running | `docker run -d -p 9000:9000 sonarqube:latest` |
| "Invalid credentials" | Token expired | Regenerate at http://localhost:9000 |

### **Debug Single Service**

```powershell
# If a service fails, test it locally:
cd auth-service
mvn clean package -DskipTests -X  # -X for debug output

# If SonarQube fails:
cd auth-service
mvn sonar:sonar -Dsonar.projectKey=test -X
```

---

## 📊 Performance Benchmarks

| Metric | Expected | Notes |
|--------|----------|-------|
| Stage 1 Time | 1 min | Checkout + verify |
| Stage 2 Time | 2-3 min | 7 × Maven build |
| Stage 3 Time | 1-2 min | 7 × SonarQube scan |
| Stage 4 Time | 30 sec | Results reporting |
| **Total Time** | **4-6 min** | First run may take longer |

---

## 🎯 After Successful Build

When build completes with ✅ SUCCESS:

### **Step 1: Check Artifacts**
```powershell
# Verify 7 JARs created
Get-ChildItem -Recurse -Filter "*-SNAPSHOT.jar" | Measure-Object
# Expected: 7 files

# Show JAR locations
Get-ChildItem -Recurse -Filter "*-SNAPSHOT.jar"
```

### **Step 2: Check SonarQube**
```
1. Open: http://localhost:9000
2. Go to: Projects
3. You should see: 7 projects
   - com.itsm:auth-service
   - com.itsm:user-service
   - com.itsm:ticket-service
   - com.itsm:assignment-service
   - com.itsm:notifications-service
   - com.itsm:analytics-service
   - com.itsm:eureka-server
```

### **Step 3: Review Code Quality**
```
For each project:
1. Click project name
2. Check: Reliability (bugs)
3. Check: Security (vulnerabilities)
4. Check: Maintainability (code smells)
5. Check: Coverage (test coverage)
```

### **Step 4: Document Results**
```powershell
# Save screenshots or notes of:
- Build completion time
- Number of bugs found
- Number of vulnerabilities
- Code coverage percentage
- Any critical issues to fix
```

---

## 🚀 Next: Phase 2 Preparation

After Stage 1 succeeds, prepare for Phase 2:

```
Phase 2: Docker & Kubernetes Deployment
├─ Build Docker images (7 services)
├─ Push to ACR (Azure Container Registry)
├─ Deploy to AKS (Azure Kubernetes Service)
└─ Verify services running

📖 Instructions: DEVSECOPS_PHASE2_DEPLOYMENT.md
```

---

## 📞 Help Resources

| Need | Document |
|------|----------|
| Quick overview | STAGE1_QUICK_REFERENCE.md |
| Detailed explanation | STAGE1_PIPELINE_CREATED.md |
| First 5 actions | DEVSECOPS_IMMEDIATE_ACTIONS.md |
| All phases guide | DEVSECOPS_QUICK_START.md |
| Phase 2 deployment | DEVSECOPS_PHASE2_DEPLOYMENT.md |

---

## 🎯 Checklist Summary

**Before Running:**
- [ ] Java 17 verified
- [ ] Maven 3.9 verified
- [ ] SonarQube running
- [ ] Jenkins accessible
- [ ] Credentials configured
- [ ] Repository updated

**During Running:**
- [ ] Watch console output
- [ ] Monitor stage progress
- [ ] Note any warnings

**After Running:**
- [ ] Check build badge (BLUE)
- [ ] Verify 7 services built
- [ ] Check SonarQube projects
- [ ] Review code quality metrics

---

## 🎉 You're Ready!

Everything is set up and ready to go.

**Next Action**: Click "Build Now" in Jenkins

**Expected Result**: ✅ SUCCESS in 4-6 minutes

**Good luck! 🚀**
