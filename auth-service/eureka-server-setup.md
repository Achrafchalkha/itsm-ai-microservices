# 🌐 Quick Eureka Server Setup

## 🚀 **Method 1: Create New Spring Boot Project**

### **Step 1: Create Eureka Server Project**
1. Go to https://start.spring.io/
2. Configure:
   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 3.2.0
   - **Group**: com.itsm
   - **Artifact**: eureka-server
   - **Name**: eureka-server
   - **Package name**: com.itsm.eurekaserver
   - **Packaging**: Jar
   - **Java**: 17

3. **Dependencies to add**:
   - Eureka Server
   - Spring Boot DevTools

4. **Download and extract** the project

### **Step 2: Configure Main Application Class**
```java
package com.itsm.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### **Step 3: Configure application.properties**
```properties
# Application Configuration
spring.application.name=eureka-server
server.port=8761

# Eureka Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Security (disable for development)
spring.security.user.name=admin
spring.security.user.password=admin

# Logging
logging.level.com.netflix.eureka=DEBUG
logging.level.com.netflix.discovery=DEBUG
```

### **Step 4: Start Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
```

## 🐳 **Method 2: Docker Eureka Server (Faster)**

### **Create docker-compose-eureka.yml**
```yaml
version: '3.8'
services:
  eureka-server:
    image: springcloud/eureka
    container_name: eureka-server
    ports:
      - "8761:8761"
    environment:
      - EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
      - EUREKA_CLIENT_FETCH_REGISTRY=false
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
```

### **Start with Docker**
```bash
docker-compose -f docker-compose-eureka.yml up -d
```

## 📋 **Method 3: Standalone JAR (Simplest)**

### **Download Pre-built Eureka Server**
```bash
# Download a pre-built Eureka server JAR
curl -L -o eureka-server.jar https://github.com/spring-cloud-samples/eureka/releases/download/v1.0.0/eureka-server-1.0.0.jar

# Run it
java -jar eureka-server.jar
```

## 🔧 **Verification Steps**

### **1. Check if Eureka Server is Running**
```bash
# Check if port 8761 is open
netstat -an | findstr :8761
```

### **2. Access Eureka Dashboard**
- **URL**: http://localhost:8761
- **Expected**: Eureka dashboard with "Instances currently registered with Eureka" section

### **3. Test with Your Auth Service**
1. **Start Eureka Server** (using any method above)
2. **Start your Auth Service**:
   ```bash
   cd Downloads\auth-service\auth-service
   mvn spring-boot:run
   ```
3. **Check Eureka Dashboard** - you should see AUTH-SERVICE listed

## 🎯 **Expected Results**

### **Eureka Dashboard Should Show:**
```
Eureka
Instances currently registered with Eureka
Application         AMIs        Availability Zones    Status
AUTH-SERVICE        n/a (1)     (1)                  UP (1) - localhost:auth-service:8081

General Info
total-avail-memory : 8gb
environment : test
num-of-cpus : 8
current-memory-usage : 134mb (1%)
server-uptime : 2 minutes
registered-replicas : 
unavailable-replicas : 
available-replicas : 
```

### **Auth Service Logs Should Show:**
```
INFO - Registering application AUTH-SERVICE with eureka with status UP
INFO - DiscoveryClient_AUTH-SERVICE/DESKTOP-XXX:auth-service:8081: registering service...
INFO - DiscoveryClient_AUTH-SERVICE/DESKTOP-XXX:auth-service:8081 - registration status: 204
```

## 🚨 **Troubleshooting**

### **If Eureka Server Won't Start:**
1. **Check Java version**: `java -version` (should be 17+)
2. **Check port 8761**: Make sure nothing else is using it
3. **Check firewall**: Ensure port 8761 is not blocked

### **If Auth Service Can't Connect:**
1. **Verify Eureka Server is running**: http://localhost:8761
2. **Check auth service configuration**:
   ```properties
   eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
   ```
3. **Check network connectivity**: `ping localhost`

## 🎯 **Recommended Approach**

**For quick testing**: Use **Method 2 (Docker)** if you have Docker installed
**For development**: Use **Method 1 (Spring Boot project)** for full control
**For immediate testing**: Use **Method 3 (JAR)** if available

Once you have Eureka Server running, your auth service will automatically register and you'll see it in the dashboard! 🚀
