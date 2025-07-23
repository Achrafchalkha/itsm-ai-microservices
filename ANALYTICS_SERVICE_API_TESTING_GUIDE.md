# 🧪 Analytics-Service - Guide de Test API

## 🎯 **Vue d'Ensemble**

Ce guide fournit des exemples complets pour tester toutes les APIs de l'analytics-service avec **Postman** ou **curl**.

### **🔗 URL de Base**
```
http://localhost:8086/api/analytics
```

### **🔐 Authentification**
Toutes les APIs (sauf health) nécessitent un token JWT dans l'en-tête :
```
Authorization: Bearer {jwt_token}
```

---

## 🔐 **1. Authentification**

### **Obtenir un Token JWT**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@itsm.com",
  "motDePasse": "admin123"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "utilisateur": {
    "id": "uuid",
    "email": "admin@itsm.com",
    "role": "ADMIN"
  }
}
```

---

## 🏥 **2. Health Check**

### **Vérification Santé Service**
```http
GET http://localhost:8086/api/analytics/health
```

**Réponse :**
```json
{
  "status": "UP",
  "service": "analytics-service",
  "timestamp": "2024-01-15T10:30:00",
  "version": "1.0.0"
}
```

### **Health Check Détaillé**
```http
GET http://localhost:8086/api/analytics/health/detailed
```

**Réponse :**
```json
{
  "status": "UP",
  "service": "analytics-service",
  "timestamp": "2024-01-15T10:30:00",
  "version": "1.0.0",
  "dependencies": {
    "database": "UP",
    "kafka": "UP",
    "ticket-service": "UP",
    "user-service": "UP",
    "assignment-service": "UP"
  }
}
```

---

## 👑 **3. APIs ADMIN**

### **📊 Dashboard ADMIN**
```http
GET http://localhost:8086/api/analytics/admin/dashboard?days=30
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "globalKPIs": {
    "totalTickets": 1250,
    "ticketsResolved": 1100,
    "resolutionRate": 88.0,
    "slaComplianceRate": 92.5,
    "averageSatisfactionScore": 4.2,
    "averageResolutionTime": 145.5
  },
  "slaOverview": {
    "breachedTicketsCount": 15,
    "approachingDeadlineCount": 8,
    "complianceRate": 92.5,
    "criticalAlertsCount": 3
  },
  "teamPerformance": [
    {
      "teamId": "uuid",
      "teamName": "Équipe Réseau",
      "totalTickets": 85,
      "resolvedTickets": 78,
      "slaComplianceRate": 94.1,
      "averageSatisfactionScore": 4.3,
      "performanceLevel": "BON"
    }
  ],
  "criticalTicketsCount": 5,
  "approachingSLATicketsCount": 8
}
```

### **📈 KPIs Globaux**
```http
GET http://localhost:8086/api/analytics/admin/kpis/global?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "totalTickets": 1250,
  "ticketsResolved": 1100,
  "ticketsClosed": 1050,
  "resolutionRate": 88.0,
  "slaComplianceRate": 92.5,
  "averageResolutionTime": 145.5,
  "averageFirstResponseTime": 25.3,
  "averageSatisfactionScore": 4.2,
  "totalSatisfactionResponses": 890,
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  }
}
```

### **🎫 Tous les Tickets**
```http
GET http://localhost:8086/api/analytics/admin/tickets/all?page=0&size=20&status=OUVERT&priority=HAUTE
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
[
  {
    "id": "uuid",
    "titre": "Problème réseau urgent",
    "statut": "OUVERT",
    "priorite": "HAUTE",
    "categorie": "RESEAU",
    "dateCreation": "2024-01-15T09:30:00",
    "technicienId": "uuid",
    "teamId": "uuid",
    "slaDeadline": "2024-01-15T13:30:00",
    "timeRemainingMinutes": 120
  }
]
```

### **🚨 Tickets SLA en Retard**
```http
GET http://localhost:8086/api/analytics/admin/tickets/sla-breached
Authorization: Bearer {jwt_token}
```

### **⏰ Tickets Approchant SLA**
```http
GET http://localhost:8086/api/analytics/admin/tickets/sla-approaching?hoursBeforeDeadline=4
Authorization: Bearer {jwt_token}
```

### **📊 Statistiques Volume**
```http
GET http://localhost:8086/api/analytics/admin/stats/volume?startDate=2024-01-01&endDate=2024-01-31&groupBy=daily
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "totalTickets": 1250,
  "resolvedTickets": 1100,
  "openTickets": 150,
  "closedTickets": 1050,
  "groupBy": "daily",
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  },
  "volumeData": [
    {
      "date": "2024-01-01",
      "label": "01 Jan",
      "created": 45,
      "resolved": 38,
      "closed": 35
    }
  ]
}
```

### **⭐ Statistiques Satisfaction**
```http
GET http://localhost:8086/api/analytics/admin/stats/satisfaction?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "averageScore": 4.2,
  "totalResponses": 890,
  "responseRate": 78.5,
  "distribution": {
    "1": 15,
    "2": 25,
    "3": 120,
    "4": 380,
    "5": 350
  },
  "positiveCount": 730,
  "neutralCount": 120,
  "negativeCount": 40,
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  }
}
```

---

## 👥 **4. APIs MANAGER**

### **📊 Dashboard MANAGER**
```http
GET http://localhost:8086/api/analytics/manager/dashboard?days=30
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "teamId": "uuid",
  "teamName": "Équipe Réseau",
  "teamKPIs": {
    "totalTickets": 85,
    "resolvedTickets": 78,
    "resolutionRate": 91.8,
    "slaComplianceRate": 94.1,
    "averageResolutionTime": 145.5,
    "averageSatisfactionScore": 4.3,
    "performanceLevel": "BON"
  },
  "technicianPerformance": [
    {
      "technicianId": "uuid",
      "nom": "Dupont",
      "prenom": "Jean",
      "totalTickets": 25,
      "resolvedTickets": 23,
      "currentWorkload": 5,
      "slaComplianceRate": 96.0,
      "averageSatisfactionScore": 4.4,
      "performanceLevel": "EXCELLENT"
    }
  ],
  "workloadOverview": {
    "totalTechnicians": 8,
    "averageWorkload": 6.2,
    "maxWorkload": 12,
    "minWorkload": 2,
    "technicianWorkloads": [...]
  },
  "slaAlerts": [...]
}
```

### **📈 KPIs Équipe**
```http
GET http://localhost:8086/api/analytics/manager/kpis?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

