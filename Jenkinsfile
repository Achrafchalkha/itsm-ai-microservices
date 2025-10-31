pipeline {pipeline {

    agent any    agent any

    stages {    stages {

        stage('Build') {        stage('Build') {

            steps {            steps {

                script {                script {

                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']

                    svcs.each { s ->                    svcs.each { s ->

                        dir(s) {                        dir(s) {

                            bat 'mvn clean package -DskipTests -q'                            bat 'mvn clean package -DskipTests -q'

                        }                        }

                    }                    }

                }                }

            }            }

        }        }

        stage('SonarQube') {        stage('SonarQube') {

            steps {            steps {

                script {                script {

                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']                    def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']

                    withCredentials([string(credentialsId: 'sonarqube-token', variable: 'TKN')]) {                    withCredentials([string(credentialsId: 'sonarqube-token', variable: 'TKN')]) {

                        svcs.each { s ->                        svcs.each { s ->

                            dir(s) {                            dir(s) {

                                bat "mvn sonar:sonar -Dsonar.projectKey=com.itsm:${s} -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%TKN% -q"                                bat 'mvn sonar:sonar -Dsonar.projectKey=com.itsm:%s% -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%TKN% -q'

                            }                            }

                        }                        }

                    }                    }

                }                }

            }            }

        }        }

    }    }

}}

