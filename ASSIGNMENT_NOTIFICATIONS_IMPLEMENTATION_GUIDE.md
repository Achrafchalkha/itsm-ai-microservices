# 🚀 Assignment-Service & Notifications-Service Implementation Guide

## 📋 **Vue d'Ensemble**

Cette implémentation ajoute deux nouveaux microservices à votre système ITSM :

### **🤖 Assignment-Service (Port 8084)**
- **Assignation intelligente** des tickets avec IA Gemini
- **Stratégies multiples** : Charge de travail, Compétences, Hybride
- **Intégration Kafka** pour événements temps réel
- **APIs REST** pour assignation manuelle et statistiques

### **🔔 Notifications-Service (Port 8085)**
- **Notifications temps réel** via WebSocket
- **Emails automatiques** avec templates HTML
- **Préférences utilisateur** personnalisables
- **Dashboard** de notifications

---

## 🏗️ **Architecture Technique**

### **Flux d'Assignation Intelligent**

```mermaid
graph TD
    A[Ticket Créé] --> B[Assignment-Service]
    B --> C{Équipes Disponibles?}
    C -->|Oui| D[Filtrage par Catégorie]
    C -->|Non| E[Échec d'Assignation]
    D --> F[Techniciens Actifs]
    F --> G{NLP Activé?}
    G -->|Oui| H[Analyse Gemini AI]
    G -->|Non| I[Stratégie Simple]
    H --> J[Score Hybride]
    I --> J
    J --> K[Assignation Créée]
    K --> L[Notification Envoyée]
    E --> M[Notification Manager]
```

### **Bases de Données**

#### **assignment_db**
- `assignments` - Assignations principales
- `assignment_history` - Historique des réassignations
- `assignment_metrics` - Métriques pour analytics

#### **notifications_db**
- `notifications` - Notifications utilisateur
- `notification_preferences` - Préférences par utilisateur
- `email_delivery_log` - Logs d'envoi email
- `notification_templates` - Templates d'email

---

## 🚀 **Installation et Configuration**

### **1. Créer les Bases de Données**

```bash
# Assignment Database
psql -U postgres -f assignment-service/create-assignment-database.sql

# Notifications Database
psql -U postgres -f notifications-service/create-notifications-database.sql
```

### **2. Configuration des Services**

#### **Assignment-Service (application.properties)**
```properties
# Port et nom du service
spring.application.name=assignment-service
server.port=8084

# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/assignment_db
spring.datasource.username=postgres
spring.datasource.password=your_database_password

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=assignment-service-group

# Gemini AI (utilise la même clé que ticket-service)
app.nlp.enabled=true
app.nlp.gemini.api-key=${GEMINI_API_KEY:AIzaSyAgS3sVnW7vtJVHizPs26NA9Rp9HlkJgj8}

# Configuration d'assignation
assignment.default.strategy=HYBRID
assignment.workload.max-per-technician=5
assignment.confidence.threshold=0.6
```

#### **Notifications-Service (application.properties)**
```properties
# Port et nom du service
spring.application.name=notifications-service
server.port=8085

# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/notifications_db

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
notifications.email.from=noreply@itsm.com

# WebSocket
notifications.websocket.enabled=true
notifications.websocket.endpoint=/ws/notifications
```

### **3. Démarrage des Services**

```bash
# 1. Démarrer Assignment-Service
cd assignment-service
mvn spring-boot:run

# 2. Démarrer Notifications-Service
cd notifications-service
mvn spring-boot:run
```

---

## 🔧 **APIs REST Disponibles**

### **Assignment-Service APIs**

#### **Assignation Manuelle**
```http
POST /api/assignments/manual
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "ticketId": "uuid",
  "technicianId": "uuid", 
  "assignedBy": "uuid",
  "reason": "Expertise spécifique requise"
}
```

#### **Réassignation**
```http
PUT /api/assignments/{assignmentId}/reassign
Authorization: Bearer <JWT_TOKEN>

{
  "newTechnicianId": "uuid",
  "reassignedBy": "uuid",
  "reason": "Équilibrage de charge"
}
```

#### **Statistiques d'Assignation**
```http
GET /api/assignments/stats
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "totalAssignments": 150,
  "activeAssignments": 45,
  "averageConfidenceScore": 0.78,
  "strategyDistribution": {
    "HYBRID": {"count": 100, "avgConfidence": 0.82},
    "LEAST_WORKLOAD": {"count": 30, "avgConfidence": 0.65}
  }
}
```

### **Notifications-Service APIs**

#### **Notifications Utilisateur**
```http
GET /api/notifications?unreadOnly=true&limit=20
Authorization: Bearer <JWT_TOKEN>

Response:
[
  {
    "id": "uuid",
    "type": "TICKET_ASSIGNED",
    "title": "Ticket #abc123 assigned to you",
    "message": "You have been assigned to ticket: Network Issue",
    "priority": "HIGH",
    "readStatus": false,
    "createdAt": "2024-01-15T10:30:00",
    "ticketId": "uuid"
  }
]
```

#### **Marquer comme Lu**
```http
PUT /api/notifications/{notificationId}/read
Authorization: Bearer <JWT_TOKEN>
```

#### **Préférences de Notification**
```http
GET /api/notifications/preferences
PUT /api/notifications/preferences

{
  "emailEnabled": true,
  "dashboardEnabled": true,
  "emailAddress": "user@company.com",
  "ticketAssignedEmail": true,
  "ticketReassignedEmail": true,
  "assignmentFailedEmail": true
}
```

