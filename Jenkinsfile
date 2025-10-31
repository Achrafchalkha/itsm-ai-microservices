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
                        bat "trivy image --severity HIGH,CRITICAL --exit-code 0 acritsmac742.azurecr.io/itsm-${s}:latest"
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
    }
}
