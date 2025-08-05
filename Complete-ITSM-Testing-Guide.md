# 🚀 Complete ITSM Platform Testing Guide

## 📋 Prerequisites & Services Overview

### 🏗️ **Microservices Architecture**
- **Auth-Service** (Port 8081) - Authentication & User Management
- **User-Service** (Port 8082) - Teams, Technicians & Competences
- **Ticket-Service** (Port 8083) - Ticket Management & NLP
- **Assignment-Service** (Port 8084) - Intelligent Ticket Assignment
- **Notifications-Service** (Port 8085) - Real-time Notifications

### 🗄️ **Databases Required**
- **auth_db** - User authentication data
- **user_db** - Teams, competences, assignments
- **ticket_db** - Tickets and resolution data
- **assignment_db** - Assignment history and analytics
- **notifications_db** - Notification preferences and history

### 🔧 **Infrastructure**
- **Kafka** - Event streaming (required for assignment & notifications)
- **PostgreSQL** - Primary database
- **Eureka** - Service discovery (optional)
- **Gemini AI** - NLP analysis (API key required)

---

## 🔐 Phase 1: Authentication Testing (Auth-Service - Port 8081)

### 🎯 **Available Endpoints**
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration (UTILISATEUR only)
- `POST /api/auth/admin/create-manager` - Create manager with team (ADMIN only)
- `POST /api/auth/sync/create-user` - Internal user sync

### 1.1 Admin Login
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "admin@itsm.com",
  "motDePasse": "admin123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "admin-uuid",
    "nom": "Admin",
    "prenom": "System",
    "email": "admin@itsm.com",
    "role": "ADMIN"
  }
}
```
**💾 Save the admin token for subsequent tests**

### 1.2 Create Manager with Team
```bash
POST http://localhost:8081/api/auth/admin/create-manager
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
  "motDePasse": "manager123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "manager-uuid"
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "manager@itsm.com",
    "role": "MANAGER"
  }
}
```
**💾 Save the manager token**

### 1.4 User Registration
```bash
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "nom": "Martin",
  "prenom": "Alice",
  "email": "alice.martin@itsm.com",
  "motDePasse": "user123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "user-uuid",
    "nom": "Martin",
    "prenom": "Alice",
    "email": "alice.martin@itsm.com",
    "role": "UTILISATEUR"
  }
}
```

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

---

## 👥 Phase 2: User Management Testing (User-Service - Port 8082)

### 🎯 **Available Endpoints**

#### **Manager Endpoints**
- `POST /api/manager/technicians` - Create technician (MANAGER only)
- `GET /api/manager/technicians` - List team technicians
- `PUT /api/manager/technicians/{id}` - Update technician
- `GET /api/manager/teams/{id}` - Get team details
- `GET /api/manager/debug-sync/{id}` - Debug database sync

#### **Technician Endpoints**
- `GET /api/technician/profile` - Get own profile
- `PUT /api/technician/profile` - Update own profile
- `PUT /api/technician/status` - Update availability status
- `POST /api/technician/competences` - Add competence

#### **Public Endpoints (Service-to-Service)**
- `GET /api/public/assignment/technicians` - Get available technicians
- `GET /api/public/assignment/teams/{id}` - Get team details
- `POST /api/public/assignment/workload` - Update technician workload

### 2.1 Create Technician (Manager)
```bash
POST http://localhost:8082/api/manager/technicians
Authorization: Bearer {manager-token}
Content-Type: application/json

