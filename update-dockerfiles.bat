@echo off
REM Create simple Dockerfiles for all services

echo FROM openjdk:17-jdk-slim > auth-service\Dockerfile
echo WORKDIR /app >> auth-service\Dockerfile
echo COPY target/*.jar app.jar >> auth-service\Dockerfile
echo EXPOSE 8081 >> auth-service\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> auth-service\Dockerfile

echo FROM openjdk:17-jdk-slim > user-service\Dockerfile
echo WORKDIR /app >> user-service\Dockerfile
echo COPY target/*.jar app.jar >> user-service\Dockerfile
echo EXPOSE 8082 >> user-service\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> user-service\Dockerfile

echo FROM openjdk:17-jdk-slim > ticket-service\Dockerfile
echo WORKDIR /app >> ticket-service\Dockerfile
echo COPY target/*.jar app.jar >> ticket-service\Dockerfile
echo EXPOSE 8083 >> ticket-service\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> ticket-service\Dockerfile

echo FROM openjdk:17-jdk-slim > assignment-service\Dockerfile
echo WORKDIR /app >> assignment-service\Dockerfile
echo COPY target/*.jar app.jar >> assignment-service\Dockerfile
echo EXPOSE 8084 >> assignment-service\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> assignment-service\Dockerfile

echo FROM openjdk:17-jdk-slim > notifications-service\Dockerfile
echo WORKDIR /app >> notifications-service\Dockerfile
echo COPY target/*.jar app.jar >> notifications-service\Dockerfile
echo EXPOSE 8085 >> notifications-service\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> notifications-service\Dockerfile

echo FROM openjdk:17-jdk-slim > analytics-service\Dockerfile
echo WORKDIR /app >> analytics-service\Dockerfile
echo COPY target/*.jar app.jar >> analytics-service\Dockerfile
echo EXPOSE 8086 >> analytics-service\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> analytics-service\Dockerfile

echo FROM openjdk:17-jdk-slim > eureka-server\Dockerfile
echo WORKDIR /app >> eureka-server\Dockerfile
echo COPY target/*.jar app.jar >> eureka-server\Dockerfile
echo EXPOSE 8761 >> eureka-server\Dockerfile
echo ENTRYPOINT ["java", "-jar", "app.jar"] >> eureka-server\Dockerfile

echo All Dockerfiles updated!
