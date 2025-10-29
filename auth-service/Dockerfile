# Multi-stage build for auth-service
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Build the application
COPY src src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.PropertiesLauncher

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
