# Auth Service - Spring Boot DDD

Service d'authentification implémenté avec Spring Boot suivant l'architecture DDD (Domain Driven Design).

## 🏗️ Architecture DDD

```
src/main/java/com/itsm/auth/
├── application/         # Cas d'utilisation et DTOs
│   ├── dto/            # Data Transfer Objects
│   └── service/        # Use Cases
├── domain/             # Entités, aggregates, value objects
│   ├── model/          # Entités métier
│   ├── repository/     # Interfaces des repositories
│   └── event/          # Événements du domaine
├── infrastructure/     # Adapters (db, kafka, security)
│   ├── persistence/    # JPA entities, repositories, mappers
│   ├── security/       # JWT provider, filters
│   └── config/         # Configuration Spring
└── interfaces/         # Controllers, DTOs
    └── rest/           # REST controllers
```

## 🚀 Fonctionnalités

- ✅ **Inscription utilisateur** (`/auth/register`) - Rôle UTILISATEUR uniquement
- ✅ **Connexion utilisateur** (`/auth/login`) - Retourne JWT
- ✅ **Authentification JWT** - Protection des routes
- ✅ **Publication d'événements Kafka** - UserCreatedEvent
- ✅ **Base de données PostgreSQL** - Persistance des utilisateurs
- ✅ **Validation des données** - Bean Validation
- ✅ **Gestion d'erreurs** - Global Exception Handler

## 🛠️ Prérequis

- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (local)
- Docker & Docker Compose (pour Kafka)

## 📦 Installation

### 1. Base de données PostgreSQL (Local)

Créer la base de données dans votre PostgreSQL local :

```sql
CREATE DATABASE auth_db;
```

Mettre à jour `application.properties` avec vos credentials PostgreSQL :
```properties
spring.datasource.username=your_postgres_user
spring.datasource.password=your_postgres_password
```

### 2. Apache Kafka (Docker)

Démarrer Kafka avec Docker Compose :

```bash
# Démarrer Kafka et Zookeeper
docker-compose up -d

# Vérifier que les conteneurs sont en cours d'exécution
docker-compose ps

# Accéder à Kafka UI : http://localhost:8080
```

### 3. Compilation et démarrage

```bash
mvn clean install
mvn spring-boot:run
```

Le service démarre sur le port **8081**.

## 🔧 Configuration

Fichier `application.properties` :

```properties
# Database (PostgreSQL local)
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# Kafka (Docker)
spring.kafka.bootstrap-servers=localhost:9092

# JWT
app.jwt.secret=mySecretKey123456789012345678901234567890123456789012345678901234567890
app.jwt.expiration=86400000
```

## 🐳 Services Docker

| Service | Port | Description |
|---------|------|-------------|
| Zookeeper | 2181 | Coordination Kafka |
| Kafka | 9092 | Message broker |
| Kafka UI | 8080 | Interface web Kafka |

## 📡 API Endpoints

### Inscription (Public)

```http
POST /auth/register
Content-Type: application/json

{
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "motDePasse": "password123"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "john.doe@example.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "UTILISATEUR"
}
```

### Connexion (Public)

```http
POST /auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "motDePasse": "password123"
}
```

### Profil utilisateur (Protégé)

```http
GET /user/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## 🔐 Sécurité

- **Hachage des mots de passe** : BCrypt
- **JWT** : HS512 avec expiration 24h
- **Rôles** : UTILISATEUR, TECHNICIEN, MANAGER
- **Inscription** : Limitée au rôle UTILISATEUR uniquement

## 📨 Événements Kafka

### UserCreatedEvent

Topic: `user-created`

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "john.doe@example.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "UTILISATEUR",
  "dateCreation": "2024-01-15T10:30:00"
}
```

## 🧪 Tests

```bash
# Exécuter tous les tests
mvn test

# Tests d'intégration
mvn test -Dtest=*IntegrationTest
```

## 📝 Exemples d'utilisation

### Test automatisé

Exécuter le script de test :
```bash
# Windows
test-api.bat

# Linux/Mac (créer test-api.sh)
chmod +x test-api.sh
./test-api.sh
```

### Test manuel avec curl

```bash
# Inscription
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@example.com",
    "motDePasse": "password123"
  }'

# Connexion
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "motDePasse": "password123"
  }'

# Profil (avec token)
curl -X GET http://localhost:8081/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🔍 Monitoring

- **Logs** : Niveau DEBUG pour `com.itsm.auth`
- **Health Check** : `/actuator/health`
- **Métriques** : `/actuator/metrics`

## 🚨 Gestion d'erreurs

- **400 Bad Request** : Données invalides
- **401 Unauthorized** : Token JWT invalide
- **409 Conflict** : Email déjà existant
- **500 Internal Server Error** : Erreur serveur
