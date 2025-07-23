@echo off
echo ========================================
echo Cleaning and Recompiling All Services
echo ========================================

echo.
echo 1. Cleaning auth-service...
cd auth-service
call mvn clean
if %errorlevel% neq 0 (
    echo ERROR: Failed to clean auth-service
    pause
    exit /b 1
)

echo.
echo 2. Compiling auth-service...
call mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile auth-service
    pause
    exit /b 1
)

echo.
echo 3. Cleaning user-service...
cd ..\user-service
call mvn clean
if %errorlevel% neq 0 (
    echo ERROR: Failed to clean user-service
    pause
    exit /b 1
)

echo.
echo 4. Compiling user-service...
call mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile user-service
    pause
    exit /b 1
)

echo.
echo 5. Cleaning ticket-service...
cd ..\ticket-service
call mvn clean
if %errorlevel% neq 0 (
    echo ERROR: Failed to clean ticket-service
    pause
    exit /b 1
)

echo.
echo 6. Compiling ticket-service...
call mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile ticket-service
    pause
    exit /b 1
)

echo.
echo 7. Cleaning assignment-service...
cd ..\assignment-service
call mvn clean
if %errorlevel% neq 0 (
    echo ERROR: Failed to clean assignment-service
    pause
    exit /b 1
)

echo.
echo 8. Compiling assignment-service...
call mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile assignment-service
    pause
    exit /b 1
)

cd ..

echo.
echo ========================================
echo All services compiled successfully!
echo ========================================

echo.
echo You can now start the services:
echo 1. Start auth-service: cd auth-service && mvn spring-boot:run
echo 2. Start user-service: cd user-service && mvn spring-boot:run  
echo 3. Start ticket-service: cd ticket-service && mvn spring-boot:run
echo 4. Start assignment-service: cd assignment-service && mvn spring-boot:run

pause
