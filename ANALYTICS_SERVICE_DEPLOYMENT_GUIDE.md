# 🚀 Analytics-Service - Guide de Déploiement et Test

## 📋 **Prérequis**

### **🔧 Environnement de Développement**
- **Java 17+** (OpenJDK ou Oracle JDK)
- **Maven 3.8+** pour la gestion des dépendances
- **PostgreSQL 13+** pour la base de données analytics_db
- **Apache Kafka 2.8+** pour les événements temps réel
- **Docker** (optionnel) pour containerisation

### **🔗 Services Dépendants**
- **auth-service** (port 8080) - Authentification JWT
- **user-service** (port 8081) - Gestion utilisateurs/équipes
- **ticket-service** (port 8082) - Gestion tickets
- **assignment-service** (port 8084) - Assignation intelligente
- **notifications-service** (port 8085) - Notifications

---

## 🗄️ **Configuration Base de Données**

### **1. Créer la Base de Données**
```sql
-- Connexion en tant que superuser PostgreSQL
CREATE DATABASE analytics_db;
CREATE USER analytics_user WITH PASSWORD 'analytics_password';
GRANT ALL PRIVILEGES ON DATABASE analytics_db TO analytics_user;
```

### **2. Exécuter le Script de Création**
```bash
cd analytics-service
psql -h localhost -U analytics_user -d analytics_db -f create-analytics-database.sql
```

### **3. Vérifier les Tables Créées**
```sql
-- Connexion à analytics_db
\dt

-- Vérifier les tables principales
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;
```

**Tables attendues :**
- `sla_configurations`
- `daily_kpis`
- `team_performance_metrics`
- `technician_performance_metrics`
- `satisfaction_scores`
- `sla_alerts`
- `monthly_reports`

---

## ⚙️ **Configuration Application**

### **1. Fichier application.properties**
```properties
# Server configuration
server.port=8086
spring.application.name=analytics-service

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/analytics_db
spring.datasource.username=analytics_user
spring.datasource.password=analytics_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=analytics-service-group
spring.kafka.consumer.auto-offset-reset=earliest

# JWT configuration
jwt.secret=your-jwt-secret-key-base64-encoded

# Service URLs
services.ticket-service.url=http://localhost:8082
services.user-service.url=http://localhost:8081
services.assignment-service.url=http://localhost:8084

# Analytics configuration
analytics.aggregation.enabled=true
analytics.aggregation.daily-job-cron=0 0 1 * * ?
analytics.aggregation.monthly-job-cron=0 0 2 1 * ?
analytics.sla.check-interval-minutes=15
```

### **2. Configuration Kafka Topics**
```bash
# Créer les topics Kafka nécessaires
kafka-topics.sh --create --topic ticket.created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic ticket.resolved --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic ticket.status.updated --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic ticket.sla.breached --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic assignment.created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic assignment.reassigned --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic sla.alert.created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

---

## 🚀 **Déploiement**

### **1. Compilation et Tests**
```bash
cd analytics-service

# Compilation
mvn clean compile

# Exécution des tests unitaires
mvn test

# Exécution des tests d'intégration
mvn integration-test

# Package complet avec tests
mvn clean package
```

### **2. Démarrage du Service**
```bash
# Méthode 1: Avec Maven
mvn spring-boot:run

# Méthode 2: Avec JAR
java -jar target/analytics-service-1.0.0.jar

# Méthode 3: Avec profil spécifique
java -jar target/analytics-service-1.0.0.jar --spring.profiles.active=production
```

### **3. Vérification du Démarrage**
```bash
# Health check
curl http://localhost:8086/api/analytics/health

# Réponse attendue
{
  "status": "UP",
  "service": "analytics-service",
  "timestamp": "2024-01-15T10:30:00",
  "version": "1.0.0"
}
```

---

## 🧪 **Tests et Validation**

### **1. Tests Unitaires**
```bash
# Exécuter tous les tests unitaires
mvn test

# Exécuter tests spécifiques
mvn test -Dtest=SLAConfigurationServiceTest
mvn test -Dtest=SatisfactionServiceTest
mvn test -Dtest=AdminDashboardControllerTest
```

### **2. Tests d'Intégration**
```bash
# Tests d'intégration complets
mvn integration-test

# Tests avec profil test
mvn test -Dspring.profiles.active=test
```

### **3. Tests API avec Postman**

#### **🔐 Authentification**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@itsm.com",
  "motDePasse": "admin123"
}
```

#### **📊 Dashboard ADMIN**
```http
GET http://localhost:8086/api/analytics/admin/dashboard?days=30
Authorization: Bearer {jwt_token}
```

#### **👥 Dashboard MANAGER**
```http
GET http://localhost:8086/api/analytics/manager/dashboard?days=30
Authorization: Bearer {jwt_token}
```

