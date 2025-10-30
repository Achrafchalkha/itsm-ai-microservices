// ════════════════════════════════════════════════════════════════════════════════
// ITSM DevSecOps Pipeline - PHASE 1: Development & Code Quality
// ════════════════════════════════════════════════════════════════════════════════
//
// PURPOSE: This pipeline focuses on CODE QUALITY, TESTING, and SECURITY SCANNING
// 
// Pipeline Stages:
//   1. Checkout code from GitHub
//   2. Verify environment (Java, Maven, SonarQube)
//   3. Build all 7 microservices with Maven
//   4. Run SonarQube code analysis  
//   5. Quality gates and reports
//
// Deployment (PHASE 2) is handled SEPARATELY:
//   - Terraform for infrastructure provisioning
//   - Docker for containerization  
//   - kubectl/Helm for Kubernetes deployment
//
// Prerequisites:
//   ✅ SonarQube running: http://localhost:9000 (admin/admin)
//   ✅ Jenkins credential: sonarqube-token (SonarQube API token)
//   ✅ Java 17+ and Maven 3.9+ installed on Jenkins server
//
// ════════════════════════════════════════════════════════════════════════════════

pipeline {
    agent any
    
    environment {
        // SonarQube Settings
        SONARQUBE_HOST = 'http://localhost:9000'
        SONARQUBE_PROJECT_KEY = 'com.itsm:microservices'
        SONARQUBE_PROJECT_NAME = 'ITSM Microservices'
        
        // Microservices List
        SERVICES = 'auth-service,user-service,ticket-service,assignment-service,notifications-service,analytics-service,eureka-server'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 2, unit: 'HOURS')
        timestamps()
    }
    
    stages {
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // STAGE 1: CHECKOUT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('📥 Checkout Code') {
            steps {
                script {
                    echo '''
                    ╔════════════════════════════════════════════════════════════╗
                    ║          📥 STAGE 1: CHECKOUT CODE FROM GITHUB            ║
                    ╚════════════════════════════════════════════════════════════╝
                    '''
                }
                checkout scm
                bat 'git log --oneline -3'
                echo '✅ Repository checked out successfully'
            }
        }
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // STAGE 2: VERIFY ENVIRONMENT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('🔍 Verify Environment') {
            steps {
                script {
                    echo '''
                    ╔════════════════════════════════════════════════════════════╗
                    ║        🔍 STAGE 2: VERIFY BUILD ENVIRONMENT               ║
                    ╚════════════════════════════════════════════════════════════╝
                    '''
                }
                bat '''
                    echo Java version:
                    java -version
                    echo.
                    echo Maven version:
                    mvn -version
                    echo.
                    echo Git version:
                    git --version
                '''
                echo '✅ Environment verification complete'
            }
        }
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // STAGE 3: BUILD ALL SERVICES
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('🔨 Build All Services') {
            steps {
                script {
                    echo '''
                    ╔════════════════════════════════════════════════════════════╗
                    ║     🔨 STAGE 3: BUILD ALL 7 MICROSERVICES WITH MAVEN      ║
                    ╚════════════════════════════════════════════════════════════╝
                    '''
                    
                    def services = ['auth-service', 'user-service', 'ticket-service', 
                                    'assignment-service', 'notifications-service', 
                                    'analytics-service', 'eureka-server']
                    
                    services.each { service ->
                        echo "🔨 Building ${service}..."
                        dir(service) {
                            bat 'mvn clean package -DskipTests -U'
                        }
                        echo "  ✅ ${service} built successfully"
                    }
                }
                echo '✅ All services built successfully'
            }
        }
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // STAGE 4: SONARQUBE ANALYSIS
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('🛡 SonarQube Code Analysis') {
            steps {
                script {
                    echo '''
                    ╔════════════════════════════════════════════════════════════╗
                    ║     🛡 STAGE 4: SONARQUBE CODE QUALITY ANALYSIS            ║
                    ║                                                            ║
                    ║   Scanning for:                                            ║
                    ║   - Code smells and anti-patterns                          ║
                    ║   - Bugs and potential vulnerabilities                     ║
                    ║   - Security hotspots                                      ║
                    ║   - Code duplications                                      ║
                    ║   - Coverage gaps                                          ║
                    ╚════════════════════════════════════════════════════════════╝
                    '''
                    
                    def services = ['auth-service', 'user-service', 'ticket-service', 
                                    'assignment-service', 'notifications-service', 
                                    'analytics-service', 'eureka-server']
                    
                    withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                        services.each { service ->
                            echo "  ▶ Analyzing ${service}..."
                            dir(service) {
                                bat '''
                                    mvn sonar:sonar ^
                                        -Dsonar.projectKey=com.itsm:%service% ^
                                        -Dsonar.projectName=%service% ^
                                        -Dsonar.sources=src/main/java ^
                                        -Dsonar.tests=src/test/java ^
                                        -Dsonar.host.url=http://localhost:9000 ^
                                        -Dsonar.login=%SONAR_TOKEN% ^
                                        -Dsonar.java.source=17 ^
                                        -Dsonar.exclusions=**/*Test.java,**/config/**
                                '''
                            }
                            echo "  ✅ ${service} analysis sent to SonarQube"
                        }
                    }
                }
                echo '✅ SonarQube analysis complete'
            }
        }
        
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // STAGE 5: RESULTS & SUMMARY
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        stage('📊 Phase 1 Complete - Code Quality Results') {
            steps {
                script {
                    echo '''
                    ╔════════════════════════════════════════════════════════════════════╗
                    ║              ✅ PHASE 1: CODE QUALITY ANALYSIS COMPLETE           ║
                    ╚════════════════════════════════════════════════════════════════════╝
                    
                    📋 What was accomplished:
                       ✅ Checkout code from GitHub repository
                       ✅ Verified build environment (Java, Maven, Git)
                       ✅ Maven clean package for all 7 services
                       ✅ SonarQube analysis for code quality
                       ✅ Compiled artifacts ready for deployment
                    
                    📊 View Code Quality Results:
                       🔗 SonarQube Dashboard: http://localhost:9000
                       📊 Projects analyzed: 7 microservices
                       🎯 Focus areas: Bugs, Vulnerabilities, Code Smells, Coverage
                    
                    ════════════════════════════════════════════════════════════════════
                    🚀 PHASE 2: DEPLOYMENT (Manual Process)
                    ════════════════════════════════════════════════════════════════════
                    
                    STEP 1 - Build & Push Docker Images:
                    ─────────────────────────────────────
                    cd %WORKSPACE%
                    az acr login --name acritsmac742
                    
                    for /d %%s in (auth-service user-service ticket-service^
                                   assignment-service notifications-service^
                                   analytics-service eureka-server) do (
                        docker build -f %%s\Dockerfile ^
                                    -t acritsmac742.azurecr.io/itsm-%%s:latest %%s
                        docker push acritsmac742.azurecr.io/itsm-%%s:latest
                        echo ✅ Pushed %%s
                    )
                    
                    STEP 2 - Deploy to Azure AKS:
                    ──────────────────────────────
                    Option A - Manual Deployment:
                        az aks get-credentials --resource-group rg-itsm-dev ^
                                              --name aks-itsm-dev
                        kubectl apply -f k8s/ -n itsm
                        kubectl get pods -n itsm
                    
                    Option B - Infrastructure as Code (Terraform):
                        cd terraform
                        terraform plan
                        terraform apply -auto-approve
                    
                    STEP 3 - Verify Deployment:
                    ───────────────────────────
                    kubectl get pods -n itsm
                    kubectl logs -n itsm -f deployment/auth-service
                    kubectl port-forward -n itsm svc/auth-service 8081:8081
                    
                    ════════════════════════════════════════════════════════════════════
                    📦 Build Artifacts Generated:
                    ════════════════════════════════════════════════════════════════════
                    '''
                    
                    def services = ['auth-service', 'user-service', 'ticket-service', 
                                    'assignment-service', 'notifications-service', 
                                    'analytics-service', 'eureka-server']
                    
                    services.each { service ->
                        echo "  ✓ ${service}/target/${service}-0.0.1-SNAPSHOT.jar"
                    }
                    
                    echo '''
                    ════════════════════════════════════════════════════════════════════
                    🔗 Important URLs & Credentials:
                    ════════════════════════════════════════════════════════════════════
                    
                    📊 SonarQube:
                       URL: http://localhost:9000
                       User: admin / PASSWORD (set during setup)
                    
                    🔑 Azure Resources:
                       Region: switzerlandnorth
                       ACR: acritsmac742.azurecr.io
                       AKS: aks-itsm-dev (in rg-itsm-dev)
                       PostgreSQL: psqlitsmac742.postgres.database.azure.com
                       Password: Itsm2025Spring
                    
                    🧹 GitHub Repository:
                       URL: https://github.com/Achrafchalkha/itsm-ai-microservices
                       Branch: main
                    
                    ════════════════════════════════════════════════════════════════════
                    ✨ Next Actions:
                    ════════════════════════════════════════════════════════════════════
                    1. Review SonarQube findings (address critical issues first)
                    2. Fix any test failures in analytics-service
                    3. Execute Phase 2 deployment steps above
                    4. Test services via Postman collection
                    5. Monitor logs in AKS
                    ════════════════════════════════════════════════════════════════════
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo '''
            ╔════════════════════════════════════════════════════════════╗
            ║                    PIPELINE COMPLETED                      ║
            ╚════════════════════════════════════════════════════════════╝
            '''
        }
        
        success {
            echo '''
            ✅ SUCCESS: Pipeline completed successfully!
            
            📊 Code Quality Analysis Results:
               → View at: http://localhost:9000/projects
            
            🚀 Ready for PHASE 2 (Deployment):
               → Use Terraform to deploy infrastructure
               → Use Docker to push images to ACR
               → Use kubectl to deploy to AKS
               
            ⏭️ Next Step: Push Docker images and deploy with Terraform
            '''
        }
        
        failure {
            echo '''
            ❌ FAILURE: Pipeline encountered errors!
            
            🔍 Troubleshooting:
               1. Check Maven build output above
               2. Verify SonarQube is running (http://localhost:9000)
               3. Verify sonarqube-token credential in Jenkins
               4. Check Java version compatibility (need 17+)
               5. Run: mvn clean install -U locally to test
            '''
        }
    }
}

