// Simple Jenkinsfile - NO PLUGINS REQUIRED (except basic Pipeline & Git)
// Works with just Maven, Docker, and Azure CLI installed on Jenkins server

pipeline {
    agent any
    
    environment {
        // Azure Container Registry
        ACR_REGISTRY = 'acritsmac742.azurecr.io'
        
        // Azure Resources
        RESOURCE_GROUP = 'rg-itsm-dev'
        AKS_CLUSTER = 'aks-itsm-dev'
        NAMESPACE = 'itsm'
        SUBSCRIPTION_ID = '339e2872-26be-4ffb-b15e-e85a3e5e4aed'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code from GitHub...'
                checkout scm
                echo '✅ Code checked out successfully!'
                bat 'dir'
            }
        }
        
        stage('Verify Environment') {
            steps {
                echo '🔍 Checking environment...'
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker --version'
                bat 'az --version'
                echo '✅ Environment verified!'
            }
        }
        
        stage('Build All Services') {
            steps {
                script {
                    echo '🔨 Building all microservices with Maven...'
                    
                    def services = ['auth-service', 'user-service', 'ticket-service', 
                                    'assignment-service', 'notifications-service', 
                                    'analytics-service', 'eureka-server']
                    
                    services.each { service ->
                        echo "Building ${service}..."
                        bat """
                            cd ${service}
                            mvn clean package -DskipTests
                            cd ..
                        """
                    }
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    echo '✅ Running tests for all services...'
                    
                    def services = ['auth-service', 'user-service', 'ticket-service', 
                                    'assignment-service', 'notifications-service', 
                                    'analytics-service', 'eureka-server']
                    
                    services.each { service ->
                        echo "Testing ${service}..."
                        bat """
                            cd ${service}
                            mvn test
                            cd ..
                        """
                    }
                }
            }
        }
        
        stage('Docker - Build & Push') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo '🐳 Building and pushing Docker images to ACR...'
                    
                    withCredentials([usernamePassword(credentialsId: 'acr-credentials', 
                                                     usernameVariable: 'ACR_USER', 
                                                     passwordVariable: 'ACR_PASS')]) {
                        // Login to ACR
                        bat "docker login ${ACR_REGISTRY} -u %ACR_USER% -p %ACR_PASS%"
                        
                        def services = ['auth-service', 'user-service', 'ticket-service', 
                                        'assignment-service', 'notifications-service', 
                                        'analytics-service', 'eureka-server']
                        
                        services.each { service ->
                            echo "📦 Building Docker image for ${service}..."
                            bat "docker build -t ${ACR_REGISTRY}/itsm-${service}:latest ${service}"
                            
                            echo "📤 Pushing ${service} to ACR..."
                            bat "docker push ${ACR_REGISTRY}/itsm-${service}:latest"
                        }
                        
                        // Logout
                        bat "docker logout ${ACR_REGISTRY}"
                    }
                }
            }
        }
        
        stage('Deploy to AKS') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo '☸️ Deploying to Azure Kubernetes Service...'
                    
                    withCredentials([usernamePassword(credentialsId: 'azure-sp-credentials', 
                                                     usernameVariable: 'AZURE_CLIENT', 
                                                     passwordVariable: 'AZURE_SECRET')]) {
                        // Login to Azure
                        bat """
                            az login --service-principal ^
                              -u %AZURE_CLIENT% ^
                              -p %AZURE_SECRET% ^
                              --tenant d4d13448-4ef9-411c-bc92-9654e9f5a3f5
                        """
                        
                        bat "az account set --subscription ${SUBSCRIPTION_ID}"
                        
                        // Get AKS credentials
                        bat """
                            az aks get-credentials ^
                              --resource-group ${RESOURCE_GROUP} ^
                              --name ${AKS_CLUSTER} ^
                              --overwrite-existing
                        """
                        
                        // Apply Kubernetes manifests
                        bat "kubectl apply -f k8s/"
                        
                        // Wait for deployments
                        echo "⏳ Waiting for deployments to complete..."
                        bat "kubectl rollout status deployment -n ${NAMESPACE} --timeout=5m"
                        
                        // Show status
                        bat "kubectl get pods -n ${NAMESPACE}"
                        bat "kubectl get svc -n ${NAMESPACE}"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
            echo '🎉 All services built, tested, and deployed!'
        }
        failure {
            echo '❌ Pipeline failed! Check logs above for details.'
        }
        always {
            echo '🧹 Cleaning up...'
        }
    }
}
