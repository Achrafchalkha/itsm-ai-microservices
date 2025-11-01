pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
                    svcs.each { s ->
                        dir(s) {
                            bat 'mvn clean package -DskipTests -q'
                        }
                    }
                }
            }
        }
        stage('SonarQube') {
            steps {
                script {
                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
                    withCredentials([string(credentialsId: 'sonarqube-token', variable: 'TKN')]) {
                        svcs.each { s ->
                            dir(s) {
                                bat "mvn sonar:sonar -Dsonar.projectKey=com.itsm:${s} -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%TKN% -q"
                            }
                        }
                    }
                }
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
                    svcs.each { s ->
                        dir(s) {
                            bat "docker build -t acritsmac742.azurecr.io/itsm-${s}:latest ."
                        }
                    }
                }
            }
        }
        stage('Trivy Scan') {
            steps {
                script {
                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
                    svcs.each { s ->
                        bat "C:\\Users\\LENOVO\\Downloads\\trivy_0.67.0_windows-64bit\\trivy.exe image --severity HIGH,CRITICAL --exit-code 0 acritsmac742.azurecr.io/itsm-${s}:latest"
                    }
                }
            }
        }
        stage('Push to ACR') {
            steps {
                script {
                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
                    withCredentials([usernamePassword(credentialsId: 'acr-credentials', usernameVariable: 'ACR_USER', passwordVariable: 'ACR_PASS')]) {
                        bat 'docker login acritsmac742.azurecr.io -u %ACR_USER% -p %ACR_PASS%'
                        svcs.each { s ->
                            bat "docker push acritsmac742.azurecr.io/itsm-${s}:latest"
                        }
                    }
                }
            }
        }
        stage('Cleanup') {
            steps {
                script {
                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
                    svcs.each { s ->
                        bat "docker rmi acritsmac742.azurecr.io/itsm-${s}:latest"
                    }
                    bat 'docker image prune -f'
                }
            }
        }
        stage('Terraform Infrastructure Provisioning') {
            steps {
                script {
                    dir('terraform') {
                        bat 'terraform init'
                        bat 'terraform plan -out=tfplan'
                        bat 'terraform apply -auto-approve tfplan'
                    }
                }
            }
        }
        stage('Deploy to AKS') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'acr-credentials', usernameVariable: 'ACR_USER', passwordVariable: 'ACR_PASS')]) {
                        bat 'az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev --overwrite-existing'
                        bat 'kubectl create secret docker-registry acr-secret --docker-server=acritsmac742.azurecr.io --docker-username=%ACR_USER% --docker-password=%ACR_PASS% --namespace=itsm --dry-run=client -o yaml | kubectl apply -f -'
                        bat 'kubectl apply -f k8s/namespace.yaml'
                        bat 'kubectl apply -f k8s/configmap.yaml'
                        bat 'kubectl apply -f k8s/secret.yaml'
                        bat 'kubectl apply -f k8s/deployments.yaml'
                        bat 'kubectl apply -f k8s/services.yaml'
                        bat 'kubectl get pods -n itsm'
                        bat 'kubectl get svc -n itsm'
                    }
                }
            }
        }
    }
}