{
  "nom": "Durand",
  "prenom": "Pierre",
  "email": "pierre.durand@itsm.com",
  "motDePasse": "tech123",
  "localisation": "Paris",
  "telephone": "+33123456789",
  "specialite": "Infrastructure",
  "competences": [
    {
      "nom": "Jenkins",
      "description": "Configuration de pipelines d'intégration continue",
      "categorie": "DEVOPS",
      "niveau": "AVANCE"
    },
    {
      "nom": "Docker",
      "description": "Containerisation des applications",
      "categorie": "CLOUD",
      "niveau": "INTERMEDIAIRE"
    },
    {
      "nom": "Ansible",
      "description": "Automatisation de la configuration des serveurs",
      "categorie": "DEVOPS",
      "niveau": "SENIOR"
    }
  ]
}
```

**Expected Response:**
```json
{
  "id": "technician-uuid",
  "nom": "Durand",
  "prenom": "Pierre",
  "email": "pierre.durand@itsm.com",
  "role": "TECHNICIEN",
  "teamId": "team-uuid",
  "competences": [
    {
      "nom": "Jenkins",
      "categorie": "DEVOPS",
      "niveau": "AVANCE"
    }
  ]
}
```

### 2.2 Get Available Technicians (For Assignment)
```bash
GET http://localhost:8082/api/public/assignment/technicians?category=DEVOPS&maxWorkload=5
```

**Expected Response:**
```json
[
  {
    "id": "technician-uuid",
    "nom": "Durand",
    "prenom": "Pierre",
    "email": "pierre.durand@itsm.com",
    "statut": "DISPONIBLE",
    "chargeActuelle": 2,
    "competences": [
      {
        "nom": "Jenkins",
        "categorie": "DEVOPS",
        "niveau": "AVANCE"
      }
    ]
  }
]
```

### 2.3 Get Team Details
```bash
GET http://localhost:8082/api/public/assignment/teams/{team-id}
```

---

## 🎫 Phase 3: Ticket Management Testing (Ticket-Service - Port 8083)

### 🎯 **Available Endpoints**

#### **User Endpoints**
- `POST /api/tickets` - Create ticket (UTILISATEUR)
- `GET /api/tickets/user/{userId}` - Get user's tickets
- `GET /api/tickets/{id}` - Get ticket details
- `GET /api/tickets/auth-test` - Test authentication

#### **Technician Endpoints**
- `GET /api/technician/my-tickets` - Get assigned tickets
- `GET /api/technician/my-tickets/status/{status}` - Filter by status
- `PUT /api/technician/tickets/{id}/start` - Start working (EN_COURS → OUVERT)
- `POST /api/technician/tickets/{id}/notes` - Add work notes
- `PUT /api/technician/tickets/{id}/resolve` - Resolve ticket
- `PUT /api/technician/tickets/{id}/request-reassignment` - Request reassignment
- `GET /api/technician/dashboard` - Get dashboard summary
- `GET /api/technician/debug/all-tickets` - Debug endpoint

#### **Manager Endpoints**
- `GET /api/manager/team/tickets` - Get all team tickets (with pagination & filters)
- `GET /api/manager/team/tickets/status/{status}` - Get team tickets by status
- `GET /api/manager/tickets/{id}` - Get ticket details (team tickets only)
- `GET /api/manager/team/dashboard` - Get team dashboard statistics

#### **Assignment Service Endpoints (Internal)**
- `GET /api/public/assignment/tickets/{id}` - Get ticket for assignment
- `PUT /api/public/assignment/tickets/{id}/assign` - Update assignment

### 3.1 Create Ticket (User)
```bash
POST http://localhost:8083/api/tickets
Authorization: Bearer {user-token}
Content-Type: application/json

