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
        stage('Terraform Infrastructure Provisioning') {
            steps {
                script {
                    withCredentials([
                        string(credentialsId: 'azure-client-id', variable: 'CLIENT_ID'),
                        string(credentialsId: 'azure-client-secret', variable: 'CLIENT_SECRET')
                    ]) {
                        dir('terraform') {
                            bat 'C:\\Users\\LENOVO\\AppData\\Local\\Microsoft\\WinGet\\Links\\terraform.exe init'
                            bat 'set TF_VAR_client_id=%CLIENT_ID% && set TF_VAR_client_secret=%CLIENT_SECRET% && C:\\Users\\LENOVO\\AppData\\Local\\Microsoft\\WinGet\\Links\\terraform.exe plan -out=tfplan'
                            bat 'C:\\Users\\LENOVO\\AppData\\Local\\Microsoft\\WinGet\\Links\\terraform.exe apply -auto-approve tfplan'
                        }
                    }
                }
            }
        }
        stage('Deploy to AKS') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'acr-credentials', usernameVariable: 'ACR_USER', passwordVariable: 'ACR_PASS')]) {
                        bat '"C:\\Program Files (x86)\\Microsoft SDKs\\Azure\\CLI2\\wbin\\az.cmd" aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev --overwrite-existing'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" create secret docker-registry acr-secret --docker-server=acritsmac742.azurecr.io --docker-username=%ACR_USER% --docker-password=%ACR_PASS% --namespace=itsm --dry-run=client -o yaml | "C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" apply -f -'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" apply -f k8s/namespace.yaml'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" apply -f k8s/configmap.yaml'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" apply -f k8s/secret.yaml'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" apply -f k8s/deployments.yaml'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" apply -f k8s/services.yaml'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" get pods -n itsm'
                        bat '"C:\\Program Files\\Docker\\Docker\\resources\\bin\\kubectl.exe" get svc -n itsm'
                    }
                }
            }
        }
    }
}
