# 🚀 Stage 1 Pipeline - Quick Reference Card

## ⚡ 30-Second Overview

```
Git Repo
   ↓
   │ 📥 Checkout & Verify (Java, Maven)
   ↓
   │ 🔨 Build & Test (mvn clean package × 7 services)
   ↓
   │ 🛡️ SonarQube Analysis (Code quality check)
   ↓
   │ 📊 Results & Summary
   ↓
Jenkins SUCCESS ✅
   ↓
Ready for Phase 2 Deployment
```

**Time: 4-6 minutes | Services: 7 | Artifacts: 7 JAR files**

---

## 📋 Stage 1 Pipeline Stages

### **Stage 1: Checkout & Verify** (1 min)
- Git clone repository
- Verify Java 17+
- Verify Maven 3.9+
- Status: Ready to build

### **Stage 2: Build & Test** (2-3 min)
- Maven clean package on all 7 services
- Skip tests (for speed)
- Create 7 JAR artifacts
- Track pass/fail per service

### **Stage 3: SonarQube Analysis** (1-2 min)
- Scan code quality for each service
- Find bugs, vulnerabilities, code smells
- Generate quality metrics
- Upload to SonarQube dashboard

### **Stage 4: Results & Summary** (30 sec)
- Show build status
- List created artifacts
- Provide SonarQube link
- Show Phase 2 next steps

---

## ✅ Prerequisites Checklist

Before running the pipeline, verify:

```powershell
# ✅ Java 17?
java -version
# Expected: openjdk version "17.x.x"

# ✅ Maven 3.9?
mvn -version
# Expected: Apache Maven 3.9.x

# ✅ Git working?
git log --oneline -1
# Expected: Latest commit shown

# ✅ SonarQube running?
curl http://localhost:9000
# Expected: HTTP 200 OK

# ✅ Jenkins credential added?
# Jenkins → Manage Credentials → Global Credentials
# Look for: sonarqube-token (Secret text)
```

---

## 🎯 How to Run

### **Option 1: Jenkins UI (Recommended)**
```
1. Open: http://localhost:8080
2. Go to: ITSM-Build job
3. Click: "Build Now" button
4. Watch: Console output updates in real-time
5. Result: Shows in 4-6 minutes
```

### **Option 2: Jenkins API**
```powershell
curl -X POST http://localhost:8080/job/ITSM-Build/build
```

### **Option 3: Command Line (Groovy)**
```groovy
// Inside Jenkins Script Console
Build.create('ITSM-Build')
```

---

## 📊 What Happens at Each Step

### **Stage 2: Build**
```
Building auth-service ........ ✅ BUILT
Building user-service ........ ✅ BUILT
Building ticket-service ...... ✅ BUILT
Building assignment-service .. ✅ BUILT
Building notifications-service ✅ BUILT
Building analytics-service ... ✅ BUILT
Building eureka-server ....... ✅ BUILT

BUILD SUMMARY: 7/7 successful ✅
```

### **Stage 3: SonarQube**
```
Analyzing auth-service ........ ✅ SENT
Analyzing user-service ........ ✅ SENT
Analyzing ticket-service ...... ✅ SENT
Analyzing assignment-service .. ✅ SENT
Analyzing notifications-service ✅ SENT
Analyzing analytics-service ... ✅ SENT
Analyzing eureka-server ....... ✅ SENT

ANALYSIS SUMMARY: 7/7 successful ✅
```

---

## 🔗 Important URLs

| URL | Purpose |
|-----|---------|
| http://localhost:8080/job/ITSM-Build/ | Pipeline Job |
| http://localhost:8080/job/ITSM-Build/lastBuild/console | Build Console |
| http://localhost:9000/projects | SonarQube Results |
| http://localhost:9000 | SonarQube Admin |

---

## 📦 Artifacts Created

After successful build, 7 JAR files are created:

```
auth-service/target/auth-service-0.0.1-SNAPSHOT.jar
user-service/target/user-service-0.0.1-SNAPSHOT.jar
ticket-service/target/ticket-service-0.0.1-SNAPSHOT.jar
assignment-service/target/assignment-service-0.0.1-SNAPSHOT.jar
notifications-service/target/notifications-service-0.0.1-SNAPSHOT.jar
analytics-service/target/analytics-service-0.0.1-SNAPSHOT.jar
eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
```

