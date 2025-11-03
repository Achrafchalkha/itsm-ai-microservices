@echo off
(
echo pipeline {
echo     agent any
echo     stages {
echo         stage('Build'^) {
echo             steps {
echo                 script {
echo                     def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
echo                     svcs.each { s -^>
echo                         dir(s^) {
echo                             bat 'mvn clean package -DskipTests -q'
echo                         }
echo                     }
echo                 }
echo             }
echo         }
echo         stage('SonarQube'^) {
echo             steps {
echo                 script {
echo                     def svcs = ['auth-service', 'user-service', 'ticket-service', 'assignment-service', 'notifications-service', 'analytics-service', 'eureka-server']
echo                     withCredentials([string(credentialsId: 'sonarqube-token', variable: 'TKN'^)]]^) {
echo                         svcs.each { s -^>
echo                             dir(s^) {
echo                                 bat "mvn sonar:sonar -Dsonar.projectKey=com.itsm:${s} -Dsonar.host.url=http://localhost:9000 -Dsonar.token=%%TKN%% -q"
echo                             }
echo                         }
echo                     }
echo                 }
echo             }
echo         }
echo     }
echo }
) > Jenkinsfile.tmp
move /Y Jenkinsfile.tmp Jenkinsfile
