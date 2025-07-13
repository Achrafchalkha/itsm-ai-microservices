@echo off
echo Starting Auth Service...
echo Make sure PostgreSQL is running and auth_db database exists
echo.
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
pause
