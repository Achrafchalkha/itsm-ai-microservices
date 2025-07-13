@echo off
echo Starting User Service...
echo.

REM Check if Maven is available
mvn --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

echo Checking dependencies...
echo.

REM Check if PostgreSQL is running
echo Testing PostgreSQL connection...
psql -U postgres -h localhost -d user_db -c "SELECT 1;" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Cannot connect to PostgreSQL user_db database
    echo Please ensure PostgreSQL is running and user_db database exists
    echo Run setup-database.sql if needed
    echo.
)

REM Check if Eureka server is running
echo Testing Eureka server connection...
curl -s http://localhost:8761/eureka/apps >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Cannot connect to Eureka server at localhost:8761
    echo Please ensure Eureka server is running
    echo.
)

REM Check if Kafka is running
echo Testing Kafka connection...
REM This is a simple check - in production you'd use kafka tools
netstat -an | find "9092" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Kafka might not be running on port 9092
    echo Please ensure Kafka is running
    echo.
)

echo Starting User Service on port 8082...
echo.
echo Service will be available at: http://localhost:8082
echo Health check: http://localhost:8082/actuator/health
echo.
echo Press Ctrl+C to stop the service
echo.

REM Start the application
mvn spring-boot:run

pause
