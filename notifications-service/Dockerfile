# Multi-stage build for notifications-service
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/notifications-service-0.0.1-SNAPSHOT.jar app.jar
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 CMD curl -f http://localhost:8085/actuator/health || exit 1
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
