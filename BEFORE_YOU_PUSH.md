# BEFORE YOU PUSH - Step By Step Instructions

## THE CORRECT ORDER

---

## STEP 1: Get AZURE_CREDENTIALS (Service Principal JSON)
**Time: 2 minutes**
**Where: PowerShell Terminal**

Open PowerShell and run this command:

```powershell
az ad sp create-for-rbac `
  --name "github-actions-sp" `
  --role "Contributor" `
  --scopes "/subscriptions/339e2872-26be-4ffb-b15e-e85a3e5e4aed"
```

**You will get output like:**
```
{
  "appId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "displayName": "github-actions-sp",
  "password": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "tenant": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

**Copy and format it like this** (for GitHub Secret):
```json
{
  "clientId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "clientSecret": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "subscriptionId": "339e2872-26be-4ffb-b15e-e85a3e5e4aed",
  "tenantId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

✅ **COPY THIS TO NOTEPAD - You'll need it in Step 3**

---

## STEP 2: Get ACR Credentials (Username & Password)
**Time: 1 minute**
**Where: PowerShell Terminal**

Run this command:

```powershell
az acr credential show `
  --resource-group rg-itsm-dev `
  --name acritsmac742
```

**You will get output like:**
```
{
  "passwords": [
    {
      "name": "password",
      "value": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    },
    {
      "name": "password2",
      "value": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    }
  ],
  "username": "acritsmac742"
}
```

**COPY these two values:**
- Username: `acritsmac742`
- Password: First value from "password" field

✅ **COPY BOTH TO NOTEPAD - You'll need them in Step 3**

---

## STEP 3: Add Secrets to GitHub Settings
**Time: 5 minutes**
**Where: GitHub Website**

### 3A. Go to Settings

1. Open: **https://github.com/Achrafchalkha/itsm-ai-microservices**
2. Click: **Settings** (top menu bar, right side)

### 3B. Navigate to Secrets

1. In left sidebar, click: **Secrets and variables**
2. Click: **Actions**

### 3C. Add Secret #1 - AZURE_CREDENTIALS

1. Click: **New repository secret** (green button)
2. **Name field**: Type exactly:
   ```
   AZURE_CREDENTIALS
   ```
3. **Value field**: Paste the formatted JSON from Step 1
4. Click: **Add secret**

✅ **Secret #1 added**

### 3D. Add Secret #2 - AZURE_REGISTRY_USERNAME

1. Click: **New repository secret** (green button)
2. **Name field**: Type exactly:
   ```
   AZURE_REGISTRY_USERNAME
   ```
3. **Value field**: Paste the username from Step 2:
   ```
   acritsmac742
   ```
4. Click: **Add secret**

✅ **Secret #2 added**

### 3E. Add Secret #3 - AZURE_REGISTRY_PASSWORD

1. Click: **New repository secret** (green button)
2. **Name field**: Type exactly:
   ```
   AZURE_REGISTRY_PASSWORD
   ```
3. **Value field**: Paste the password from Step 2
4. Click: **Add secret**

✅ **Secret #3 added**

### 3F. Verify All Secrets Are There

Go back to: **Settings** → **Secrets and variables** → **Actions**

You should see:
- ✅ AZURE_CREDENTIALS
- ✅ AZURE_REGISTRY_USERNAME
- ✅ AZURE_REGISTRY_PASSWORD

All showing with asterisks `●●●●●●` (values are hidden)

---

## STEP 4: Check Your Code Locally
**Time: 2 minutes**
**Where: PowerShell in VS Code**

Run this command to see what files changed:

```bash
git status
```

You should see files like:
- `.github/workflows/ci-cd-pipeline.yml`
- `GITHUB_ACTIONS_GUIDE.md`
- `GITHUB_FIRST_STEPS.md`
- etc.

These are all the new files you created.

---

## STEP 5: Commit Your Changes
**Time: 1 minute**
**Where: PowerShell Terminal**

Run these commands one by one:

```bash
# Add all changes
git add .

# Commit with message
git commit -m "feat: Add GitHub Actions CI/CD pipeline with documentation"

# Verify commit was created
git log --oneline -1
```

You should see output showing your commit was created.

---

## STEP 6: Push to GitHub
**Time: 2 minutes**
**Where: PowerShell Terminal**

Run this command:

```bash
git push origin main
```

**Success looks like:**
```
Enumerating objects: 10, done.
Counting objects: 100% (10/10), done.
Delta compression: 50% (5/5), done.
Total 7 (delta 1), reused 0 (delta 0)
To https://github.com/Achrafchalkha/itsm-ai-microservices.git
   89ca3fe..04e7dd6  main -> main
```

✅ **Code pushed to GitHub!**

---

## STEP 7: Watch the Workflow Run
**Time: 15-20 minutes**
**Where: GitHub Website**

1. Go to: **https://github.com/Achrafchalkha/itsm-ai-microservices/actions**
2. Click: On the workflow that's currently running
3. Watch the steps:
   - **Build & Test** (should be running)
   - **Docker Build** (will start after build succeeds)
   - **Deploy to AKS** (will start after docker succeeds)
   - **Security Scan** (runs in parallel)

### Status will show:
- 🟡 **In Progress** - Workflow is running
- ✅ **Success** - All steps completed
- ❌ **Failed** - Something went wrong (check logs)

---

## Summary - Complete Checklist

### Before Pushing:
- [ ] **Step 1**: Get AZURE_CREDENTIALS from Azure CLI
- [ ] **Step 2**: Get ACR credentials from Azure CLI
- [ ] **Step 3**: Add all 3 secrets to GitHub Settings
- [ ] **Step 3F**: Verify all 3 secrets appear in GitHub
- [ ] **Step 4**: Run `git status` to see changes
- [ ] **Step 5**: Run `git add .` and `git commit`
- [ ] **Step 6**: Run `git push origin main`

### After Pushing:
- [ ] **Step 7**: Watch workflow run in Actions tab
- [ ] Verify it completes successfully
- [ ] Check if Docker images pushed to ACR
- [ ] Check if pods deployed to AKS

---

## Common Issues & Quick Fixes

### Issue: "Command not found: az"
**Fix**: Azure CLI not installed. Install it first: https://learn.microsoft.com/cli/azure/install-azure-cli

### Issue: "Please login first"
**Fix**: Run `az login` before other az commands

### Issue: "Secret not found error in workflow"
**Fix**: You didn't add secrets to GitHub before pushing. Add them now and re-run workflow.

### Issue: "Name is already in use"
**Fix**: Service principal name already exists. Use a different name or delete the old one first.

### Issue: Workflow says "Cannot authenticate"
**Fix**: Double-check the secret values are correct. They might be truncated or have extra spaces.

---

## EXACT COMMANDS TO RUN (Copy & Paste)

### In PowerShell:

```powershell
# Step 1
az ad sp create-for-rbac --name "github-actions-sp" --role "Contributor" --scopes "/subscriptions/339e2872-26be-4ffb-b15e-e85a3e5e4aed"

# Step 2
az acr credential show --resource-group rg-itsm-dev --name acritsmac742

# Step 4
git status

# Step 5
git add .
git commit -m "feat: Add GitHub Actions CI/CD pipeline with documentation"
git log --oneline -1

# Step 6
git push origin main
```

---

## Time Breakdown

| Step | Time | Status |
|------|------|--------|
| 1. Get AZURE_CREDENTIALS | 2 min | ⏳ TODO |
| 2. Get ACR Credentials | 1 min | ⏳ TODO |
| 3. Add to GitHub Settings | 5 min | ⏳ TODO |
| 4. Check Code Locally | 2 min | ⏳ TODO |
| 5. Commit Changes | 1 min | ⏳ TODO |
| 6. Push to GitHub | 2 min | ⏳ TODO |
| 7. Watch Workflow | 15-20 min | ⏳ TODO |
| **TOTAL** | **~30 minutes** | |

---

## FINAL CHECKLIST

Before you push, answer these questions:

- ✅ Do I have Azure CLI installed? (`az --version`)
- ✅ Am I logged into Azure? (`az account show`)
- ✅ Did I get AZURE_CREDENTIALS JSON? (Copied to notepad)
- ✅ Did I get ACR username? (Copied to notepad)
- ✅ Did I get ACR password? (Copied to notepad)
- ✅ Are all 3 secrets in GitHub? (Can see them in Settings)
- ✅ Did I commit my changes? (`git log --oneline` shows my commit)
- ✅ Did I push to main? (No error on `git push`)

If all answers are YES, you're ready! ✅

---

## Next: After It All Works

Once workflow completes successfully:

1. Check Docker images pushed to ACR
   ```bash
   az acr repository list --name acritsmac742
   ```

2. Check pods deployed to AKS
   ```bash
   kubectl get pods -n itsm
   ```

3. Check deployment status
   ```bash
   kubectl get deployments -n itsm
   ```

4. View service logs
   ```bash
   kubectl logs -n itsm -l app=auth-service --tail=50
   ```

---

**STATUS**: 🔴 Not Started  
**ACTION**: Start with Step 1  
**QUESTION**: Do you have all 3 secrets ready?