{
  "titre": "VPN ne fonctionne pas",
  "description": "Impossible de me connecter au VPN de l'entreprise depuis ce matin. Message d'erreur: 'Connection timeout'. J'ai essayé de redémarrer mon ordinateur mais le problème persiste.",
  "enableNlp": true
}
```

**Expected Response:**
```json
{
  "ticketId": "ticket-uuid",
  "titre": "VPN ne fonctionne pas",
  "statut": "EN_COURS",
  "priorite": "NORMALE",
  "categorie": "SECURITE",
  "nlpRecommendation": {
    "recommendedCategory": "SECURITE",
    "recommendedPriority": "HAUTE",
    "confidence": 0.85,
    "reasoning": "Analyse par IA Gemini"
  }
}
```

### 3.2 Get User's Tickets
```bash
GET http://localhost:8083/api/tickets/user/{user-id}
Authorization: Bearer {user-token}
```

### 3.3 Technician Dashboard
```bash
GET http://localhost:8083/api/technician/dashboard
Authorization: Bearer {technician-token}
```

**Expected Response:**
```json
{
  "totalAssignedTickets": 5,
  "openTickets": 2,
  "inProgressTickets": 1,
  "resolvedToday": 1,
  "overdueTickets": 0,
  "averageResolutionTime": 120.5,
  "urgentTicketsCount": 1,
  "newTicketsCount": 1
}
```

### 3.4 Get Technician's Assigned Tickets
```bash
GET http://localhost:8083/api/technician/my-tickets?page=0&size=20&sortBy=dateCreation&sortDir=desc
Authorization: Bearer {technician-token}
```

### 3.5 Start Working on Ticket
```bash
PUT http://localhost:8083/api/technician/tickets/{ticket-id}/start
Authorization: Bearer {technician-token}
```

**Expected Result:**
- Status changes: `EN_COURS` → `OUVERT`
- Adds comment: `"[2025-07-23 14:30:00] Travail commencé sur le ticket"`

### 3.6 Add Work Notes
```bash
POST http://localhost:8083/api/technician/tickets/{ticket-id}/notes
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "note": "Diagnostic initial: ping vers le serveur VPN échoue. Vérification du statut des services en cours."
}
```

**Expected Result:**
- Replaces `commentaire_resolution` with: `"[2025-07-23 14:35:00] Note de travail: Diagnostic initial..."`

### 3.7 Resolve Ticket
```bash
PUT http://localhost:8083/api/technician/tickets/{ticket-id}/resolve
Authorization: Bearer {technician-token}
Content-Type: application/json

{
  "solution": "Service VPN redémarré à 14h35. Tests de connectivité réussis avec 3 utilisateurs. Problème résolu. Recommandation: ajouter surveillance proactive du service VPN."
}
```

**Expected Result:**
- Status changes: `OUVERT` → `RESOLU`
- Replaces `commentaire_resolution` with resolution
- Calculates resolution time and SLA compliance
- Decrements technician workload

---

## 🤖 Phase 4: Assignment Service Testing (Assignment-Service - Port 8084)

### 🎯 **Available Endpoints**

#### **Assignment Management**
- `POST /api/assignments/manual` - Manual assignment (MANAGER/ADMIN)
- `PUT /api/assignments/{id}/reassign` - Reassign ticket (MANAGER/ADMIN)
- `GET /api/assignments/ticket/{ticketId}` - Get assignment by ticket
- `GET /api/assignments/technician/{technicianId}` - Get technician assignments
- `GET /api/assignments/team/{teamId}` - Get team assignments
- `GET /api/assignments/stats` - Assignment statistics

### 4.1 Manual Assignment
```bash
POST http://localhost:8084/api/assignments/manual
Authorization: Bearer {manager-token}
Content-Type: application/json

{
  "ticketId": "ticket-uuid",
  "technicianId": "technician-uuid",
  "assignedBy": "manager-uuid",
  "reason": "Expertise spécifique en Jenkins requise"
}
```

**Expected Response:**
```json
{
  "id": "assignment-uuid",
  "ticketId": "ticket-uuid",
  "technicianId": "technician-uuid",
  "teamId": "team-uuid",
  "assignedBy": "manager-uuid",
  "assignmentStrategy": "MANUAL",
  "confidenceScore": 1.0,
  "reason": "Expertise spécifique en Jenkins requise",
  "assignedAt": "2025-07-23T14:30:00",
  "status": "ACTIVE"
}
```

### 4.2 Reassign Ticket
```bash
PUT http://localhost:8084/api/assignments/{assignment-id}/reassign
Authorization: Bearer {manager-token}
Content-Type: application/json