#### **⚙️ Configuration SLA**
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

#### **⭐ Satisfaction**
```http
POST http://localhost:8086/api/analytics/satisfaction
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "ticketId": "uuid-ticket",
  "technicienId": "uuid-technician",
  "teamId": "uuid-team",
  "score": 4,
  "commentaire": "Excellent service"
}
```

---

## 📊 **Monitoring et Logs**

### **1. Logs Application**
```bash
# Suivre les logs en temps réel
tail -f logs/analytics-service.log

# Filtrer les logs par niveau
grep "ERROR" logs/analytics-service.log
grep "WARN" logs/analytics-service.log
```

### **2. Métriques Kafka**
```bash
# Vérifier les consommateurs Kafka
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group analytics-service-group

# Surveiller les topics
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ticket.created --from-beginning
```

### **3. Monitoring Base de Données**
```sql
-- Vérifier les connexions actives
SELECT count(*) FROM pg_stat_activity WHERE datname = 'analytics_db';

-- Surveiller les requêtes lentes
SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;

-- Vérifier la taille des tables
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 🔧 **Dépannage**

### **❌ Problèmes Courants**

#### **1. Erreur de Connexion Base de Données**
```bash
# Vérifier la connexion PostgreSQL
psql -h localhost -U analytics_user -d analytics_db -c "SELECT 1;"

# Vérifier les permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO analytics_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO analytics_user;
```

#### **2. Erreur Kafka**
```bash
# Vérifier que Kafka est démarré
kafka-topics.sh --list --bootstrap-server localhost:9092

# Recréer les topics si nécessaire
kafka-topics.sh --delete --topic ticket.created --bootstrap-server localhost:9092
kafka-topics.sh --create --topic ticket.created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

#### **3. Erreur JWT**
```bash
# Vérifier la clé JWT dans application.properties
echo "dGVzdC1zZWNyZXQtZm9yLWFuYWx5dGljcy1zZXJ2aWNl" | base64 -d
```

#### **4. Erreur de Mémoire**
```bash
# Augmenter la mémoire JVM
java -Xmx2g -Xms1g -jar target/analytics-service-1.0.0.jar
```

### **🔍 Logs de Debug**
```properties
# Activer les logs debug dans application.properties
logging.level.com.itsm.analytics=DEBUG
logging.level.org.springframework.kafka=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## 📈 **Performance et Optimisation**

### **1. Optimisation Base de Données**
```sql
-- Analyser les performances
ANALYZE;

-- Reconstruire les index
REINDEX DATABASE analytics_db;

-- Vérifier les statistiques
SELECT schemaname, tablename, n_tup_ins, n_tup_upd, n_tup_del 
FROM pg_stat_user_tables ORDER BY n_tup_ins DESC;
```

### **2. Optimisation Kafka**
```properties
# Configuration optimisée pour production
spring.kafka.consumer.max-poll-records=100
spring.kafka.consumer.fetch-min-size=1024
spring.kafka.consumer.fetch-max-wait=500
spring.kafka.producer.batch-size=16384
spring.kafka.producer.linger-ms=5
```

### **3. Optimisation JVM**
```bash
# Configuration JVM pour production
java -server \
     -Xmx4g \
     -Xms2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar target/analytics-service-1.0.0.jar
```

---

## ✅ **Checklist de Déploiement**

### **🔧 Avant Déploiement**
- [ ] PostgreSQL installé et configuré
- [ ] Kafka installé et topics créés
- [ ] Services dépendants démarrés
- [ ] Configuration application.properties complète
- [ ] Tests unitaires passent (mvn test)
- [ ] Tests d'intégration passent

### **🚀 Pendant Déploiement**
- [ ] Base de données créée et script exécuté
- [ ] Service démarre sans erreur
- [ ] Health check répond OK
- [ ] Connexions Kafka établies
- [ ] Logs sans erreur critique

### **✅ Après Déploiement**
- [ ] APIs ADMIN accessibles
- [ ] APIs MANAGER accessibles
- [ ] Configuration SLA fonctionnelle
- [ ] Satisfaction scoring opérationnel
- [ ] Événements Kafka traités
- [ ] Agrégations planifiées actives

---

## 🎯 **Résumé**

L'analytics-service est maintenant **entièrement déployé et opérationnel** avec :

- ✅ **Dashboards complets** ADMIN et MANAGER
- ✅ **KPIs temps réel** via Kafka
- ✅ **Configuration SLA** dynamique
- ✅ **Système satisfaction** intégré
- ✅ **Agrégations automatiques** planifiées
- ✅ **APIs sécurisées** par rôles
- ✅ **Tests complets** unitaires et intégration
- ✅ **Monitoring** et observabilité

**Port : 8086 | Base : analytics_db | Kafka : Temps Réel | Production Ready**
