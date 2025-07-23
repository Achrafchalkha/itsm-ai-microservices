# 📊 Analytics-Service - Guide Complet

## 🎯 **Vue d'Ensemble**

L'**analytics-service** est le service central d'analyse et de reporting du système ITSM. Il fournit des tableaux de bord complets, des KPIs en temps réel, et des fonctionnalités d'analyse avancées pour les rôles **ADMIN** et **MANAGER**.

### **🏗️ Architecture**

- **Port** : 8086
- **Base de données** : analytics_db (PostgreSQL)
- **Architecture** : Domain-Driven Design (DDD)
- **Sécurité** : JWT avec contrôle d'accès basé sur les rôles
- **Intégration** : Kafka pour analytics temps réel + clients REST

---

## 📋 **Fonctionnalités Complètes**

### **🔷 Dashboard ADMIN - Supervision Globale**

#### **✅ Superviser tous les tickets**
```http
GET /api/analytics/admin/tickets/all
```
- Consultation tous tickets, toutes équipes confondues
- Filtrage par statut, priorité, catégorie
- Pagination et recherche avancée
- Intervention sur tickets bloqués

#### **✅ Superviser les SLA globalement**
```http
GET /api/analytics/admin/tickets/sla-breached
GET /api/analytics/admin/tickets/sla-approaching
```
- Tickets en retard et approchant limite
- Configuration SLA par catégorie/priorité
- Alertes automatiques et escalade

#### **✅ Suivre les KPI globaux**
```http
GET /api/analytics/admin/dashboard
GET /api/analytics/admin/kpis/global
GET /api/analytics/admin/stats/volume
GET /api/analytics/admin/stats/satisfaction
```
- Volume tickets par mois
- Taux de satisfaction global
- Pourcentage tickets résolus dans SLA
- Charges par équipe et technicien

### **🔷 Dashboard MANAGER - Gestion d'Équipe**

#### **✅ Consulter les tickets de son équipe**
```http
GET /api/analytics/manager/tickets
```
- Liste tickets ouverts/en cours/fermés de l'équipe
- Filtrage par technicien, priorité, statut
- Vue détaillée performance équipe

#### **✅ Superviser le respect des SLA**
```http
GET /api/analytics/manager/dashboard
```
- Tickets approchant limite SLA
- Alertes dépassement délais
- Monitoring temps réel équipe

#### **✅ Suivre les KPI de son équipe**
```http
GET /api/analytics/manager/kpis
GET /api/analytics/manager/technicians/performance
GET /api/analytics/manager/workload
```
- **MTTR** (Mean Time To Resolution)
- Tickets résolus dans délais SLA
- Répartition tickets par catégorie
- Charge actuelle par technicien

---

## 🗄️ **Base de Données Analytics**

### **📊 Tables Principales**

#### **SLA Configuration**
```sql
CREATE TABLE sla_configurations (
    id UUID PRIMARY KEY,
    categorie VARCHAR(50) NOT NULL,
    priorite VARCHAR(20) NOT NULL,
    delai_premiere_reponse_heures INTEGER NOT NULL,
    delai_resolution_heures INTEGER NOT NULL,
    escalade_manager_heures INTEGER,
    escalade_admin_heures INTEGER,
    actif BOOLEAN DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(categorie, priorite)
);
```

#### **KPIs Journaliers**
```sql
CREATE TABLE daily_kpis (
    id UUID PRIMARY KEY,
    date_kpi DATE NOT NULL UNIQUE,
    total_tickets_created INTEGER DEFAULT 0,
    total_tickets_resolved INTEGER DEFAULT 0,
    tickets_within_sla INTEGER DEFAULT 0,
    tickets_breached_sla INTEGER DEFAULT 0,
    average_resolution_time_minutes DECIMAL(10,2),
    average_satisfaction_score DECIMAL(3,2),
    team_metrics_json TEXT,
    technician_metrics_json TEXT,
    category_metrics_json TEXT
);
```

