@echo off
echo 🚀 Testing Auth Service API
echo ==========================

set BASE_URL=http://localhost:8081

echo.
echo 📝 Test 1: Register a new user
curl -X POST "%BASE_URL%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"nom\": \"Doe\", \"prenom\": \"John\", \"email\": \"john.doe@example.com\", \"motDePasse\": \"password123\"}"

echo.
echo.
echo ❌ Test 2: Try to register the same user again (should fail)
curl -X POST "%BASE_URL%/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"nom\": \"Doe\", \"prenom\": \"John\", \"email\": \"john.doe@example.com\", \"motDePasse\": \"password123\"}"

echo.
echo.
echo 🔐 Test 3: Login with the registered user
curl -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"john.doe@example.com\", \"motDePasse\": \"password123\"}"

echo.
echo.
echo 🔒 Test 4: Access protected endpoint (you need to copy the token from login response)
echo Please copy the token from the login response above and run:
echo curl -X GET "%BASE_URL%/user/profile" -H "Authorization: Bearer YOUR_TOKEN_HERE"

echo.
echo.
echo 🚫 Test 5: Try to access protected endpoint without token (should fail)
curl -X GET "%BASE_URL%/user/profile"

echo.
echo.
echo ❌ Test 6: Try to login with wrong password (should fail)
curl -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"john.doe@example.com\", \"motDePasse\": \"wrongpassword\"}"

echo.
echo.
echo ✅ API tests completed!
pause
