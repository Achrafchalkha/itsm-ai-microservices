@echo off
echo ========================================
echo Checking Kafka Topics for ITSM System
echo ========================================

echo.
echo 1. Listing all Kafka topics...
echo.
kafka-topics.sh --bootstrap-server localhost:9092 --list

echo.
echo ========================================
echo 2. Checking required topics exist...
echo ========================================

echo.
echo Checking: ticket.created
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic ticket.created
echo.

echo Checking: ticket.status.changed
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic ticket.status.changed
echo.

echo Checking: ticket.note.added
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic ticket.note.added
echo.

echo Checking: assignment.created
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic assignment.created
echo.

echo Checking: assignment.reassigned
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic assignment.reassigned
echo.

echo ========================================
echo 3. Creating missing topics if needed...
echo ========================================

echo Creating ticket.created topic...
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic ticket.created --partitions 3 --replication-factor 1 --if-not-exists

echo Creating ticket.status.changed topic...
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic ticket.status.changed --partitions 3 --replication-factor 1 --if-not-exists

echo Creating ticket.note.added topic...
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic ticket.note.added --partitions 3 --replication-factor 1 --if-not-exists

echo Creating assignment.created topic...
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic assignment.created --partitions 3 --replication-factor 1 --if-not-exists

echo Creating assignment.reassigned topic...
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic assignment.reassigned --partitions 3 --replication-factor 1 --if-not-exists

echo.
echo ========================================
echo 4. Final topic list...
echo ========================================
kafka-topics.sh --bootstrap-server localhost:9092 --list

echo.
echo ========================================
echo Topics check completed!
echo ========================================

echo.
echo To monitor events in real-time, run these commands in separate terminals:
echo.
echo Monitor ticket status changes:
echo kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ticket.status.changed --from-beginning
echo.
echo Monitor ticket note additions:
echo kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ticket.note.added --from-beginning
echo.
echo Monitor assignment events:
echo kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic assignment.created --from-beginning

pause
