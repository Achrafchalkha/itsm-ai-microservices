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
    }
}
