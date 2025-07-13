# 🚀 Kafka Setup Guide

## Option 1: Docker Setup (Recommended)

### Prerequisites
- Install Docker Desktop for Windows
- Download from: https://www.docker.com/products/docker-desktop

### Start Kafka with Docker
```bash
# Navigate to the auth-service directory
cd Downloads\auth-service\auth-service

# Start Kafka and Zookeeper
docker compose up -d

# Check if containers are running
docker compose ps

# View logs
docker compose logs kafka

# Stop Kafka
docker compose down
```

### Kafka UI Access
- **URL**: http://localhost:8080
- **Features**: View topics, messages, consumer groups

## Option 2: Manual Kafka Setup (Without Docker)

### Download Kafka
1. Go to: https://kafka.apache.org/downloads
2. Download Kafka 2.13-3.6.0 (or latest)
3. Extract to `C:\kafka`

### Start Kafka Manually
```bash
# 1. Start Zookeeper (in first terminal)
cd C:\kafka
bin\windows\zookeeper-server-start.bat config\zookeeper.properties

# 2. Start Kafka (in second terminal)
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties

# 3. Create topic (in third terminal)
cd C:\kafka
bin\windows\kafka-topics.bat --create --topic user-created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 4. List topics
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

### Test Kafka Manually
```bash
# Producer (send messages)
bin\windows\kafka-console-producer.bat --topic user-created --bootstrap-server localhost:9092

# Consumer (receive messages) - in another terminal
bin\windows\kafka-console-consumer.bat --topic user-created --bootstrap-server localhost:9092 --from-beginning
```

## Option 3: Test Without Kafka

If you want to test the API without Kafka, you can temporarily disable it:

### Disable Kafka in application.properties
```properties
# Comment out Kafka configuration
# spring.kafka.bootstrap-servers=localhost:9092
# spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
# spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Modify RegisterUserUseCase
Comment out the Kafka publishing code:
```java
// kafkaTemplate.send("user-created", event);
// log.info("Événement UserCreated publié pour l'utilisateur: {}", utilisateurSauvegarde.getId());
log.info("User created successfully (Kafka disabled): {}", utilisateurSauvegarde.getId());
```

## 🔍 Verify Kafka is Working

### Check Application Logs
When you register a user, you should see:
```
INFO - Événement UserCreated publié pour l'utilisateur: [user-id]
```

### Check Kafka Topic
```bash
# List messages in the topic
bin\windows\kafka-console-consumer.bat --topic user-created --bootstrap-server localhost:9092 --from-beginning
```

### Expected Kafka Message
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

## 🚨 Troubleshooting

### Kafka Connection Issues
- Check if Kafka is running on port 9092
- Verify Zookeeper is running on port 2181
- Check firewall settings

### Application Startup Issues
- If Kafka is not available, the app may fail to start
- Temporarily disable Kafka configuration to test API only

### Docker Issues
- Make sure Docker Desktop is running
- Check if ports 9092, 2181, 8080 are available
- Try `docker compose down -v` and `docker compose up -d` for clean restart

## 📊 Monitoring

### Kafka UI (Docker only)
- **URL**: http://localhost:8080
- **Username**: Not required
- **Features**:
  - View topics and partitions
  - Browse messages
  - Monitor consumer groups
  - Cluster information

### Command Line Monitoring
```bash
# Check topic details
bin\windows\kafka-topics.bat --describe --topic user-created --bootstrap-server localhost:9092

# Check consumer groups
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
```

## 🎯 Recommendation

For development and testing:
1. **Start with Option 3** (No Kafka) to test the API first
2. **Then try Option 1** (Docker) if you have Docker installed
3. **Use Option 2** (Manual) only if Docker is not available

The auth service works perfectly without Kafka - it just won't publish events.
