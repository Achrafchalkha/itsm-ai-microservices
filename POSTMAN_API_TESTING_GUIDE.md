# 📮 Guide de Test API - Assignment & Notifications Services

## 🚀 **Configuration Postman**

### **Variables d'Environnement**

Créez un environnement Postman avec ces variables :

```json
{
  "assignment_service_url": "http://localhost:8084",
  "notifications_service_url": "http://localhost:8085",
  "jwt_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user_id": "123e4567-e89b-12d3-a456-426614174000",
  "ticket_id": "550e8400-e29b-41d4-a716-446655440000",
  "technician_id": "660e8400-e29b-41d4-a716-446655440001"
}
```

---

## 🤖 **Assignment-Service APIs**

### **1. Assignation Manuelle**

```http
POST {{assignment_service_url}}/api/assignments/manual
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "ticketId": "{{ticket_id}}",
  "technicianId": "{{technician_id}}",
  "assignedBy": "{{user_id}}",
  "reason": "Expertise spécifique en réseau requise"
}
```

**Réponse Attendue :**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "ticketId": "550e8400-e29b-41d4-a716-446655440000",
  "technicianId": "660e8400-e29b-41d4-a716-446655440001",
  "teamId": "880e8400-e29b-41d4-a716-446655440003",
  "strategy": "BEST_SKILL",
  "confidenceScore": 1.0,
  "assignmentReason": "Manual assignment: Expertise spécifique en réseau requise",
  "assignedAt": "2024-01-15T10:30:00",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00"
}
```

### **2. Réassignation de Ticket**

```http
PUT {{assignment_service_url}}/api/assignments/770e8400-e29b-41d4-a716-446655440002/reassign
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "newTechnicianId": "990e8400-e29b-41d4-a716-446655440004",
  "reassignedBy": "{{user_id}}",
  "reason": "Équilibrage de la charge de travail"
}
```

### **3. Consulter Assignation par Ticket**

```http
GET {{assignment_service_url}}/api/assignments/ticket/{{ticket_id}}
Authorization: Bearer {{jwt_token}}
```

### **4. Assignations d'un Technicien**

```http
GET {{assignment_service_url}}/api/assignments/technician/{{technician_id}}?page=0&size=10
Authorization: Bearer {{jwt_token}}
```

### **5. Assignations d'une Équipe**

```http
GET {{assignment_service_url}}/api/assignments/team/880e8400-e29b-41d4-a716-446655440003?page=0&size=20
Authorization: Bearer {{jwt_token}}
```

### **6. Statistiques d'Assignation**

```http
GET {{assignment_service_url}}/api/assignments/stats
Authorization: Bearer {{jwt_token}}
```

**Réponse Attendue :**
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
    "BEST_SKILL": {
      "count": 20,
      "avgConfidence": 0.91
    }
  },
  "technicianWorkload": {
    "660e8400-e29b-41d4-a716-446655440001": 3,
    "990e8400-e29b-41d4-a716-446655440004": 5
  }
}
```

### **7. Assignations Nécessitant Attention**

```http
GET {{assignment_service_url}}/api/assignments/attention
Authorization: Bearer {{jwt_token}}
```

---

## 🔔 **Notifications-Service APIs**

### **1. Notifications Utilisateur**

#### **Toutes les Notifications**
```http
GET {{notifications_service_url}}/api/notifications?unreadOnly=false&limit=50
Authorization: Bearer {{jwt_token}}
```

#### **Notifications Non Lues Uniquement**
```http
GET {{notifications_service_url}}/api/notifications?unreadOnly=true&limit=20
Authorization: Bearer {{jwt_token}}
```