---

## 🧠 **Intelligence Artificielle avec Gemini**

### **Analyse NLP des Tickets**

L'assignment-service utilise Gemini AI pour :

1. **Analyser la description** du ticket
2. **Extraire les technologies** mentionnées
3. **Identifier les compétences** requises
4. **Évaluer la complexité** et l'urgence
5. **Scorer les techniciens** selon leurs compétences

### **Exemple d'Analyse Gemini**

**Input :** "Problème de connectivité réseau sur le serveur web Apache. Erreur 500 intermittente."

**Output Gemini :**
```json
{
  "detectedTechnologies": ["Apache", "HTTP", "Réseau"],
  "requiredCompetences": ["Administration Web", "Diagnostic Réseau"],
  "complexityLevel": "MEDIUM",
  "urgencyLevel": "HIGH",
  "technicianScores": {
    "tech-uuid-1": 0.92,
    "tech-uuid-2": 0.75
  },
  "reasoning": "Technicien 1 a une expertise Apache et réseau élevée"
}
```

---

## 📧 **Système de Notifications**

### **Types de Notifications**

| Type | Description | Email par Défaut | Dashboard |
|------|-------------|------------------|-----------|
| `TICKET_ASSIGNED` | Assignation de ticket | ✅ | ✅ |
| `TICKET_REASSIGNED` | Réassignation | ✅ | ✅ |
| `ASSIGNMENT_FAILED` | Échec d'assignation | ✅ | ✅ |
| `SLA_WARNING` | Alerte SLA | ✅ | ✅ |
| `TICKET_UPDATED` | Mise à jour ticket | ❌ | ✅ |

### **Templates Email**

Les emails utilisent des templates Thymeleaf professionnels :
- **assignment-notification.html** - Assignation de ticket
- **reassignment-notification.html** - Réassignation
- **assignment-failure-notification.html** - Échec (pour managers)

### **WebSocket Temps Réel**

```javascript
// Connexion WebSocket pour notifications temps réel
const socket = new SockJS('/ws/notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // S'abonner aux notifications utilisateur
    stompClient.subscribe('/topic/notifications/' + userId, function(notification) {
        const data = JSON.parse(notification.body);
        displayNotification(data);
    });
});
```

---

## 🔄 **Intégration Kafka**

### **Topics Kafka**

| Topic | Producer | Consumer | Description |
|-------|----------|----------|-------------|
| `ticket.created` | ticket-service | assignment-service | Nouveau ticket créé |
| `assignment.created` | assignment-service | notifications-service | Assignation créée |
| `assignment.reassigned` | assignment-service | notifications-service | Réassignation |
| `assignment.failed` | assignment-service | notifications-service | Échec d'assignation |

### **Flux d'Événements**

1. **Ticket créé** → `ticket.created` event
2. **Assignment-service** traite l'événement
3. **Assignation réussie** → `assignment.created` event
4. **Notifications-service** crée la notification
5. **Email envoyé** + **WebSocket notification**

---

## 📊 **Monitoring et Métriques**

### **Endpoints Actuator**

```http
# Health check
GET /actuator/health

# Métriques
GET /actuator/metrics

# Info service
GET /actuator/info
```

### **Métriques Clés**

- **Taux de réussite d'assignation**
- **Temps moyen d'assignation**
- **Score de confiance moyen**
- **Distribution des stratégies**
- **Charge de travail par technicien**

---

## 🧪 **Tests et Validation**

### **Test du Flux Complet**

1. **Créer un ticket** via ticket-service
2. **Vérifier l'assignation** automatique
3. **Contrôler la notification** reçue
4. **Tester la réassignation** manuelle

### **Test des Notifications**

```bash
# Test email
curl -X POST http://localhost:8085/api/test/email \
  -H "Content-Type: application/json" \
  -d '{"email": "test@company.com"}'

# Test WebSocket
# Connecter via navigateur à ws://localhost:8085/ws/notifications
```

---

## 🚨 **Dépannage**

### **Problèmes Courants**

#### **Assignation ne fonctionne pas**
- Vérifier que Kafka est démarré
- Contrôler les logs d'assignment-service
- Vérifier la configuration Gemini API

#### **Emails non envoyés**
- Vérifier la configuration SMTP
- Contrôler les logs de notifications-service
- Tester avec `sendTestEmail()`

#### **WebSocket ne se connecte pas**
- Vérifier la configuration CORS
- Contrôler les logs WebSocket
- Tester la connectivité réseau

### **Logs Utiles**

```bash
# Assignment-service logs
tail -f assignment-service/logs/application.log | grep "Assignment"

# Notifications-service logs  
tail -f notifications-service/logs/application.log | grep "Notification"

# Kafka logs
tail -f kafka/logs/server.log
```

---

## 🎯 **Prochaines Étapes**

1. **Analytics-Service** - Tableaux de bord et KPI
2. **Chatbot-Service** - Assistant IA pour utilisateurs
3. **Mobile App** - Application mobile pour techniciens
4. **Advanced AI** - Prédiction de résolution et recommandations

---

## 📞 **Support**

Pour toute question ou problème :
1. Consulter les logs des services
2. Vérifier la configuration Kafka et bases de données
3. Tester les endpoints individuellement
4. Valider l'intégration Gemini AI

**L'implémentation est maintenant complète et prête pour la production !** 🎉
