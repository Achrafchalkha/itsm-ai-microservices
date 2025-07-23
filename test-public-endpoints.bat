@echo off
echo ========================================
echo Testing Public Endpoints for Assignment-Service
echo ========================================

echo.
echo 1. Testing User-Service Public Endpoints...
echo.

echo Testing: Get all active teams
curl -s http://localhost:8082/api/public/assignment/teams
echo.
echo.

echo Testing: Get teams by category SECURITE
curl -s http://localhost:8082/api/public/assignment/teams/category/SECURITE
echo.
echo.

echo Testing: Get all active technicians
curl -s http://localhost:8082/api/public/assignment/technicians
echo.
echo.

echo Testing: Get technicians by category SECURITE
curl -s http://localhost:8082/api/public/assignment/technicians/category/SECURITE
echo.
echo.

echo Testing: Health check
curl -s http://localhost:8082/api/public/assignment/health
echo.
echo.

echo 2. Testing Ticket-Service Public Endpoints...
echo.

echo Testing: Health check
curl -s http://localhost:8083/api/public/assignment/health
echo.
echo.

echo ========================================
echo Test completed!
echo ========================================

echo.
echo If all endpoints return data (not 404), the public endpoints are working correctly.
echo Assignment-service should now be able to access these endpoints without JWT tokens.

pause
