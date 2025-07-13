# User Service - ITSM Platform

## Overview

The User Service is a microservice responsible for managing user business profiles, competencies, team management, and technician assignment in the ITSM platform. It follows Domain-Driven Design (DDD) architecture and implements a hierarchical workflow matching enterprise ITSM practices.

## Hierarchical Workflow

The service implements the following organizational structure:

### **ADMIN (DSI ALTEN)**
- Creates and manages MANAGERs
- Defines organizational structure
- Oversees the entire ITSM platform

### **MANAGER (Team Leaders)**
- Created by ADMIN
- Creates and manages TECHNICIENs in their team
- Assigns competencies and manages team categories
- Handles ticket categories (Infrastructure, Applications, Security)

### **TECHNICIEN (Support Staff)**
- Created by their assigned MANAGER
- Belongs to a specific team with defined competencies
- Has status tracking (DISPONIBLE, OCCUPÉ, ABSENT, HORS_LIGNE)
- Handles tickets based on skills and availability

### **UTILISATEUR (End Users)**
- Self-register through the platform
- Automatically assigned UTILISATEUR role
- Submit tickets and requests for support

## Key Features

- **Hierarchical User Management**: ADMIN → MANAGER → TECHNICIEN workflow
- **Team Management**: Complete team structure with categories and members
- **Competency Management**: Three-level expertise (JUNIOR, SENIOR, EXPERT)
- **Intelligent Assignment**: Find technicians by skills, team, location, status
- **Event-Driven Integration**: Listens to user creation events from auth-service

## Architecture

### Domain-Driven Design Structure

```
src/main/java/com/itsm/user/
├── application/
│   └── service/
│       ├── UserService.java
│       └── TechnicianAssignmentService.java
├── domain/
│   ├── model/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── StatutTechnicien.java
│   │   └── Competence.java
│   └── repository/
│       └── UserRepository.java
├── infrastructure/
│   ├── persistence/
│   │   ├── JpaUserEntity.java
│   │   ├── JpaCompetenceEntity.java
│   │   └── JpaUserRepository.java
│   ├── repository/
│   │   └── UserRepositoryImpl.java
│   ├── kafka/
│   │   ├── UserCreatedEvent.java
│   │   └── UserCreatedEventListener.java
│   ├── security/
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserSecurityService.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── KafkaConfig.java
│   └── mapper/
│       └── UserMapper.java
└── interfaces/
    ├── controller/
    │   ├── UserController.java
    │   └── TechnicianController.java
    ├── dto/
    │   ├── UserProfileResponse.java
    │   ├── UpdateUserProfileRequest.java
    │   ├── AddCompetenceRequest.java
    │   └── TechnicianSearchRequest.java
    └── mapper/
        └── UserDtoMapper.java
```

## Domain Models

### User
- **ID**: UUID (same as auth-service)
- **Basic Info**: nom, prenom, email, role
- **Hierarchy**: managerId (for TECHNICIEN), teamId
- **Business Attributes**: statutTechnicien, localisation
- **Competencies**: List of technical skills (for TECHNICIEN)
- **Metadata**: dateCreation, dateModification, actif

### Team
- **ID**: UUID
- **Details**: nom, description, managerId
- **Categories**: List of ticket categories handled
- **Members**: List of technician IDs
- **Metadata**: dateCreation, dateModification, actif

### Competence
- **ID**: UUID
- **Details**: nom, description, categorie
- **Expertise Level**: JUNIOR, SENIOR, EXPERT

### Enums
- **Role**: ADMIN, MANAGER, TECHNICIEN, UTILISATEUR
- **StatutTechnicien**: DISPONIBLE, OCCUPE, ABSENT, HORS_LIGNE
- **NiveauCompetence**: JUNIOR, SENIOR, EXPERT

## API Endpoints

### User Management
- `GET /api/users/{id}` - Get user profile
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/{id}` - Update user profile
- `POST /api/users/{id}/competences` - Add competence
- `DELETE /api/users/{id}/competences/{competenceId}` - Remove competence
- `PUT /api/users/{id}/status` - Change technician status
- `PUT /api/users/{id}/team` - Assign to team

### Technician Queries
- `GET /api/technicians/available` - Get available technicians
- `POST /api/technicians/search` - Search with criteria
- `GET /api/technicians/best-match` - Find best technician for assignment
- `GET /api/technicians/status/{status}` - Get by status
- `GET /api/technicians/location/{location}` - Get by location
- `GET /api/technicians/team/{teamId}` - Get by team
- `GET /api/technicians/stats/availability` - Get availability statistics

## Configuration

### Database
- **Database**: PostgreSQL
- **Name**: user_db
- **Port**: 5432
- **Tables**: users, competences

### Kafka
- **Bootstrap Servers**: localhost:9092
- **Consumer Group**: user-service-group
- **Topics**: user-created (from auth-service)

### Security
- **JWT Validation**: Validates tokens from auth-service
- **Role-Based Access**: RBAC with @PreAuthorize annotations
- **User Context**: Extracts user ID and role from JWT

### Service Discovery
- **Eureka Client**: Registers with eureka-server on port 8761
- **Service Port**: 8082

## Setup Instructions

### 1. Database Setup
```bash
# Connect to PostgreSQL
psql -U postgres -h localhost

# Run setup script
\i setup-database.sql

# Verify setup
\i check-database.sql
```

### 2. Start Dependencies
```bash
# Start PostgreSQL
# Start Kafka
# Start Eureka Server
# Start Auth Service
```

### 3. Run Application
```bash
# Using Maven
mvn spring-boot:run

# Using JAR
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

## Integration Points

### With Auth Service
- **Kafka Events**: Listens to `user-created` topic
- **JWT Validation**: Validates tokens issued by auth-service
- **User Synchronization**: Creates business profiles when users register

### With Other Services
- **Ticket Service**: Provides technician availability for assignment
- **Assignment Service**: Offers intelligent technician matching
- **Team Service**: Integrates with team management
- **Analytics Service**: Provides user and availability data

## Event Flow

1. **User Registration**: Auth-service publishes UserCreatedEvent
2. **Profile Creation**: User-service creates business profile
3. **Profile Management**: Managers assign teams, locations, competencies
4. **Assignment Queries**: Other services query for available technicians
5. **Status Updates**: Technicians update their availability status

## Security Model

### Authentication
- JWT tokens from auth-service
- Stateless authentication
- Token validation on each request

### Authorization
- Role-based access control (RBAC)
- Method-level security with @PreAuthorize
- User can only access own profile
- Managers can access all profiles

### Roles and Permissions
- **UTILISATEUR**: Read own profile
- **TECHNICIEN**: Read/update own profile, view other technicians
- **MANAGER**: Full access to all profiles and management operations

## Monitoring and Health

### Health Checks
- Spring Boot Actuator endpoints
- Database connectivity
- Kafka connectivity

### Logging
- Structured logging with SLF4J
- Debug level for development
- Request/response logging for API calls

## Testing

### Unit Tests
- Domain model tests
- Service layer tests
- Repository tests

### Integration Tests
- API endpoint tests
- Kafka event processing tests
- Database integration tests

## Deployment

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `APP_JWT_SECRET`
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`
