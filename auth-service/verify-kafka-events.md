# 🔍 How to Verify Kafka Events

## 📋 **Step 1: Check Application Logs**

When you register a user, look for these log messages in your Spring Boot console:

### ✅ **Success Messages to Look For:**
```
INFO - Tentative d'inscription pour l'email: john.doe@example.com
INFO - Utilisateur créé avec succès: [user-id]
INFO - Événement UserCreated publié pour l'utilisateur: [user-id]
```

### ❌ **Error Messages to Watch For:**
```
ERROR - Failed to send message to Kafka
WARN - Connection to node -1 could not be established
ERROR - org.apache.kafka.common.errors.TimeoutException
```

## 🐳 **Step 2: Start Kafka (If Not Running)**

### Option A: Using Docker (Recommended)
```bash
# In your auth-service directory
docker compose up -d

# Check if Kafka is running
docker compose ps
```

### Option B: Check if Kafka is Running
```bash
# Check if Kafka port is open
netstat -an | findstr :9092

# If you see something like "0.0.0.0:9092", Kafka is running
```

## 📨 **Step 3: Monitor Kafka Messages**

### Method 1: Using Kafka Console Consumer
```bash
# If you have Kafka installed locally
cd C:\kafka
bin\windows\kafka-console-consumer.bat --topic user-created --bootstrap-server localhost:9092 --from-beginning
```

### Method 2: Using Docker Kafka Consumer
```bash
# If using Docker Kafka
docker exec -it auth-kafka kafka-console-consumer --topic user-created --bootstrap-server localhost:9092 --from-beginning
```

### Method 3: Using Kafka UI (Docker)
1. Open browser: http://localhost:8080
2. Navigate to Topics
3. Look for `user-created` topic
4. View messages in the topic

## 🧪 **Step 4: Test Kafka Event Creation**

### Test Procedure:
1. **Start monitoring Kafka** (using one of the methods above)
2. **Register a new user** via Postman:
   ```json
   POST http://localhost:8081/auth/register
   {
     "nom": "Test",
     "prenom": "User",
     "email": "test.user@example.com",
     "motDePasse": "password123"
   }
   ```
3. **Check the Kafka consumer** - you should see a message like:
   ```json
   {
     "userId": "123e4567-e89b-12d3-a456-426614174000",
     "email": "test.user@example.com",
     "nom": "Test",
     "prenom": "User",
     "role": "UTILISATEUR",
     "dateCreation": "2025-01-15T10:30:00"
   }
   ```

## 🔧 **Step 5: Troubleshooting Kafka**

### If Kafka Events Are Not Working:

#### Check 1: Kafka Connection
Add this to your application.properties for more detailed logging:
```properties
logging.level.org.apache.kafka=DEBUG
logging.level.org.springframework.kafka=DEBUG
```

#### Check 2: Disable Kafka Temporarily
If Kafka is causing issues, temporarily disable it:
```properties
# Comment out Kafka configuration
# spring.kafka.bootstrap-servers=localhost:9092
# spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
# spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

#### Check 3: Manual Topic Creation
```bash
# Create the topic manually
docker exec -it auth-kafka kafka-topics --create --topic user-created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# List topics to verify
docker exec -it auth-kafka kafka-topics --list --bootstrap-server localhost:9092
```

## 📊 **Step 6: Alternative - Check Application Logs Only**

If you can't access Kafka directly, you can verify events are being sent by checking the application logs:

1. **Enable DEBUG logging** in application.properties:
   ```properties
   logging.level.com.itsm.auth.application.service.RegisterUserUseCase=DEBUG
   logging.level.org.springframework.kafka.core.KafkaTemplate=DEBUG
   ```

2. **Register a user** and look for:
   ```
   DEBUG - Sending message to Kafka topic: user-created
   INFO - Événement UserCreated publié pour l'utilisateur: [user-id]
   ```

## 🎯 **Expected Kafka Message Format**

When a user registers, this JSON message should be sent to the `user-created` topic:

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@example.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "UTILISATEUR",
  "dateCreation": "2025-01-15T14:30:00.123"
}
```

## ✅ **Quick Verification Checklist**

- [ ] Application starts without Kafka errors
- [ ] Registration works and returns JWT token
- [ ] Application logs show "Événement UserCreated publié"
- [ ] Kafka consumer receives the message
- [ ] Message contains correct user data
- [ ] Role is always "UTILISATEUR" for registration

## 🚨 **If Kafka is Not Available**

The application will still work perfectly for authentication, but events won't be published. This is fine for testing the core functionality!
