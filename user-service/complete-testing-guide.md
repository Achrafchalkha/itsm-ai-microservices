# Complete Testing Guide - Auth Service & User Service

## Prerequisites

1. **Start Services in Order:**
   ```bash
   # 1. Start PostgreSQL
   # 2. Start Kafka
   # 3. Start Eureka Server (port 8761)
   # 4. Start Auth Service (port 8081)
   # 5. Start User Service (port 8082)
   ```

2. **Setup Database:**
   ```bash
   # Run the hierarchy setup script
   psql -U postgres -h localhost -d user_db -f complete-hierarchy-setup.sql
   ```

## Testing Workflow

### Phase 1: Service Health Checks

```bash
# Check Eureka Server
curl http://localhost:8761/

# Check Auth Service Health
curl http://localhost:8081/actuator/health

# Check User Service Health  
curl http://localhost:8082/actuator/health

# Verify services are registered in Eureka
curl http://localhost:8761/eureka/apps
```

### Phase 2: Auth Service Testing

#### 1. Register Users (Self-Registration for UTILISATEUR)
```bash
# Register end user (auto-assigned UTILISATEUR role)
curl -X POST "http://localhost:8081/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@alten.com",
    "motDePasse": "password123",
    "nom": "Test",
    "prenom": "User"
  }'
```

#### 2. Login and Get JWT Tokens
```bash
# Login as ADMIN
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@alten.com",
    "motDePasse": "password123"
  }'

# Login as MANAGER (Infrastructure)
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "pierre.durand@alten.com",
    "motDePasse": "password123"
  }'

# Login as TECHNICIEN
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jean.dupont@alten.com",
    "motDePasse": "password123"
  }'

# Login as UTILISATEUR
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sophie.leroy@alten.com",
    "motDePasse": "password123"
  }'
```

**Save the JWT tokens from responses for next steps!**

### Phase 3: User Service Testing

#### 1. User Profile Management

```bash
# Get user profile (replace {USER_ID} and {JWT_TOKEN})
curl -X GET "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000301" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Update user profile
curl -X PUT "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000301" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "localisation": "Paris - La Défense",
    "statutTechnicien": "DISPONIBLE"
  }'
```

#### 2. Competency Management (MANAGER only)

```bash
# Add competence to technician (use MANAGER token)
curl -X POST "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000301/competences" \
  -H "Authorization: Bearer {MANAGER_JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Docker",
    "description": "Containerization and orchestration",
    "categorie": "DevOps",
    "niveau": "SENIOR"
  }'

# Remove competence
curl -X DELETE "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000301/competences/{COMPETENCE_ID}" \
  -H "Authorization: Bearer {MANAGER_JWT_TOKEN}"
```

#### 3. Technician Assignment Queries

```bash
# Get all available technicians
curl -X GET "http://localhost:8082/api/technicians/available" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Search technicians by skill
curl -X GET "http://localhost:8082/api/technicians/available?skill=Cisco&level=2" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Search technicians by location
curl -X GET "http://localhost:8082/api/technicians/location/Paris" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Search technicians by team
curl -X GET "http://localhost:8082/api/technicians/team/00000000-0000-0000-0000-000000000201" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Find best technician for assignment
curl -X GET "http://localhost:8082/api/technicians/best-match?skill=Windows%20Server&level=2&location=Paris" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Advanced search
curl -X POST "http://localhost:8082/api/technicians/search" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "competence": "SAP",
    "niveauMinimum": 2,
    "localisation": "Lyon",
    "statut": "DISPONIBLE"
  }'
```

#### 4. Status Management

```bash
# Change technician status
curl -X PUT "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000301/status?status=OCCUPE" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Get availability statistics (MANAGER only)
curl -X GET "http://localhost:8082/api/technicians/stats/availability" \
  -H "Authorization: Bearer {MANAGER_JWT_TOKEN}"
```

#### 5. Team Management

```bash
# Get users by role (MANAGER only)
curl -X GET "http://localhost:8082/api/users/role/TECHNICIEN" \
  -H "Authorization: Bearer {MANAGER_JWT_TOKEN}"

# Assign user to team (MANAGER only)
curl -X PUT "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000301/team?teamId=00000000-0000-0000-0000-000000000201" \
  -H "Authorization: Bearer {MANAGER_JWT_TOKEN}"
```

### Phase 4: Integration Testing

#### 1. Test Kafka Integration
```bash
# Register a new user in auth-service and verify profile creation in user-service
curl -X POST "http://localhost:8081/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "integration.test@alten.com",
    "motDePasse": "password123",
    "nom": "Integration",
    "prenom": "Test"
  }'

# Check if profile was created in user-service (get the user ID from auth response)
curl -X GET "http://localhost:8082/api/users/{NEW_USER_ID}" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

#### 2. Test Service Discovery
```bash
# Verify services are discoverable through Eureka
curl http://localhost:8761/eureka/apps/AUTH-SERVICE
curl http://localhost:8761/eureka/apps/USER-SERVICE
```

## Expected Test Results

### Successful Hierarchy Verification:
- **1 ADMIN**: admin@alten.com
- **3 MANAGERS**: Infrastructure, Applications, Security
- **7 TECHNICIANS**: Distributed across teams
- **3+ UTILISATEURS**: Self-registered end users

### Competency Distribution:
- **Infrastructure Team**: Network, System, Desktop specialists
- **Applications Team**: ERP, CRM specialists  
- **Security Team**: Security analysis, Identity management

### Access Control:
- **ADMIN**: Can view all users and teams
- **MANAGER**: Can manage their team members and competencies
- **TECHNICIEN**: Can view/update own profile and team info
- **UTILISATEUR**: Can view/update own profile only

## Troubleshooting

### Common Issues:
1. **JWT Token Expired**: Re-login to get new token
2. **403 Forbidden**: Check user role permissions
3. **404 Not Found**: Verify user/team IDs exist
4. **Service Not Found**: Check Eureka registration
5. **Database Connection**: Verify PostgreSQL is running

### Debug Commands:
```bash
# Check service logs
docker logs auth-service
docker logs user-service

# Verify database data
psql -U postgres -h localhost -d user_db -c "SELECT * FROM users WHERE role = 'ADMIN';"

# Check Kafka topics
kafka-topics.sh --list --bootstrap-server localhost:9092
```