**Réponse Attendue :**
```json
[
  {
    "id": "aa0e8400-e29b-41d4-a716-446655440005",
    "type": "TICKET_ASSIGNED",
    "title": "Ticket #550e8400 assigned to you",
    "message": "You have been assigned to ticket: Problème réseau serveur web",
    "priority": "HIGH",
    "channel": "BOTH",
    "readStatus": false,
    "createdAt": "2024-01-15T10:30:00",
    "ticketId": "550e8400-e29b-41d4-a716-446655440000",
    "assignmentId": "770e8400-e29b-41d4-a716-446655440002",
    "data": {
      "assignmentStrategy": "HYBRID",
      "confidenceScore": 0.85,
      "ticketCategory": "RESEAU"
    }
  }
]
```

### **2. Nombre de Notifications Non Lues**

```http
GET {{notifications_service_url}}/api/notifications/count/unread
Authorization: Bearer {{jwt_token}}
```

**Réponse :**
```json
{
  "count": 5
}
```

### **3. Marquer Notification comme Lue**

```http
PUT {{notifications_service_url}}/api/notifications/aa0e8400-e29b-41d4-a716-446655440005/read
Authorization: Bearer {{jwt_token}}
```

### **4. Marquer Toutes comme Lues**

```http
PUT {{notifications_service_url}}/api/notifications/read-all
Authorization: Bearer {{jwt_token}}
```

### **5. Notifications Paginées**

```http
GET {{notifications_service_url}}/api/notifications/paginated?page=0&size=10&unreadOnly=false
Authorization: Bearer {{jwt_token}}
```

### **6. Notifications par Type**

```http
GET {{notifications_service_url}}/api/notifications/type/TICKET_ASSIGNED?limit=10
Authorization: Bearer {{jwt_token}}
```

---

## ⚙️ **Préférences de Notification**

### **1. Consulter Préférences**

```http
GET {{notifications_service_url}}/api/notifications/preferences
Authorization: Bearer {{jwt_token}}
```

**Réponse :**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "emailEnabled": true,
  "dashboardEnabled": true,
  "emailAddress": "user@company.com",
  "ticketAssignedEmail": true,
  "ticketReassignedEmail": true,
  "ticketUpdatedEmail": false,
  "assignmentFailedEmail": true,
  "slaWarningEmail": true,
  "teamMemberAddedEmail": false,
  "ticketAssignedDashboard": true,
  "ticketReassignedDashboard": true,
  "ticketUpdatedDashboard": true,
  "assignmentFailedDashboard": true,
  "slaWarningDashboard": true,
  "teamMemberAddedDashboard": true,
  "createdAt": "2024-01-15T09:00:00",
  "updatedAt": "2024-01-15T10:00:00"
}
```

### **2. Mettre à Jour Préférences**

```http
PUT {{notifications_service_url}}/api/notifications/preferences
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "emailEnabled": true,
  "dashboardEnabled": true,
  "emailAddress": "newemail@company.com",
  "ticketAssignedEmail": true,
  "ticketReassignedEmail": true,
  "ticketUpdatedEmail": true,
  "assignmentFailedEmail": true,
  "slaWarningEmail": true,
  "teamMemberAddedEmail": false,
  "ticketAssignedDashboard": true,
  "ticketReassignedDashboard": true,
  "ticketUpdatedDashboard": true,
  "assignmentFailedDashboard": true,
  "slaWarningDashboard": true,
  "teamMemberAddedDashboard": true
}
```

### **3. Mettre à Jour Email**

```http
PUT {{notifications_service_url}}/api/notifications/preferences/email
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "emailAddress": "updated@company.com"
}
```

### **4. Activer/Désactiver Email**

```http
PUT {{notifications_service_url}}/api/notifications/preferences/email/enabled
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "enabled": false
}
```

### **5. Activer/Désactiver Dashboard**

```http
PUT {{notifications_service_url}}/api/notifications/preferences/dashboard/enabled
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "enabled": true
}
```

### **6. Préférences par Type**

```http
PUT {{notifications_service_url}}/api/notifications/preferences/type/TICKET_ASSIGNED
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "emailEnabled": true,
  "dashboardEnabled": true
}
```

---

## 🧪 **Tests de Flux Complet**

### **Scénario 1 : Assignation Automatique**

1. **Créer un ticket** (via ticket-service)
2. **Vérifier l'assignation** automatique
3. **Contrôler la notification** reçue

```http
# 1. Créer ticket (ticket-service)
POST http://localhost:8083/api/tickets
{
  "titre": "Problème réseau critique",
  "description": "Serveur Apache inaccessible, erreurs 500",
  "categorie": "RESEAU",
  "priorite": "HAUTE",
  "enableNlp": true
}