These are used in Phase 2 for Docker image building.

---

## ❌ If Build Fails

### **Error: "Java version not found"**
```powershell
# Solution:
java -version  # Check version (need 17+)
# Install from: https://adoptium.net
```

### **Error: "Maven command not found"**
```powershell
# Solution:
mvn -version  # Check version (need 3.9+)
# Install from: https://maven.apache.org
```

### **Error: "SonarQube connection refused"**
```powershell
# Solution:
docker run -d -p 9000:9000 sonarqube:latest
docker logs sonarqube  # Wait for startup
# Access: http://localhost:9000
```

### **Error: "sonarqube-token not found"**
```
Solution:
1. Go to: http://localhost:9000
2. Login as admin
3. Avatar → My Account → Security
4. Generate new token
5. Add to Jenkins: Manage Credentials
6. ID: sonarqube-token
```

---

## 📈 Performance Tips

| Optimization | Effect |
|--------------|--------|
| Skip tests | -30 sec (already done) |
| Parallel builds | -1 min (if 4-core CPU) |
| Skip javadoc | -10 sec (already done) |
| Local Maven cache | -30 sec (first time only) |

Current pipeline: **Optimized for 4-6 minutes**

---

## 🎯 Success Criteria

✅ Pipeline is successful when:

- [ ] All 7 services build without errors
- [ ] All 7 JAR files created
- [ ] SonarQube receives all 7 projects
- [ ] No red flags in console
- [ ] Build status: ✅ SUCCESS

---

## 🚀 Next: Phase 2

When Stage 1 completes successfully, next step is Phase 2:

**Phase 2: Docker & Kubernetes Deployment**
- Build Docker images (7 services)
- Push to Azure ACR
- Deploy to Azure AKS
- Verify services running

📖 Instructions: See `DEVSECOPS_PHASE2_DEPLOYMENT.md`

---

## 📞 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| Build too slow | Clear Maven cache: `rm -rf ~/.m2` |
| Port 9000 in use | Kill process or use: `-p 9001:9000` |
| No artifacts found | Check `target/` directory exists |
| SonarQube shows no projects | Wait 2-3 minutes after build |
| Token expired | Regenerate at SonarQube: Admin → Security |

---

## 📊 Pipeline Timeline

```
00:00 - Pipeline starts
00:30 - Checkout & Verify complete
01:30 - Build stage starts
03:30 - Build complete (7 JARs)
04:30 - SonarQube analysis complete
05:00 - Summary displayed
05:30 - Pipeline SUCCESS ✅
```

**Expected duration: 4-6 minutes**

---

## 🎓 Pipeline Architecture

```
Jenkins Agent
├── Git Clone (GitHub)
├── Maven 3.9
│   ├── Clean
│   ├── Compile
│   ├── Package
│   └── → 7 JARs
├── SonarQube Client
│   ├── Scan Code
│   ├── Find Issues
│   └── → SonarQube Server
└── Reporting

Output:
├── Build Status (SUCCESS/FAILURE)
├── 7 JAR Artifacts
├── 7 SonarQube Projects
└── Quality Metrics
```

---

## ✨ What's New in This Pipeline

| Feature | Status |
|---------|--------|
| Per-service tracking | ✅ NEW |
| Better error messages | ✅ NEW |
| Build success counter | ✅ NEW |
| Analysis success counter | ✅ NEW |
| Phase 2 next steps | ✅ NEW |
| Detailed output | ✅ NEW |
| Timeout protection | ✅ NEW |
| Build history limit | ✅ NEW |

---

## 🎯 Remember

- **Stage 1**: Build + Test + SonarQube (This Pipeline)
- **Stage 2**: Docker + ACR + AKS (Manual)
- **Stage 3**: Security Scanning (Trivy/Snyk)
- **Stage 4**: Monitoring (Prometheus/Grafana)

You are at: **Stage 1 ✅**

---

**Ready? → Run the pipeline now! 🚀**
