# 🚀 Quick Command Reference - Stage 1 Pipeline

## 🎯 Test the Pipeline Now

### **Option 1: Use Jenkins UI**
```
1. http://localhost:8080/job/ITSM-Build/
2. Click "Build Now" button
3. Wait 4-6 minutes
```

### **Option 2: Use curl**
```powershell
curl -X POST http://localhost:8080/job/ITSM-Build/build
```

### **Option 3: View Live Console**
```
http://localhost:8080/job/ITSM-Build/lastBuild/console
```

---

## 📊 After Build Completes

### **View Code Quality**
```
http://localhost:9000/projects
```

### **Check Artifacts**
```powershell
# All 7 JAR files should exist
Get-ChildItem -Recurse -Filter "*-SNAPSHOT.jar"
```

### **View Build Status**
```powershell
cd c:\Users\LENOVO\Downloads\ITSM
git log --oneline -1  # Should show latest commit
```

---

## 🔍 If Something Fails

### **Check SonarQube Running**
```powershell
docker ps --filter "name=sonarqube"
```

### **Restart SonarQube**
```powershell
docker restart sonarqube
# Wait 2-3 minutes for startup
```

### **Test Single Service Build**
```powershell
cd c:\Users\LENOVO\Downloads\ITSM\auth-service
mvn clean package -DskipTests
```

### **View Jenkins Logs**
```
http://localhost:8080/job/ITSM-Build/lastBuild/console
```

---

## 📈 Monitor Progress

### **While Building**
```
Jenkins Console: http://localhost:8080/job/ITSM-Build/lastBuild/console
Expected stages:
  - Stage 1: Checkout (1 min)
  - Stage 2: Build (2-3 min)
  - Stage 3: SonarQube (1-2 min)
  - Stage 4: Summary (30 sec)
```

### **After Completion**
```
✅ SUCCESS - Should see all 7 services built
http://localhost:9000 - View quality metrics
```

---

## 🔧 Useful Commands

### **Check Status**
```powershell
# Java version
java -version

# Maven version
mvn -version

# Git status
git status

# Docker containers
docker ps
```

### **Clean Up**
```powershell
# Clear Maven cache (if needed)
rm -r ~/.m2/repository

# Remove old builds
rm -r target/
```

### **Verify Files**
```powershell
# Check Jenkinsfile exists
Test-Path Jenkinsfile

# Check pom.xml has SonarQube plugin
Select-String "sonar-maven-plugin" auth-service/pom.xml

# Check JAR created
Test-Path eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
```

---

## 🎯 The 3-Step Process

### **1. Trigger Build** (10 sec)
```
Open: http://localhost:8080/job/ITSM-Build/
Click: "Build Now"
```

### **2. Wait for Completion** (4-6 min)
```
Watch: Console output updates
Look for: ✅ SUCCESS at the end
```

### **3. View Results** (1 min)
```
Open: http://localhost:9000
See: 7 projects with metrics
```

---

## 📝 What to Write Down

After build completes, note:

```
Build Status: ✅ SUCCESS or ❌ FAILED

Services Built: 7/7
Services Analyzed: 7/7

SonarQube Projects: 7
  - com.itsm:auth-service
  - com.itsm:user-service
  - com.itsm:ticket-service
  - com.itsm:assignment-service
  - com.itsm:notifications-service
  - com.itsm:analytics-service
  - com.itsm:eureka-server

Build Time: ___ minutes
```

---

## 🎊 Success Indicators ✅

If you see these, Stage 1 works:

- ✅ Jenkins build badge is **BLUE**
- ✅ Console shows: "✅ SUCCESS"
- ✅ 7 services listed as "BUILT ✓"
- ✅ SonarQube shows 7 projects
- ✅ No red errors in output

---

## ⏭️ Next: Phase 2

When Stage 1 succeeds:

```
Phase 2: Docker & Kubernetes
- Build Docker images
- Push to ACR
- Deploy to AKS
- Verify services

See: DEVSECOPS_PHASE2_DEPLOYMENT.md
```

---

**Ready? → Start the pipeline now!** 🚀
