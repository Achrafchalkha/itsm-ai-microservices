# 🎯 ITSM Microservices Platform

> Enterprise-grade IT Service Management system built with Spring Boot microservices, deployed on Azure Kubernetes Service (AKS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.33-blue.svg)](https://kubernetes.io/)
[![Azure](https://img.shields.io/badge/Azure-AKS-0078D4.svg)](https://azure.microsoft.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Getting Started](#getting-started)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [CI/CD Pipeline](#cicd-pipeline)
- [Contributing](#contributing)

---

## 🌟 Overview

A cloud-native ITSM platform demonstrating enterprise microservices architecture with event-driven communication, deployed on Azure Kubernetes Service. Features include JWT authentication, real-time notifications via WebSocket, comprehensive monitoring, and full DevSecOps pipeline.

### Key Features

✅ **7 Spring Boot Microservices** with independent scaling  
✅ **Event-Driven Architecture** using Apache Kafka  
✅ **Service Discovery** with Netflix Eureka  
✅ **Real-time Notifications** via WebSocket  
✅ **JWT Authentication** with Spring Security  
✅ **Database per Service** pattern (6 PostgreSQL databases)  
✅ **Azure Cloud Deployment** (AKS, ACR, PostgreSQL Flexible Server)  
✅ **Complete Monitoring** with Prometheus & Grafana  
✅ **DevSecOps Pipeline** (Jenkins, SonarQube, Trivy, Terraform)  

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    AZURE CLOUD (AKS)                        │
├─────────────────────────────────────────────────────────────┤
│  Load Balancer → Eureka Server → Microservices             │
│                                                             │
│  Services: Auth | User | Ticket | Assignment |             │
│            Analytics | Notifications | Eureka              │
│                          ↓                                  │
│            Apache Kafka (Event Bus)                         │
│                          ↓                                  │
│  PostgreSQL Flexible Server (6 databases)                   │
│                                                             │
│  Monitoring: Prometheus + Grafana                           │
└─────────────────────────────────────────────────────────────┘
```

**Architecture Highlights:**
- **Microservices Pattern**: Independent, loosely-coupled services
- **Event-Driven Communication**: Kafka for asynchronous messaging
- **Database per Service**: Each service owns its data
- **Service Discovery**: Eureka for dynamic service registration
- **API Gateway**: Load balancing and routing
- **Circuit Breaker**: Resilience patterns implemented

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Modern Java features
- **Spring Boot 3.2** - Microservices framework
- **Spring Cloud 2023.0.0** - Cloud-native tools
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - ORM with Hibernate
- **Lombok** - Boilerplate reduction

### Messaging & Events
- **Apache Kafka** - Event streaming platform
- **WebSocket** - Real-time bidirectional communication

### Database
- **PostgreSQL 14** - Relational database
- **Azure PostgreSQL Flexible Server** - Managed database service

### Cloud & Infrastructure
- **Azure Kubernetes Service (AKS)** - Container orchestration
- **Azure Container Registry (ACR)** - Docker image registry
- **Azure Virtual Network** - Network isolation
- **Terraform** - Infrastructure as Code

### Monitoring & Observability
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization
- **Spring Boot Actuator** - Application metrics
- **Micrometer** - Metrics instrumentation

### DevSecOps
- **Jenkins** - CI/CD automation
- **SonarQube** - Code quality analysis
- **OWASP Dependency Check** - Vulnerability scanning
- **Trivy** - Container security scanning
- **Maven** - Build automation
- **Docker** - Containerization

---

## 🔧 Microservices

### 1. **Eureka Server** (Port 8761)
- Service discovery and registration
- Health monitoring of all services
- Load balancing coordination

### 2. **Auth Service** (Port 8080)
- JWT token generation and validation
- User authentication
- Role-based access control (RBAC)
- Database: `auth_db`

### 3. **User Service** (Port 8081)
- User profile management
- Team and competence management
- Business logic for users
- Database: `user_db`

### 4. **Ticket Service** (Port 8082)
- Ticket creation and management
- Status tracking
- Priority management
- Database: `ticket_db`
- Events: Publishes `ticket-created` to Kafka

### 5. **Assignment Service** (Port 8083)
- Automatic ticket assignment
- Technician matching (location, competence)
- Assignment history
- Database: `assignment_db`
- Events: Consumes `ticket-created`, publishes `ticket-assigned`

### 6. **Notifications Service** (Port 8084)
- Real-time notifications via WebSocket
- Email notifications (planned)
- Notification history
- Database: `notifications_db`
- Events: Consumes `ticket-assigned`

### 7. **Analytics Service** (Port 8085)
- Performance metrics
- Ticket statistics
- Team analytics
- Database: `analytics_db`
- Events: Consumes all events for analytics

---

## 🚀 Getting Started

### Prerequisites

```bash
- Java 17+
- Maven 3.9+
- Docker & Docker Desktop
- Azure CLI
- kubectl
- Terraform
```

### Local Development Setup

1. **Clone the repository**
```bash
git clone https://github.com/Achrafchalkha/itsm-ai-microservices.git
cd itsm-ai-microservices
```

2. **Configure environment variables**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start infrastructure services**
```bash
# Start Kafka & PostgreSQL
docker-compose up -d
```

4. **Build all services**
```bash
mvn clean install -DskipTests
```

5. **Run services** (each in separate terminal)
```bash
# Start Eureka Server first
cd eureka-server && mvn spring-boot:run

# Start other services
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd ticket-service && mvn spring-boot:run
cd assignment-service && mvn spring-boot:run
cd notifications-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
```

6. **Verify services**
- Eureka Dashboard: http://localhost:8761
- Auth Service: http://localhost:8080/actuator/health
- User Service: http://localhost:8081/actuator/health

---

## ☁️ Deployment

### Azure Kubernetes Deployment

The project includes complete Terraform configurations and Kubernetes manifests for Azure deployment.

#### Infrastructure Resources Created:
- Azure Kubernetes Service (3 nodes, Standard_DS2_v2)
- Azure Container Registry
- Azure PostgreSQL Flexible Server
- Azure Virtual Network with subnets
- Load Balancer with public IPs
- Network Security Groups

#### Deploy to AKS:

```bash
# 1. Login to Azure
az login

# 2. Run Jenkins pipeline (automated)
# The Jenkinsfile handles:
#   - Build & test
#   - SonarQube analysis
#   - Docker build
#   - Trivy security scan
#   - Push to ACR
#   - Terraform infrastructure provisioning
#   - Kubernetes deployment

# Or manually:
# 3. Build and push images
docker build -t acritsmac742.azurecr.io/itsm-auth-service:latest ./auth-service
az acr login --name acritsmac742
docker push acritsmac742.azurecr.io/itsm-auth-service:latest

# 4. Deploy to AKS
az aks get-credentials --resource-group rg-itsm-dev --name aks-itsm-dev
kubectl apply -f k8s/
```

---

## 📊 Monitoring

### Prometheus & Grafana Stack

**Access Dashboards:**
- Prometheus: http://20.208.64.39:9090
- Grafana: http://20.199.153.102 (admin/admin123)

**Monitored Metrics:**
- Service uptime and availability
- JVM metrics (heap, non-heap memory)
- HTTP request rates and latencies
- Database connection pool metrics
- Kafka consumer lag
- Custom business metrics

**Grafana Dashboards:**
- Spring Boot APM Dashboard (ID: 12900)
- Custom ITSM metrics dashboard
- Kafka monitoring dashboard

---

## 🔄 CI/CD Pipeline

### Jenkins Pipeline Stages:

1. **Build** - Maven clean package
2. **Test** - Unit tests with JUnit
3. **SonarQube Analysis** - Code quality check
4. **OWASP Dependency Check** - Vulnerability scanning
5. **Docker Build** - Container image creation
6. **Trivy Scan** - Container security scanning
7. **Push to ACR** - Image upload to Azure Container Registry
8. **Terraform Plan** - Infrastructure validation
9. **Deploy to AKS** - Kubernetes deployment

**Pipeline Features:**
- Automated quality gates
- Security scanning at multiple levels
- Zero-downtime deployments
- Rollback capabilities
- Automated notifications

---

## 📁 Project Structure

```
itsm-ai-microservices/
├── analytics-service/       # Analytics microservice
├── assignment-service/      # Assignment logic microservice
├── auth-service/            # Authentication microservice
├── eureka-server/           # Service discovery
├── notifications-service/   # Notifications microservice
├── ticket-service/          # Ticket management microservice
├── user-service/            # User management microservice
├── k8s/                     # Kubernetes manifests
│   ├── deployments.yaml
│   ├── services.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   └── servicemonitor.yaml
├── terraform/               # Infrastructure as Code
│   ├── main.tf
│   ├── variables.tf
│   └── outputs.tf
├── monitoring/              # Prometheus & Grafana configs
├── Jenkinsfile              # CI/CD pipeline
├── .env.example             # Environment template
└── README.md                # This file
```

---

## 🧪 Testing

### API Testing
Use the provided Postman collection for comprehensive API testing.

### Health Checks
All services expose Spring Boot Actuator endpoints:
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/prometheus` - Metrics for Prometheus

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Achraf Chalkha**

- GitHub: [@Achrafchalkha](https://github.com/Achrafchalkha)
- LinkedIn: [Achraf Chalkha](https://linkedin.com/in/achraf-chalkha)

---

## 🙏 Acknowledgments

- Spring Boot & Spring Cloud teams
- Azure Kubernetes Service
- Apache Kafka community
- Prometheus & Grafana projects

---

## 📞 Support

For questions or support, please open an issue on GitHub.

---

**⭐ If you find this project useful, please consider giving it a star!**
