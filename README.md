# ITSM - IT Service Management System

A complete microservices-based ITSM system with role hierarchy and automatic team assignment.

## 🏗️ Architecture

- **Auth-Service** (Port 8081): Authentication and user management
- **User-Service** (Port 8082): Business profiles and team management
- **Kafka**: Event-driven communication between services
- **PostgreSQL**: Separate databases (auth_db, user_db)

## 👥 Role Hierarchy

```
ITSM System - Role Hierarchy
├── ADMIN (System Administrator)
│   └── Creates MANAGERs with teams
├── MANAGER (Team Lead)
│   ├── Manages specific team
│   └── Creates TECHNICIENs (auto-assigned to their team)
├── TECHNICIEN (Technical Support)
│   ├── Assigned to manager's team automatically
│   ├── Has competences and location
│   └── Resolves tickets
└── UTILISATEUR (End User)
    └── Self-registers and creates tickets
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Docker & Docker Compose

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Achrafchalkha/ITSM.git
   cd ITSM
   ```

2. **Setup environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your actual database credentials
   ```

3. **Create databases:**
   ```sql
   CREATE DATABASE auth_db;
   CREATE DATABASE user_db;
   ```

4. **Start Kafka:**
   ```bash
   cd auth-service
   docker compose up -d
   ```

5. **Start services:**
   ```bash
   # Terminal 1: Auth Service
   cd auth-service
   mvn spring-boot:run

   # Terminal 2: User Service
   cd user-service
   mvn spring-boot:run
   ```

### Verification
- Auth-service: http://localhost:8081/actuator/health
- User-service: http://localhost:8082/actuator/health
- Kafka UI: http://localhost:8080

## 📋 Testing

See `Complete-ITSM-Testing-Guide.md` for comprehensive API testing guide with Postman examples.

## ✨ Features

✅ JWT Authentication with role-based access control  
✅ Automatic team assignment via Kafka events  
✅ Competence and location management  
✅ Clean microservices architecture  
✅ Complete REST API endpoints  
✅ Event-driven communication  
✅ Secure configuration management  

## 🔧 Configuration

The application uses environment variables for sensitive data:

- `DB_PASSWORD`: PostgreSQL password
- `JWT_SECRET`: JWT signing secret
- `DB_USERNAME`: Database username
- `DB_URL`: Database connection URL

## 🏢 Workflow

1. **ADMIN** creates managers with teams
2. **MANAGER** creates technicians (auto-assigned to manager's team)
3. **UTILISATEUR** self-registers with automatic role assignment
4. **TECHNICIEN** gets competences and location automatically

## 📊 Database Schema

### Auth DB (auth_db)
- `utilisateurs`: Authentication data

### User DB (user_db)
- `users`: Business profiles
- `teams`: Team management
- `team_members`: Team membership
- `competences`: User skills

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.
