@echo off
echo Testing User Service API endpoints...
echo.

REM Set the base URL
set BASE_URL=http://localhost:8082/api

REM Test health endpoint (should work without authentication)
echo Testing health endpoint...
curl -X GET "%BASE_URL%/../actuator/health" -H "Content-Type: application/json"
echo.
echo.

REM Note: The following endpoints require JWT authentication
REM You need to get a JWT token from auth-service first

echo To test authenticated endpoints, first get a JWT token from auth-service:
echo curl -X POST "http://localhost:8081/api/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"jean.dupont@alten.com\",\"motDePasse\":\"password123\"}"
echo.

echo Then use the token in subsequent requests:
echo curl -X GET "%BASE_URL%/users/550e8400-e29b-41d4-a716-446655440001" -H "Authorization: Bearer YOUR_JWT_TOKEN"
echo.

echo Available test endpoints:
echo GET %BASE_URL%/users/{id} - Get user profile
echo GET %BASE_URL%/technicians/available - Get available technicians
echo GET %BASE_URL%/technicians/stats/availability - Get availability statistics
echo.

pause
