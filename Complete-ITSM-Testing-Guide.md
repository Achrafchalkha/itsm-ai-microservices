# Complete ITSM Platform Testing Guide

## Prerequisites
- Auth-service running on port 8081
- User-service running on port 8082
- PostgreSQL databases (auth_db, user_db) running
- Kafka running (optional for events)

## Phase 1: Authentication Testing (Auth-Service)

### 1.1 Admin Login
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "admin@itsm.com",
  "password": "admin123"
}
```
**Save the admin token for subsequent tests**

### 1.2 Create Manager with Team
```bash
POST http://localhost:8081/api/admin/createmanager
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "manager@itsm.com",
  "motDePasse": "manager123",
  "teamName": "Support Technique",
  "teamDescription": "Équipe de support technique niveau 1 et 2"
}
```

### 1.3 Manager Login
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "manager@itsm.com",
  "password": "manager123"
}
```
**Save the manager token**

### 1.4 Create Technician (using Manager token)
```bash
POST http://localhost:8081/api/auth/register
Authorization: Bearer {manager-token}
Content-Type: application/json

{
  "nom": "Martin",
  "prenom": "Pierre",
  "email": "tech@itsm.com",
  "motDePasse": "tech123",
  "role": "TECHNICIEN"
}
```

### 1.5 Technician Login
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "tech@itsm.com",
  "password": "tech123"
}
```
**Save the technician token**

### 1.6 Regular User Registration
```bash
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "nom": "Client",
  "prenom": "Test",
  "email": "client@itsm.com",
  "motDePasse": "client123"
}
```

## Phase 2: User Service Testing

### 2.1 Get All Users (Admin/Manager)
```bash
GET http://localhost:8082/api/users
Authorization: Bearer {admin-token}
```

### 2.2 Get User Profile
```bash
GET http://localhost:8082/api/users/profile
Authorization: Bearer {any-token}
```

### 2.3 Update User Profile
```bash
PUT http://localhost:8082/api/users/profile
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "nom": "Martin",
  "prenom": "Pierre",
  "localisation": "Paris Bureau 1",
  "statutTechnicien": "DISPONIBLE"
}
```

## Phase 3: Team Management Testing

### 3.1 Get All Teams
```bash
GET http://localhost:8082/api/teams
Authorization: Bearer {any-token}
```

### 3.2 Create Additional Team (Admin only)
```bash
POST http://localhost:8082/api/teams
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "nom": "Infrastructure",
  "description": "Équipe infrastructure et réseau",
  "managerId": "{manager-user-id}",
  "categories": ["RESEAU", "SERVEUR", "SECURITE"]
}
```

### 3.3 Add Technician to Team
```bash
POST http://localhost:8082/api/teams/{team-id}/members/{technician-id}
Authorization: Bearer {manager-token}
```

### 3.4 Get Team Details
```bash
GET http://localhost:8082/api/teams/{team-id}
Authorization: Bearer {any-token}
```

## Phase 4: Technician Skills Testing

### 4.1 Add Competence to Technician
```bash
POST http://localhost:8082/api/users/{technician-id}/competences
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "nom": "Java",
  "description": "Développement Java Spring Boot",
  "categorie": "DEVELOPPEMENT",
  "niveau": "EXPERT"
}
```

### 4.2 Add More Competences
```bash
POST http://localhost:8082/api/users/{technician-id}/competences
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "nom": "Docker",
  "description": "Containerisation et orchestration",
  "categorie": "INFRASTRUCTURE",
  "niveau": "INTERMEDIAIRE"
}
```

### 4.3 Get Available Technicians
```bash
GET http://localhost:8082/api/technicians/available
Authorization: Bearer {manager-token}
```

### 4.4 Search Technicians by Skills
```bash
GET http://localhost:8082/api/technicians/by-skills?skills=Java,Docker&level=2
Authorization: Bearer {manager-token}
```

### 4.5 Update Technician Status
```bash
PUT http://localhost:8082/api/users/{technician-id}/status
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "statutTechnicien": "OCCUPE"
}
```

## Phase 5: Authorization Testing

### 5.1 Test Unauthorized Access (Should Fail)
```bash
GET http://localhost:8082/api/users
# No Authorization header - should return 401
```

### 5.2 Test Insufficient Privileges (Should Fail)
```bash
POST http://localhost:8081/api/admin/createmanager
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "nom": "Test",
  "prenom": "Fail",
  "email": "fail@itsm.com",
  "motDePasse": "fail123",
  "teamName": "Fail Team"
}
# Should return 403 Forbidden
```

## Phase 6: Database Verification

### 6.1 Check Auth Database
```sql
-- Connect to auth_db
psql -U postgres -h localhost -d auth_db

-- Check all users
SELECT nom, prenom, email, role, actif FROM utilisateurs ORDER BY role;

-- Check teams
SELECT name, description, manager_id FROM teams;
```

### 6.2 Check User Database
```sql
-- Connect to user_db
psql -U postgres -h localhost -d user_db

-- Check user profiles
SELECT nom, prenom, email, role, team_id, statut_technicien FROM users ORDER BY role;

-- Check teams
SELECT nom, description, manager_id FROM teams;

-- Check competences
SELECT u.nom, u.prenom, c.nom as competence, c.niveau 
FROM users u 
JOIN user_competences uc ON u.id = uc.user_id 
JOIN competences c ON uc.competence_id = c.id;
```

## Expected Results Summary

✅ **Authentication**: All roles can login and get JWT tokens  
✅ **Authorization**: Role-based access control working  
✅ **User Management**: CRUD operations for users  
✅ **Team Management**: Teams created and members assigned  
✅ **Skills Management**: Competences added to technicians  
✅ **Database Sync**: Both databases have consistent data  
✅ **Kafka Events**: Events published and consumed (if Kafka running)  

## Troubleshooting

- **401 Unauthorized**: Check JWT token format and validity
- **403 Forbidden**: Check user role and endpoint permissions
- **404 Not Found**: Verify service is running on correct port
- **500 Internal Error**: Check database connections and logs