### **🎫 Tickets Équipe**
```http
GET http://localhost:8086/api/analytics/manager/tickets?page=0&size=20&status=EN_COURS&technicianId=uuid
Authorization: Bearer {jwt_token}
```

### **👤 Performance Techniciens**
```http
GET http://localhost:8086/api/analytics/manager/technicians/performance?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
[
  {
    "technicianId": "uuid",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@itsm.com",
    "totalTicketsAssigned": 25,
    "totalTicketsResolved": 23,
    "currentWorkload": 5,
    "averageResolutionTime": 135.2,
    "slaComplianceRate": 96.0,
    "averageSatisfactionScore": 4.4,
    "performanceLevel": "EXCELLENT",
    "teamRanking": 1
  }
]
```

### **⚖️ Charge de Travail**
```http
GET http://localhost:8086/api/analytics/manager/workload
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "totalTechnicians": 8,
  "averageWorkload": 6.2,
  "maxWorkload": 12,
  "minWorkload": 2,
  "technicianWorkloads": [
    {
      "technicianId": "uuid",
      "nom": "Dupont",
      "prenom": "Jean",
      "currentWorkload": 5
    }
  ]
}
```

---

## ⚙️ **5. Configuration SLA**

### **Créer Configuration SLA**
```http
POST http://localhost:8086/api/analytics/sla-configurations
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "categorie": "RESEAU",
  "priorite": "URGENTE",
  "delaiPremiereReponseHeures": 1,
  "delaiResolutionHeures": 4,
  "escaladeManagerHeures": 2,
  "escaladeAdminHeures": 3
}
```

**Réponse :**
```json
{
  "id": "uuid",
  "categorie": "RESEAU",
  "priorite": "URGENTE",
  "delaiPremiereReponseHeures": 1,
  "delaiResolutionHeures": 4,
  "escaladeManagerHeures": 2,
  "escaladeAdminHeures": 3,
  "actif": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

### **Modifier Configuration SLA**
```http
PUT http://localhost:8086/api/analytics/sla-configurations/{configId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "delaiPremiereReponseHeures": 2,
  "delaiResolutionHeures": 6,
  "escaladeManagerHeures": 3,
  "escaladeAdminHeures": 4
}
```

### **Lister Configurations SLA**
```http
GET http://localhost:8086/api/analytics/sla-configurations
Authorization: Bearer {jwt_token}
```

### **Configuration par Catégorie**
```http
GET http://localhost:8086/api/analytics/sla-configurations/category/RESEAU
Authorization: Bearer {jwt_token}
```

### **Activer/Désactiver Configuration**
```http
PUT http://localhost:8086/api/analytics/sla-configurations/{configId}/activate
Authorization: Bearer {jwt_token}