# 2. Vérifier assignation (assignment-service)
GET {{assignment_service_url}}/api/assignments/ticket/{{ticket_id}}

# 3. Vérifier notification (notifications-service)
GET {{notifications_service_url}}/api/notifications?unreadOnly=true
```

### **Scénario 2 : Réassignation Manuelle**

```http
# 1. Réassigner ticket
PUT {{assignment_service_url}}/api/assignments/{{assignment_id}}/reassign
{
  "newTechnicianId": "new-tech-uuid",
  "reassignedBy": "{{user_id}}",
  "reason": "Expertise spécialisée requise"
}

# 2. Vérifier notifications de réassignation
GET {{notifications_service_url}}/api/notifications/type/TICKET_REASSIGNED
```

---

## 🔍 **Tests de Santé et Monitoring**

### **Health Checks**

```http
# Assignment-Service Health
GET {{assignment_service_url}}/actuator/health

# Notifications-Service Health  
GET {{notifications_service_url}}/actuator/health
```

### **Métriques**

```http
# Assignment-Service Metrics
GET {{assignment_service_url}}/actuator/metrics

# Notifications-Service Metrics
GET {{notifications_service_url}}/actuator/metrics
```

---

## 🚨 **Tests d'Erreur**

### **Assignation avec Ticket Inexistant**

```http
POST {{assignment_service_url}}/api/assignments/manual
{
  "ticketId": "00000000-0000-0000-0000-000000000000",
  "technicianId": "{{technician_id}}",
  "assignedBy": "{{user_id}}",
  "reason": "Test erreur"
}
```

**Réponse Attendue :** `400 Bad Request`

### **Notification Inexistante**

```http
PUT {{notifications_service_url}}/api/notifications/00000000-0000-0000-0000-000000000000/read
```

**Réponse Attendue :** `404 Not Found`

---

## 📊 **Collection Postman Complète**

Importez cette collection JSON dans Postman :

```json
{
  "info": {
    "name": "ITSM Assignment & Notifications API",
    "description": "Tests complets pour assignment-service et notifications-service"
  },
  "variable": [
    {
      "key": "assignment_service_url",
      "value": "http://localhost:8084"
    },
    {
      "key": "notifications_service_url", 
      "value": "http://localhost:8085"
    }
  ],
  "item": [
    {
      "name": "Assignment Service",
      "item": [
        {
          "name": "Manual Assignment",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwt_token}}"
              }
            ],
            "url": "{{assignment_service_url}}/api/assignments/manual"
          }
        }
      ]
    }
  ]
}
```

---

## ✅ **Checklist de Test**

### **Assignment-Service**
- [ ] Assignation manuelle fonctionne
- [ ] Réassignation fonctionne  
- [ ] Statistiques retournées
- [ ] Gestion d'erreurs appropriée
- [ ] Kafka events publiés

### **Notifications-Service**
- [ ] Notifications créées automatiquement
- [ ] Préférences modifiables
- [ ] Emails envoyés (si configuré)
- [ ] WebSocket fonctionne
- [ ] Pagination correcte

### **Intégration**
- [ ] Flux ticket → assignation → notification
- [ ] Événements Kafka transmis
- [ ] Données cohérentes entre services
- [ ] Performance acceptable

**Tous les tests passent ? Votre implémentation est prête ! 🎉**