{
  "newTechnicianId": "other-technician-uuid",
  "reassignedBy": "manager-uuid",
  "reason": "Équilibrage de charge de travail"
}
```

### 4.3 Get Assignment Statistics
```bash
GET http://localhost:8084/api/assignments/stats
Authorization: Bearer {manager-token}
```

**Expected Response:**
```json
{
  "totalAssignments": 150,
  "activeAssignments": 45,
  "averageConfidenceScore": 0.78,
  "strategyDistribution": {
    "HYBRID": {
      "count": 100,
      "avgConfidence": 0.82
    },
    "LEAST_WORKLOAD": {
      "count": 30,
      "avgConfidence": 0.65
    },
    "MANUAL": {
      "count": 20,
      "avgConfidence": 1.0
    }
  },
  "technicianWorkload": [
    {
      "technicianId": "tech-1",
      "currentWorkload": 3,
      "averageResolutionTime": 120
    }
  ]
}
```

### 4.4 Get Technician Assignments
```bash
GET http://localhost:8084/api/assignments/technician/{technician-id}?page=0&size=20
Authorization: Bearer {technician-token}
```

### 4.5 Get Team Assignments
```bash
GET http://localhost:8084/api/assignments/team/{team-id}?page=0&size=20
Authorization: Bearer {manager-token}
```

### 4.6 Automatic Assignment (Triggered by Ticket Creation)
When a ticket is created, the assignment-service automatically:

1. **Receives TicketCreatedEvent** via Kafka
2. **Analyzes ticket** with Gemini AI for category/priority
3. **Finds available technicians** with matching competences
4. **Calculates best assignment** using hybrid strategy
5. **Assigns ticket** and publishes AssignmentCreatedEvent
6. **Updates technician workload**

**Kafka Events Flow:**
```
ticket.created → assignment.created → technician.workload.updated
```

---

## 🔔 Phase 5: Notifications Service Testing (Notifications-Service - Port 8085)

### 🎯 **Available Endpoints**

#### **Notification Management**
- `GET /api/notifications` - Get user notifications
- `GET /api/notifications/unread-count` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/read-all` - Mark all as read
- `GET /api/notifications/paginated` - Paginated notifications

#### **Notification Preferences**
- `GET /api/notifications/preferences` - Get user preferences
- `PUT /api/notifications/preferences` - Update preferences
- `PUT /api/notifications/preferences/email/enabled` - Toggle email notifications
- `PUT /api/notifications/preferences/dashboard/enabled` - Toggle dashboard notifications

### 5.1 Get User Notifications
```bash
GET http://localhost:8085/api/notifications?unreadOnly=true&limit=20
Authorization: Bearer {user-token}
```

**Expected Response:**
```json
[
  {
    "id": "notification-uuid",
    "type": "TICKET_ASSIGNED",
    "title": "Ticket #abc123 assigned to you",
    "message": "You have been assigned to ticket: VPN ne fonctionne pas",
    "priority": "HIGH",
    "readStatus": false,
    "createdAt": "2025-07-23T14:30:00",
    "ticketId": "ticket-uuid",
    "userId": "user-uuid"
  },
  {
    "id": "notification-uuid-2",
    "type": "TICKET_RESOLVED",
    "title": "Your ticket has been resolved",
    "message": "Ticket #abc123 - VPN ne fonctionne pas has been marked as RESOLVED",
    "priority": "MEDIUM",
    "readStatus": false,
    "createdAt": "2025-07-23T15:00:00",
    "ticketId": "ticket-uuid",
    "userId": "user-uuid"
  }
]
```

### 5.2 Get Unread Count
```bash
GET http://localhost:8085/api/notifications/unread-count
Authorization: Bearer {user-token}
```

**Expected Response:**
```json
{
  "unreadCount": 3
}
```

### 5.3 Mark Notification as Read
```bash
PUT http://localhost:8085/api/notifications/{notification-id}/read
Authorization: Bearer {user-token}
```

### 5.4 Mark All Notifications as Read
```bash
PUT http://localhost:8085/api/notifications/read-all
Authorization: Bearer {user-token}
```

### 5.5 Get Notification Preferences
```bash
GET http://localhost:8085/api/notifications/preferences
Authorization: Bearer {user-token}
```

**Expected Response:**
```json
{
  "userId": "user-uuid",
  "emailEnabled": true,
  "dashboardEnabled": true,
  "emailAddress": "user@itsm.com",
  "notificationTypes": {
    "TICKET_ASSIGNED": true,
    "TICKET_RESOLVED": true,
    "TICKET_UPDATED": false,
    "ASSIGNMENT_FAILED": true
  }
}
```

### 5.6 Update Notification Preferences
```bash
PUT http://localhost:8085/api/notifications/preferences
Authorization: Bearer {user-token}
Content-Type: application/json

{
  "emailEnabled": true,
  "dashboardEnabled": true,
  "notificationTypes": {
    "TICKET_ASSIGNED": true,
    "TICKET_RESOLVED": true,
    "TICKET_UPDATED": true,
    "ASSIGNMENT_FAILED": false
  }
}
```