PUT http://localhost:8086/api/analytics/sla-configurations/{configId}/deactivate
Authorization: Bearer {jwt_token}
```

---

## ⭐ **6. Satisfaction**

### **Créer Score Satisfaction**
```http
POST http://localhost:8086/api/analytics/satisfaction
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "ticketId": "uuid-ticket",
  "technicienId": "uuid-technician",
  "teamId": "uuid-team",
  "score": 4,
  "commentaire": "Excellent service, résolution rapide"
}
```

**Réponse :**
```json
{
  "id": "uuid",
  "ticketId": "uuid-ticket",
  "utilisateurId": "uuid-user",
  "technicienId": "uuid-technician",
  "teamId": "uuid-team",
  "score": 4,
  "commentaire": "Excellent service, résolution rapide",
  "satisfactionLevel": "POSITIF",
  "createdAt": "2024-01-15T10:30:00"
}
```

### **Mettre à Jour Détails Satisfaction**
```http
PUT http://localhost:8086/api/analytics/satisfaction/{satisfactionId}/details
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "tempsResolutionSatisfaisant": true,
  "qualiteCommunicationScore": 4,
  "competenceTechniqueScore": 5
}
```

### **Satisfaction par Ticket**
```http
GET http://localhost:8086/api/analytics/satisfaction/ticket/{ticketId}
Authorization: Bearer {jwt_token}
```

### **Satisfaction par Technicien**
```http
GET http://localhost:8086/api/analytics/satisfaction/technician/{technicienId}?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

### **Satisfaction par Équipe**
```http
GET http://localhost:8086/api/analytics/satisfaction/team/{teamId}?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

### **Moyenne Satisfaction Technicien**
```http
GET http://localhost:8086/api/analytics/satisfaction/technician/{technicienId}/average?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "technicienId": "uuid",
  "averageScore": 4.2,
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  }
}
```

### **Distribution Satisfaction**
```http
GET http://localhost:8086/api/analytics/satisfaction/distribution?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {jwt_token}
```

**Réponse :**
```json
{
  "distribution": {
    "1": 5,
    "2": 8,
    "3": 25,
    "4": 45,
    "5": 35
  },
  "period": {
    "start": "2024-01-01",
    "end": "2024-01-31"
  }
}
```

---

## 🧪 **7. Collection Postman**

### **Variables d'Environnement**
```json
{
  "analytics_base_url": "http://localhost:8086/api/analytics",
  "auth_base_url": "http://localhost:8080/api/auth",
  "jwt_token": "{{token_from_login}}",
  "admin_email": "admin@itsm.com",
  "admin_password": "admin123",
  "manager_email": "manager@itsm.com",
  "manager_password": "manager123"
}
```

### **Script Pre-request (Authentification Auto)**
```javascript
// Script à ajouter dans Pre-request Script de la collection
if (!pm.environment.get("jwt_token") || pm.environment.get("token_expired")) {
    pm.sendRequest({
        url: pm.environment.get("auth_base_url") + "/login",
        method: 'POST',
        header: {
            'Content-Type': 'application/json'
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                email: pm.environment.get("admin_email"),
                motDePasse: pm.environment.get("admin_password")
            })
        }
    }, function (err, response) {
        if (response.code === 200) {
            const jsonData = response.json();
            pm.environment.set("jwt_token", jsonData.token);
            pm.environment.unset("token_expired");
        }
    });
}
```

---

## ✅ **8. Tests de Validation**

### **Scénarios de Test Complets**

#### **🔐 Test Sécurité**
1. Accès sans token → 401 Unauthorized
2. Accès avec token invalide → 401 Unauthorized  
3. Accès MANAGER aux APIs ADMIN → 403 Forbidden
4. Accès UTILISATEUR aux APIs MANAGER → 403 Forbidden

#### **📊 Test Fonctionnel**
1. Créer configuration SLA → 201 Created
2. Récupérer dashboard ADMIN → 200 OK avec données
3. Récupérer dashboard MANAGER → 200 OK avec données équipe
4. Créer score satisfaction → 201 Created
5. Calculer KPIs → 200 OK avec métriques

#### **⚡ Test Performance**
1. Dashboard avec 30 jours de données < 2s
2. KPIs globaux < 1s
3. Satisfaction par technicien < 500ms
4. Health check < 100ms

---

## 🎯 **Résumé**

L'analytics-service fournit **30+ endpoints** couvrant :

- ✅ **Dashboards** ADMIN et MANAGER complets
- ✅ **KPIs** globaux et par équipe
- ✅ **Configuration SLA** dynamique
- ✅ **Satisfaction** avec scoring détaillé
- ✅ **Statistiques** volume et performance
- ✅ **Sécurité** par rôles JWT
- ✅ **APIs RESTful** documentées

**Toutes les APIs sont testées et prêtes pour production !**
