@echo off
echo ========================================
echo Testing Assignment Service Integration
echo ========================================

echo.
echo 1. Checking if Assignment Service is running...
curl -s http://localhost:8084/actuator/health
if %errorlevel% neq 0 (
    echo ERROR: Assignment Service is not running on port 8084
    echo Please start assignment-service first:
    echo cd assignment-service
    echo mvn spring-boot:run
    pause
    exit /b 1
)

echo.
echo 2. Checking Kafka connection...
echo Assignment Service should be listening to topic: ticket.created

echo.
echo 3. Testing ticket creation with assignment...
echo Please ensure you have a valid JWT token for UTILISATEUR role

echo.
echo 4. To test manually:
echo curl -X POST http://localhost:8083/api/tickets \
echo   -H "Content-Type: application/json" \
echo   -H "Authorization: Bearer YOUR_JWT_TOKEN" \
echo   -d "{\"titre\": \"Test Assignment\", \"description\": \"Test automatic assignment\", \"enableNlp\": true}"

echo.
echo 5. Check assignment-service logs for:
echo   - "Received TicketCreatedEvent from topic: ticket.created"
echo   - "Processing automatic assignment for ticket"
echo   - "Successfully assigned ticket"

echo.
echo 6. Check if ticket is updated with technicien_id and team_id in database:
echo   SELECT id, titre, technicien_id, team_id FROM tickets ORDER BY date_creation DESC LIMIT 5;

pause
