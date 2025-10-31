# SonarQube Token Setup - Step by Step

## STEP 1: Open SonarQube Web Interface
1. Open browser: http://localhost:9000
2. Login with default credentials:
   - Username: **admin**
   - Password: **admin**

## STEP 2: Generate SonarQube Token
1. Click on your **Avatar** (top right corner) → **My Account**
2. Click on **Security** tab
3. Under "Tokens" section:
   - Token name: `jenkins-token`
   - Type: `User Token`
   - Expires in: `No expiration` (or choose 365 days)
4. Click **Generate**
5. **COPY the token** - it looks like: `squ_123456789abcdef...`
6. Save it somewhere safe - you won't see it again!

## STEP 3: Add Token to Jenkins
1. Open Jenkins: http://localhost:8080
2. Go to: **Manage Jenkins** → **Manage Credentials**
3. Click on **(global)** under "Stores scoped to Jenkins"
4. Click **Add Credentials** (top left)
5. Fill in the form:
   - Kind: **Secret text**
   - Secret: Paste the SonarQube token you copied
   - ID: `sonarqube-token` (IMPORTANT - must match!)
   - Description: `SonarQube Jenkins Token`
6. Click **Create**

## STEP 4: Verify Credential Created
1. In Credentials page, you should see:
   - ID: `sonarqube-token`
   - Kind: `Secret text`
   - Status: ✅ Created

## STEP 5: Test the Pipeline
1. Go to Jenkins job: http://localhost:8080/job/ITSM-Microservices-Pipeline
2. Click **Build Now**
3. Monitor console output - SonarQube analysis should now work!

---

## TROUBLESHOOTING

**Error: "Not authorized. Please check the user token"**
- Verify token is copied correctly (no spaces at start/end)
- Check credential ID is exactly `sonarqube-token`
- Regenerate token if needed

**Error: "Credentials 'sonarqube-token' not found"**
- Make sure credential was created in (global) scope
- Jenkins might need a restart after adding credential
- Check ID spelling matches exactly

**SonarQube connection refused**
- Verify SonarQube is running: `docker ps | grep sonarqube`
- Check it's accessible: http://localhost:9000
- If not running, start it: `docker run -d -p 9000:9000 sonarqube:latest`

---

## QUICK REFERENCE

**SonarQube URL:** http://localhost:9000
- Username: admin
- Password: admin

**Jenkins URL:** http://localhost:8080

**Token Location in SonarQube:** Avatar → My Account → Security → Tokens

**Credential Details for Jenkins:**
- Kind: Secret text
- ID: sonarqube-token
- Description: SonarQube Jenkins Token