### 5.7 Toggle Email Notifications
```bash
PUT http://localhost:8085/api/notifications/preferences/email/enabled
Authorization: Bearer {user-token}
Content-Type: application/json

{
  "enabled": false
}
```

### 5.8 Automatic Notifications (Event-Driven)
The notifications-service automatically creates notifications for:

#### **Ticket Events:**
- **TICKET_ASSIGNED** - When ticket assigned to technician
- **TICKET_RESOLVED** - When ticket marked as resolved
- **TICKET_UPDATED** - When ticket status changes
- **ASSIGNMENT_FAILED** - When no technician available

#### **Manager Events:**
- **MANAGER_NOTIFICATION** - When tickets waiting for assignment

**Kafka Events Consumed:**
```
assignment.created → TICKET_ASSIGNED notification
ticket.resolved → TICKET_RESOLVED notification
manager.notification → MANAGER_NOTIFICATION
assignment.failed → ASSIGNMENT_FAILED notification
```

---

## 🔄 Phase 6: Complete End-to-End Testing Scenarios

### 🎯 **Scenario 1: Complete Ticket Lifecycle**

#### **Step 1: User Creates Ticket**
```bash
# User login
USER_TOKEN=$(curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "alice.martin@itsm.com", "motDePasse": "user123"}' | jq -r '.token')

# Create ticket
TICKET_RESPONSE=$(curl -s -X POST "http://localhost:8083/api/tickets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "titre": "Pipeline Jenkins en échec",
    "description": "Le pipeline de déploiement Jenkins échoue systématiquement à l'\''étape de build Docker. Erreur: '\''Docker daemon not responding'\''.",
    "enableNlp": true
  }')

TICKET_ID=$(echo $TICKET_RESPONSE | jq -r '.ticketId')
echo "Created ticket: $TICKET_ID"
```

#### **Step 2: Automatic Assignment (AI-Powered)**
The system automatically:
1. Analyzes ticket with Gemini AI → Category: `DEVOPS`
2. Finds technicians with Jenkins/Docker competences
3. Assigns to best match based on workload + expertise
4. Sends notification to technician

#### **Step 3: Technician Works on Ticket**
```bash
# Technician login
TECH_TOKEN=$(curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "pierre.durand@itsm.com", "motDePasse": "tech123"}' | jq -r '.token')

# Check dashboard
curl -X GET "http://localhost:8083/api/technician/dashboard" \
  -H "Authorization: Bearer $TECH_TOKEN"

# Start working
curl -X PUT "http://localhost:8083/api/technician/tickets/$TICKET_ID/start" \
  -H "Authorization: Bearer $TECH_TOKEN"

# Add work notes
curl -X POST "http://localhost:8083/api/technician/tickets/$TICKET_ID/notes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TECH_TOKEN" \
  -d '{"note": "Docker daemon redémarré. Tests de pipeline en cours."}'

# Resolve ticket
curl -X PUT "http://localhost:8083/api/technician/tickets/$TICKET_ID/resolve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TECH_TOKEN" \
  -d '{
    "solution": "Problème résolu: Docker daemon était arrêté. Service redémarré et pipeline testé avec succès. Recommandation: ajouter monitoring du service Docker."
  }'
```

#### **Step 4: User Receives Notifications**
```bash
# Check notifications
curl -X GET "http://localhost:8085/api/notifications?unreadOnly=true" \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 🎯 **Scenario 2: Manager Oversight**

#### **Manager Dashboard & Team Management**
```bash
# Manager login
MANAGER_TOKEN=$(curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "manager@itsm.com", "motDePasse": "manager123"}' | jq -r '.token')

# View team assignments
curl -X GET "http://localhost:8084/api/assignments/team/{team-id}" \
  -H "Authorization: Bearer $MANAGER_TOKEN"

# Manual reassignment if needed
curl -X PUT "http://localhost:8084/api/assignments/{assignment-id}/reassign" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -d '{
    "newTechnicianId": "other-tech-uuid",
    "reassignedBy": "manager-uuid",
    "reason": "Expertise spécialisée requise"
  }'

