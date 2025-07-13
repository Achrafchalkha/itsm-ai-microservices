@echo off
echo ========================================
echo ITSM Platform - Quick Start Guide
echo ========================================
echo.

echo Step 1: Setup Database
echo ----------------------
echo Run this command to setup the hierarchical structure:
echo psql -U postgres -h localhost -d user_db -f complete-hierarchy-setup.sql
echo.
pause

echo Step 2: Start Services
echo ----------------------
echo Make sure these services are running in order:
echo 1. PostgreSQL (port 5432)
echo 2. Kafka (port 9092)  
echo 3. Eureka Server (port 8761)
echo 4. Auth Service (port 8081)
echo 5. User Service (port 8082)
echo.
pause

echo Step 3: Test Service Health
echo ---------------------------
echo Testing Eureka Server...
curl -s http://localhost:8761/ >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Eureka Server is running
) else (
    echo ✗ Eureka Server is not accessible
)

echo Testing Auth Service...
curl -s http://localhost:8081/actuator/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Auth Service is running
) else (
    echo ✗ Auth Service is not accessible
)

echo Testing User Service...
curl -s http://localhost:8082/actuator/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ User Service is running
) else (
    echo ✗ User Service is not accessible
)
echo.

echo Step 4: Test Login (Get JWT Token)
echo ----------------------------------
echo Login as ADMIN to get JWT token:
echo.
echo curl -X POST "http://localhost:8081/api/auth/login" ^
echo   -H "Content-Type: application/json" ^
echo   -d "{\"email\":\"admin@alten.com\",\"motDePasse\":\"password123\"}"
echo.
echo Save the JWT token from the response!
echo.
pause

echo Step 5: Test User Service
echo -------------------------
echo Get ADMIN profile (replace YOUR_JWT_TOKEN):
echo.
echo curl -X GET "http://localhost:8082/api/users/00000000-0000-0000-0000-000000000001" ^
echo   -H "Authorization: Bearer YOUR_JWT_TOKEN"
echo.
echo Get available technicians:
echo.
echo curl -X GET "http://localhost:8082/api/technicians/available" ^
echo   -H "Authorization: Bearer YOUR_JWT_TOKEN"
echo.

echo Step 6: Verify Hierarchy
echo ------------------------
echo Check the organizational structure in database:
echo.
echo psql -U postgres -h localhost -d user_db -c "
echo SELECT 
echo   u.role,
echo   u.nom,
echo   u.prenom,
echo   u.email,
echo   m.nom as manager_name,
echo   t.nom as team_name
echo FROM users u
echo LEFT JOIN users m ON u.manager_id = m.id  
echo LEFT JOIN teams t ON u.team_id = t.id
echo ORDER BY u.role, u.nom;"
echo.

echo ========================================
echo Quick Test URLs:
echo ========================================
echo Eureka Dashboard: http://localhost:8761/
echo Auth Service Health: http://localhost:8081/actuator/health
echo User Service Health: http://localhost:8082/actuator/health
echo.
echo Test Accounts:
echo ADMIN: admin@alten.com / password123
echo MANAGER: pierre.durand@alten.com / password123  
echo TECHNICIEN: jean.dupont@alten.com / password123
echo UTILISATEUR: sophie.leroy@alten.com / password123
echo.
echo For complete testing guide, see: complete-testing-guide.md
echo.
pause