#### **Scores de Satisfaction**
```sql
CREATE TABLE satisfaction_scores (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    utilisateur_id UUID NOT NULL,
    technicien_id UUID NOT NULL,
    team_id UUID NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    commentaire TEXT,
    temps_resolution_satisfaisant BOOLEAN,
    qualite_communication_score INTEGER,
    competence_technique_score INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### **Alertes SLA**
```sql
CREATE TABLE sla_alerts (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    sla_deadline TIMESTAMP NOT NULL,
    time_remaining_minutes INTEGER,
    escalated_to UUID,
    resolved BOOLEAN DEFAULT FALSE,
    resolution_action VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 🔧 **Services et Architecture**

### **📈 Services Métier**

#### **KPICalculationEngine**
```java
@Service
public class KPICalculationEngine {
    // Calcul KPIs journaliers
    public DailyKPI calculateDailyKPIs(LocalDate date);
    
    // Performance équipe
    public TeamPerformanceMetrics calculateTeamPerformance(UUID teamId, LocalDate start, LocalDate end);
    
    // KPIs globaux
    public GlobalKPIResult calculateGlobalKPIs(LocalDate start, LocalDate end);
    
    // MTTR et métriques
    public BigDecimal calculateMTTR(UUID teamId, LocalDate start, LocalDate end);
    public BigDecimal calculateSLAComplianceRate(UUID teamId, LocalDate start, LocalDate end);
}
```

#### **SLAConfigurationService**
```java
@Service
public class SLAConfigurationService {
    // Gestion configurations SLA
    public SLAConfiguration creerConfiguration(String categorie, String priorite, ...);
    public SLAConfiguration mettreAJourConfiguration(UUID configId, ...);
    
    // Calcul délais SLA
    public LocalDateTime calculerDateLimiteSLA(String categorie, String priorite, LocalDateTime creation);
    public LocalDateTime calculerDateLimitePremiereReponse(String categorie, String priorite, LocalDateTime creation);
    
    // Vérification escalade
    public boolean doitEscaladerManager(String categorie, String priorite, LocalDateTime creation);
    public boolean doitEscaladerAdmin(String categorie, String priorite, LocalDateTime creation);
}
```

#### **SatisfactionService**
```java
@Service
public class SatisfactionService {
    // Gestion satisfaction
    public SatisfactionScore createSatisfactionScore(UUID ticketId, UUID userId, ...);
    public SatisfactionScore updateSatisfactionScoreDetails(UUID satisfactionId, ...);
    
    // Calculs satisfaction
    public BigDecimal calculateAverageSatisfactionForTechnician(UUID technicienId, ...);
    public BigDecimal calculateAverageSatisfactionForTeam(UUID teamId, ...);
    public Map<Integer, Long> getSatisfactionDistribution(LocalDate start, LocalDate end);
}
```

### **🔄 Intégration Temps Réel**

#### **Kafka Event Listeners**
```java
@Component
public class TicketEventListener {
    @KafkaListener(topics = "ticket.created")
    public void handleTicketCreated(Map<String, Object> ticketData);
    
    @KafkaListener(topics = "ticket.resolved")
    public void handleTicketResolved(Map<String, Object> ticketData);
    
    @KafkaListener(topics = "ticket.sla.breached")
    public void handleTicketSLABreached(Map<String, Object> ticketData);
}

@Component
public class AssignmentEventListener {
    @KafkaListener(topics = "assignment.created")
    public void handleAssignmentCreated(Map<String, Object> assignmentData);
    
    @KafkaListener(topics = "assignment.reassigned")
    public void handleAssignmentReassigned(Map<String, Object> reassignmentData);
}
```

#### **Agrégation Planifiée**
```java
@Service
public class ScheduledAggregationService {
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void runDailyAggregation();
    
    @Scheduled(cron = "0 0 2 1 * ?") // Monthly on 1st at 2 AM
    public void runMonthlyAggregation();
    
    @Scheduled(fixedRate = 14400000) // Every 4 hours
    public void runSatisfactionAggregation();
}
```

---

## 🚀 **APIs REST Complètes**

### **🔷 APIs ADMIN**

#### **Dashboard Global**
```http
GET /api/analytics/admin/dashboard?days=30
```
**Response:**
```json
{
  "globalKPIs": {
    "totalTickets": 1250,
    "ticketsResolved": 1100,
    "resolutionRate": 88.0,
    "slaComplianceRate": 92.5,
    "averageSatisfactionScore": 4.2
  },
  "slaOverview": {
    "breachedTicketsCount": 15,
    "approachingDeadlineCount": 8,
    "complianceRate": 92.5
  },
  "teamPerformance": [...],
  "criticalTicketsCount": 5,
  "approachingSLATicketsCount": 8
}
```

#### **KPIs Globaux**
```http
GET /api/analytics/admin/kpis/global?startDate=2024-01-01&endDate=2024-01-31
```

#### **Supervision Tickets**
```http
GET /api/analytics/admin/tickets/all?page=0&size=50&status=OUVERT&priority=HAUTE
GET /api/analytics/admin/tickets/sla-breached
GET /api/analytics/admin/tickets/sla-approaching?hoursBeforeDeadline=4
```

#### **Statistiques**
```http
GET /api/analytics/admin/stats/volume?startDate=2024-01-01&endDate=2024-01-31&groupBy=daily
GET /api/analytics/admin/stats/satisfaction?startDate=2024-01-01&endDate=2024-01-31
```

### **🔷 APIs MANAGER**

#### **Dashboard Équipe**
```http
GET /api/analytics/manager/dashboard?days=30
```
**Response:**
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
  "technicianPerformance": [...],
  "workloadOverview": {...},
  "slaAlerts": [...]
}
```

#### **Performance Équipe**
```http
GET /api/analytics/manager/tickets?page=0&size=20&status=EN_COURS&technicianId=uuid
GET /api/analytics/manager/kpis?startDate=2024-01-01&endDate=2024-01-31
GET /api/analytics/manager/technicians/performance?startDate=2024-01-01&endDate=2024-01-31
GET /api/analytics/manager/workload
```

### **🔷 APIs Configuration SLA**

#### **Gestion Configurations**
```http
POST /api/analytics/sla-configurations
PUT /api/analytics/sla-configurations/{configId}
GET /api/analytics/sla-configurations
GET /api/analytics/sla-configurations/category/{category}
PUT /api/analytics/sla-configurations/{configId}/activate
PUT /api/analytics/sla-configurations/{configId}/deactivate
```

**Exemple Création SLA:**
```json
{
  "categorie": "RESEAU",
  "priorite": "URGENTE",
  "delaiPremiereReponseHeures": 1,
  "delaiResolutionHeures": 4,
  "escaladeManagerHeures": 2,
  "escaladeAdminHeures": 3
}
```

### **🔷 APIs Satisfaction**

#### **Gestion Satisfaction**
```http
POST /api/analytics/satisfaction
PUT /api/analytics/satisfaction/{satisfactionId}/details
GET /api/analytics/satisfaction/ticket/{ticketId}
GET /api/analytics/satisfaction/technician/{technicienId}?startDate=...&endDate=...
GET /api/analytics/satisfaction/team/{teamId}?startDate=...&endDate=...
GET /api/analytics/satisfaction/technician/{technicienId}/average?startDate=...&endDate=...
GET /api/analytics/satisfaction/distribution?startDate=...&endDate=...
```

**Exemple Création Satisfaction:**
```json
{
  "ticketId": "uuid",
  "technicienId": "uuid",
  "teamId": "uuid",
  "score": 4,
  "commentaire": "Résolution rapide et efficace"
}
```

---

## 📊 **KPIs et Métriques Calculés**

### **🌐 KPIs Globaux (ADMIN)**

#### **Volume et Performance**
- **Volume total tickets** par période
- **Taux résolution global** (tickets résolus / tickets créés)
- **Temps moyen résolution** (MTTR global)
- **Temps moyen première réponse**

#### **SLA et Qualité**
- **Taux conformité SLA global** (% tickets dans délais)
- **Tickets en retard** par catégorie/priorité
- **Escalades** manager/admin
- **Tendances SLA** dans le temps

#### **Satisfaction et Feedback**
- **Score satisfaction moyen** global
- **Taux réponse satisfaction** (% tickets évalués)
- **Distribution scores** (1-5 étoiles)
- **Satisfaction par équipe/technicien**

### **👥 KPIs Équipe (MANAGER)**

#### **Performance Équipe**
- **MTTR équipe** (Mean Time To Resolution)
- **Taux conformité SLA équipe**
- **Temps moyen première réponse équipe**
- **Taux réassignation** (% tickets réassignés)

#### **Charge et Répartition**
- **Distribution charge travail** par technicien
- **Équilibre charge** (écart min/max)
- **Charge moyenne** par technicien
- **Disponibilité équipe** (techniciens actifs)

#### **Qualité et Satisfaction**
- **Score satisfaction équipe**
- **Performance individuelle** techniciens
- **Répartition tickets** par catégorie
- **Évolution performance** dans le temps

### **👤 Métriques Technicien**

#### **Productivité**
- **Tickets assignés/résolus** par période
- **Temps résolution moyen** individuel
- **Charge actuelle** et historique
- **Taux activité** (% temps actif)

#### **Qualité**
- **Conformité SLA individuelle**
- **Score satisfaction reçu**
- **Confidence score** assignations IA
- **Taux réassignation** (tickets repris)

---

## 🔄 **Intégration Multi-Services**

### **📊 Données Agrégées**

#### **Depuis ticket-service**
- Tickets avec SLA tracking complet
- Métriques temps résolution/première réponse
- Statuts et workflow transitions
- Assignations et réassignations

#### **Depuis user-service**
- Équipes et hiérarchie manager-technicien
- Charge actuelle techniciens
- Compétences et spécialisations
- Relations équipe-catégories

#### **Depuis assignment-service**
- Métriques assignation IA
- Confidence scores et stratégies
- Historique réassignations
- Performance algorithmes

#### **Depuis notifications-service**
- Logs delivery notifications
- Préférences utilisateurs
- Historique communications
- Taux ouverture/réponse

---

## ⚡ **Analytics Temps Réel**

### **🔄 Events Kafka Traités**

#### **Ticket Events**
- `ticket.created` → Mise à jour volume quotidien
- `ticket.resolved` → Calcul MTTR, SLA compliance
- `ticket.status.updated` → Tracking workflow
- `ticket.sla.breached` → Alertes et escalade
- `ticket.priority.updated` → Recalcul SLA

#### **Assignment Events**
- `assignment.created` → Métriques assignation
- `assignment.reassigned` → Tracking réassignations
- `assignment.failed` → Analyse échecs
- `technician.status.updated` → Disponibilité
- `team.member.added/removed` → Taille équipe

### **📈 Agrégation Automatique**

#### **Jobs Planifiés**
- **Quotidien (1h00)** : Agrégation KPIs jour précédent
- **Hebdomadaire (Dimanche 2h00)** : Métriques semaine
- **Mensuel (1er du mois 2h00)** : Rapports mensuels
- **Satisfaction (toutes les 4h)** : Scores satisfaction

#### **Monitoring SLA**
- **Toutes les 15 min** : Vérification délais SLA
- **Temps réel** : Alertes approche/dépassement
- **Escalade automatique** : Manager → Admin

---

## 🎯 **Fonctionnalités Avancées**

### **🔧 Configuration SLA Dynamique**
- Délais configurables par catégorie/priorité
- Escalade automatique multi-niveaux
- Activation/désactivation configurations
- Validation règles métier

### **📊 Calcul KPIs Intelligent**
- Agrégation données multi-services
- Calculs temps réel et historiques
- Métriques comparatives équipes
- Tendances et évolutions temporelles

### **🚨 Système d'Alertes**
- Monitoring approche délais SLA
- Escalade automatique hiérarchique
- Notifications intégrées Kafka
- Résolution tracking

### **📈 Analytics Prédictifs**
- Identification tendances performance
- Prédiction charge travail
- Optimisation répartition équipes
- Benchmarking inter-équipes

---

## ✅ **Statut d'Implémentation**

### **🎯 COMPLET ET FONCTIONNEL**

**Toutes les fonctionnalités demandées sont implémentées :**
- ✅ **Supervision globale ADMIN** complète
- ✅ **Gestion équipe MANAGER** détaillée
- ✅ **Configuration SLA dynamique** flexible
- ✅ **Calcul KPIs complets** temps réel
- ✅ **Intégration multi-services** robuste
- ✅ **APIs REST sécurisées** complètes
- ✅ **Base données optimisée** performante
- ✅ **Tests unitaires** complets
- ✅ **Documentation** exhaustive

### **🚀 Prêt pour Production**

L'analytics-service est entièrement fonctionnel et prêt pour déploiement en production. Il fournit tous les tableaux de bord, KPIs, et fonctionnalités d'analyse nécessaires pour une gestion efficace et optimisée du système ITSM.

**Port : 8086 | Base : analytics_db | Sécurité : JWT | Architecture : DDD | Kafka : Temps Réel**
