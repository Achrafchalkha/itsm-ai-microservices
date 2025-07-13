# 🌐 Eureka Server Setup Guide

## 🎯 **What is Eureka?**

Eureka is a service discovery server that allows microservices to:
- **Register themselves** when they start up
- **Discover other services** without hardcoding URLs
- **Load balance** requests across multiple instances
- **Handle failover** automatically

## 🚀 **Option 1: Quick Eureka Server (Recommended)**

### **Create a Simple Eureka Server Project**

1. **Create new Spring Boot project** with these dependencies:
   - Spring Boot Starter Web
   - Eureka Server
   - Spring Boot DevTools

2. **Main Application Class:**
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

3. **application.properties:**
```properties
spring.application.name=eureka-server
server.port=8761

# Eureka Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Security (disable for development)
spring.security.user.name=admin
spring.security.user.password=admin
```

4. **Start the Eureka Server:**
```bash
mvn spring-boot:run
```

## 🐳 **Option 2: Docker Eureka Server**

### **docker-compose.yml for Eureka:**
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
```

### **Start with Docker:**
```bash
docker-compose up -d eureka-server
```

## 📋 **URLs to Check**

### **🌐 Eureka Dashboard**
- **URL**: http://localhost:8761
- **Description**: Web interface showing all registered services
- **What you'll see**: List of registered microservices

### **🔍 Service Registry API**
- **URL**: http://localhost:8761/eureka/apps
- **Description**: REST API showing registered services in XML format
- **What you'll see**: XML with service details

### **📊 JSON Service Registry**
- **URL**: http://localhost:8761/eureka/apps/AUTH-SERVICE
- **Description**: Details about your auth-service specifically
- **What you'll see**: Service instances, health status, metadata

### **🎯 Your Auth Service via Eureka**
- **URL**: http://localhost:8761/eureka/apps/auth-service
- **Description**: Your auth service registration details
- **What you'll see**: Instance information, status, endpoints

## 🔧 **Testing the Setup**

### **Step 1: Start Eureka Server**
```bash
# If using standalone project
mvn spring-boot:run

# If using Docker
docker-compose up -d eureka-server
```

### **Step 2: Update Auth Service Dependencies**
Make sure your auth service has the Eureka client dependency and configuration.

### **Step 3: Start Auth Service**
```bash
# In your auth-service directory
mvn clean install
mvn spring-boot:run
```

### **Step 4: Verify Registration**
1. Open http://localhost:8761
2. Look for "AUTH-SERVICE" in the "Instances currently registered with Eureka" section
3. You should see:
   - **Application**: AUTH-SERVICE
   - **Status**: UP (1) - localhost:auth-service:8081

## 📊 **Expected Eureka Dashboard**

When everything is working, you'll see:

```
Instances currently registered with Eureka
Application         AMIs        Availability Zones    Status
AUTH-SERVICE        n/a (1)     (1)                  UP (1) - localhost:auth-service:8081
```

## 🔍 **Service Discovery URLs**

### **Access Auth Service via Eureka:**
- **Direct**: http://localhost:8081/auth/register
- **Via Service Name**: http://AUTH-SERVICE/auth/register (from other services)

### **Health Check:**
- **URL**: http://localhost:8081/actuator/health
- **Via Eureka**: http://AUTH-SERVICE/actuator/health

## 🛠️ **Troubleshooting**

### **Auth Service Not Appearing in Eureka**
1. Check Eureka server is running on port 8761
2. Verify auth service configuration:
   ```properties
   eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
   ```
3. Check auth service logs for Eureka registration messages

### **Connection Refused to Eureka**
1. Ensure Eureka server is started first
2. Check port 8761 is not blocked by firewall
3. Verify Eureka server configuration

### **Service Shows as DOWN**
1. Check auth service health endpoint: http://localhost:8081/actuator/health
2. Verify database connection
3. Check application logs for errors

## 🎯 **Benefits of Using Eureka**

### **For Your Auth Service:**
- ✅ **Service Discovery**: Other services can find auth-service by name
- ✅ **Load Balancing**: Multiple auth-service instances can be load balanced
- ✅ **Health Monitoring**: Eureka monitors service health
- ✅ **Failover**: Automatic failover if one instance goes down

### **For Microservices Architecture:**
- ✅ **Dynamic Configuration**: No hardcoded service URLs
- ✅ **Scalability**: Easy to add/remove service instances
- ✅ **Monitoring**: Central view of all services
- ✅ **Circuit Breaker**: Integration with resilience patterns

## 📝 **Next Steps**

1. **Start Eureka Server** (port 8761)
2. **Start Auth Service** (port 8081)
3. **Check Eureka Dashboard** (http://localhost:8761)
4. **Verify Service Registration**
5. **Test Service Discovery** from other microservices

Your auth service will now be discoverable by other microservices in your ecosystem! 🚀
