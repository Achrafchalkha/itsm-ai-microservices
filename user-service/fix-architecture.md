# ITSM Architecture Fix

## Current Issues
1. JWT tokens only generated during registration
2. Only UTILISATEUR can register
3. Two separate user tables (auth_db.utilisateurs vs user_db.users)
4. No proper login flow

## Recommended Solution: Centralized Auth

### Architecture:
```
┌─────────────────┐    ┌─────────────────┐
│   AUTH-SERVICE  │    │   USER-SERVICE  │
│   (Port 8081)   │    │   (Port 8082)   │
│                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ Users Table │ │    │ │ Teams Table │ │
│ │ JWT Auth    │ │    │ │ Competences │ │
│ │ Login/Reg   │ │    │ │ Assignment  │ │
│ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘
         │                       │
         └───────────────────────┘
              HTTP Calls
```

### Changes Needed:

#### 1. AUTH-SERVICE (Master User Management)
- Keep all user data in auth_db.utilisateurs
- Add login endpoint that returns JWT
- Support all roles (ADMIN, MANAGER, TECHNICIEN, UTILISATEUR)
- Handle user creation for all roles

#### 2. USER-SERVICE (Business Logic)
- Remove users table
- Call auth-service for user data
- Focus on teams, competences, assignments
- Validate JWT tokens only

#### 3. Database Structure:
- auth_db: utilisateurs, roles, permissions
- user_db: teams, competences, assignments (no users table)