# View assignment statistics
curl -X GET "http://localhost:8084/api/assignments/stats" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

### 🎯 **Scenario 3: No Available Technicians**

When no technicians are available:
1. **Ticket Status** → `EN_ATTENTE`
2. **Manager Notification** → Sent via Kafka
3. **Email Alert** → Manager receives notification
4. **Dashboard Alert** → Shows waiting tickets

```bash
# Check tickets in EN_ATTENTE status
curl -X GET "http://localhost:8083/api/technician/my-tickets/status/EN_ATTENTE" \
  -H "Authorization: Bearer $TECH_TOKEN"

# Manager checks notifications
curl -X GET "http://localhost:8085/api/notifications?unreadOnly=true" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

---

## 🛠️ Troubleshooting Guide

### 🔍 **Common Issues & Solutions**

#### **1. Authentication Issues**
```bash
# Test authentication
curl -X GET "http://localhost:8083/api/tickets/auth-test" \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with user details
# If 401/403: Check token validity and role
```

#### **2. Service Communication Issues**
```bash
# Check service health
curl -X GET "http://localhost:8082/api/public/assignment/health"
curl -X GET "http://localhost:8083/api/public/assignment/health"

# Check Eureka registration (if using)
curl -X GET "http://localhost:8761/eureka/apps"
```

#### **3. Database Connection Issues**
```bash
# Check database connectivity
docker exec -it postgres_container psql -U itsm_user -d auth_db -c "SELECT COUNT(*) FROM utilisateurs;"
docker exec -it postgres_container psql -U itsm_user -d user_db -c "SELECT COUNT(*) FROM teams;"
docker exec -it postgres_container psql -U itsm_user -d ticket_db -c "SELECT COUNT(*) FROM tickets;"
```

#### **4. Kafka Issues**
```bash
# Check Kafka topics
docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Expected topics:
# - ticket.created
# - assignment.created
# - manager.notification
# - technician.workload.updated
```

#### **5. Assignment Not Working**
```bash
# Debug technician availability
curl -X GET "http://localhost:8082/api/public/assignment/technicians?category=DEVOPS"

# Debug ticket assignment
curl -X GET "http://localhost:8083/api/technician/debug/all-tickets" \
  -H "Authorization: Bearer $TECH_TOKEN"
```

### 📊 **Performance Testing**

#### **Load Testing with Multiple Tickets**
```bash
#!/bin/bash
# Create 10 tickets rapidly
for i in {1..10}; do
  curl -X POST "http://localhost:8083/api/tickets" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d "{
      \"titre\": \"Test Ticket $i\",
      \"description\": \"Load testing ticket number $i\",
      \"enableNlp\": true
    }" &
done
wait

# Check assignment statistics
curl -X GET "http://localhost:8084/api/assignments/stats" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

### 🔧 **Environment Variables**

#### **Required Environment Variables**
```bash
# Gemini AI (for NLP)
export GEMINI_API_KEY="your-gemini-api-key"

# Database URLs
export AUTH_DB_URL="jdbc:postgresql://localhost:5432/auth_db"
export USER_DB_URL="jdbc:postgresql://localhost:5432/user_db"
export TICKET_DB_URL="jdbc:postgresql://localhost:5432/ticket_db"

# Kafka
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

# Service URLs (for inter-service communication)
export AUTH_SERVICE_URL="http://localhost:8081"
export USER_SERVICE_URL="http://localhost:8082"
export TICKET_SERVICE_URL="http://localhost:8083"
```

---

## 📈 Success Metrics

### ✅ **System Health Indicators**
- All services respond to health checks
- Database connections established
- Kafka topics created and accessible
- JWT authentication working across services

### ✅ **Functional Success**
- Users can create tickets
- Tickets automatically assigned to appropriate technicians
- Technicians can work on and resolve tickets
- Notifications sent and received
- Managers can oversee and reassign tickets

### ✅ **Performance Success**
- Ticket creation < 2 seconds
- Assignment decision < 5 seconds
- Notification delivery < 1 second
- Dashboard loads < 3 seconds

