# Kafka Setup for Auth Service

## 🚀 Quick Start

### 1. Start Kafka with Docker Compose

```bash
# Start Kafka and Zookeeper
docker-compose up -d

# Check if containers are running
docker-compose ps

# View logs
docker-compose logs -f kafka
```

### 2. Stop Kafka

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean restart)
docker-compose down -v
```

## 📊 Kafka UI

Once Kafka is running, you can access the Kafka UI at:
- **URL**: http://localhost:8080
- **Features**: 
  - View topics and messages
  - Monitor consumer groups
  - Browse Kafka cluster information

## 🔧 Manual Topic Management (Optional)

### Create the user-created topic manually:

```bash
# Connect to Kafka container
docker exec -it auth-kafka bash

# Create topic
kafka-topics --create \
  --topic user-created \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
kafka-topics --describe \
  --topic user-created \
  --bootstrap-server localhost:9092
```

### Test Kafka manually:

```bash
# Producer (send messages)
kafka-console-producer \
  --topic user-created \
  --bootstrap-server localhost:9092

# Consumer (receive messages) - in another terminal
kafka-console-consumer \
  --topic user-created \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

## 📝 Services Overview

| Service | Port | Description |
|---------|------|-------------|
| Zookeeper | 2181 | Kafka coordination service |
| Kafka | 9092 | Message broker |
| Kafka UI | 8080 | Web interface for Kafka |

## 🔍 Troubleshooting

### Check container status:
```bash
docker-compose ps
```

### View logs:
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs kafka
docker-compose logs zookeeper
```

### Restart services:
```bash
docker-compose restart kafka
```

### Clean restart:
```bash
docker-compose down -v
docker-compose up -d
```

## ✅ Verification

1. **Kafka UI**: Visit http://localhost:8080
2. **Topics**: Should see `user-created` topic after first user registration
3. **Messages**: Check messages in the topic when users register

## 🔗 Integration with Auth Service

The auth service will automatically:
- Connect to Kafka on `localhost:9092`
- Create the `user-created` topic if it doesn't exist
- Publish `UserCreatedEvent` messages when users register