This comprehensive testing guide covers all aspects of the ITSM platform! 🎉
Authorization: Bearer {admin-token}
```

### 2.4 Update Manager (ADMIN only)
```bash
PUT http://localhost:8082/api/admin/managers/{managerId}
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "nom": "Benali",
  "prenom": "Nadia",
  "email": "manager1@itsm.com",
  "motDePasse": "newpassword123",
  "localisation": "Paris - Bureau 205",
  "telephone": "+33 1 42 56 78 90",
  "specialite": "Management IT",
  "teamName": "Équipe Infrastructure",
  "teamDescription": "Équipe infrastructure et sécurité",
  "teamCategories": ["INFRASTRUCTURE", "SECURITE", "RESEAU"]
}
```

### 2.5 Delete Manager (ADMIN only) - Soft Delete
```bash
DELETE http://localhost:8082/api/admin/managers/{managerId}
Authorization: Bearer {admin-token}
```

### 2.6 Reactivate Manager (ADMIN only) - Undo Soft Delete
```bash
POST http://localhost:8082/api/admin/managers/{managerId}/reactivate
Authorization: Bearer {admin-token}
```

**Response:**
```json
{
  "message": "Manager réactivé avec succès dans les deux bases de données",
  "managerId": "uuid-here"
}
```

## Phase 3: Technician CRUD (Manager Authorization)

### 3.1 Create Technician with Competences (MANAGER only)
```bash
POST http://localhost:8082/api/manager/technicians
Authorization: Bearer {manager-token}
Content-Type: application/json

{
  "nom": "Moreau",
  "prenom": "Julien",
  "email": "technicien2@itsm.com",
  "motDePasse": "devtech2025",
  "localisation": "Toulouse",
  "telephone": "+33 5 62 34 56 78",
  "specialite": "Intégration Continue et Déploiement",
  "competences": [
    {
      "nom": "Jenkins",
      "description": "Configuration de pipelines d'intégration continue",
      "categorie": "DEVOPS",
      "niveau": "AVANCE"
    },
    {
      "nom": "Docker",
      "description": "Création et gestion de conteneurs pour les applications",
      "categorie": "CLOUD",
      "niveau": "SENIOR"
    },
    {
      "nom": "Git",
      "description": "Utilisation avancée de Git pour le versionnement",
      "categorie": "DEVELOPPEMENT",
      "niveau": "AVANCE"
    }
  ]
}
```

### 3.2 Get All Technicians (ADMIN or MANAGER)
```bash
GET http://localhost:8082/api/manager/technicians
Authorization: Bearer {manager-token}
```

### 3.3 Get Technician by ID (ADMIN, MANAGER, or own profile)
```bash
GET http://localhost:8082/api/manager/technicians/{technicianId}
Authorization: Bearer {manager-token}
```

### 3.4 Update Technician (ADMIN or MANAGER)
```bash
PUT http://localhost:8082/api/manager/technicians/{technicianId}
Authorization: Bearer {manager-token}
Content-Type: application/json

{
  "nom": "Moreau",
  "prenom": "Julien", 
  "email": "technicien2@itsm.com",
  "motDePasse": "newtech2025",
  "localisation": "Paris",
  "telephone": "+33 1 45 67 89 01",
  "specialite": "DevOps et Cloud",
  "competences": [
    {
      "nom": "Kubernetes",
      "description": "Orchestration de conteneurs",
      "categorie": "CLOUD",
      "niveau": "EXPERT"
    },
    {
      "nom": "Terraform",
      "description": "Infrastructure as Code",
      "categorie": "DEVOPS",
      "niveau": "SENIOR"
    }
  ]
}
```

### 3.5 Delete Technician (ADMIN or MANAGER) - Soft Delete
```bash
DELETE http://localhost:8082/api/manager/technicians/{technicianId}
Authorization: Bearer {manager-token}
```

### 3.6 Reactivate Technician (ADMIN or MANAGER) - Undo Soft Delete
```bash
POST http://localhost:8082/api/manager/technicians/{technicianId}/reactivate
Authorization: Bearer {manager-token}
```

**Response:**
```json
{
  "message": "Technician réactivé avec succès dans les deux bases de données",
  "technicianId": "uuid-here"
}
```

## Phase 4: Additional User Service Testing

### 4.1 Get User Profile
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
