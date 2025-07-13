#!/bin/bash

# Test script for Auth Service API
BASE_URL="http://localhost:8081"

echo "🚀 Testing Auth Service API"
echo "=========================="

# Test 1: Register a new user
echo "📝 Test 1: Register a new user"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@example.com",
    "motDePasse": "password123"
  }')

echo "Response: $REGISTER_RESPONSE"

# Extract token from response
TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"
echo ""

# Test 2: Try to register the same user again (should fail)
echo "❌ Test 2: Try to register the same user again (should fail)"
curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@example.com",
    "motDePasse": "password123"
  }' | jq .
echo ""

# Test 3: Login with the registered user
echo "🔐 Test 3: Login with the registered user"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "motDePasse": "password123"
  }')

echo "Response: $LOGIN_RESPONSE"

# Extract token from login response
LOGIN_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Login Token: $LOGIN_TOKEN"
echo ""

# Test 4: Access protected endpoint with token
echo "🔒 Test 4: Access protected endpoint with token"
curl -s -X GET "$BASE_URL/user/profile" \
  -H "Authorization: Bearer $LOGIN_TOKEN" | jq .
echo ""

# Test 5: Try to access protected endpoint without token (should fail)
echo "🚫 Test 5: Try to access protected endpoint without token (should fail)"
curl -s -X GET "$BASE_URL/user/profile" | jq .
echo ""

# Test 6: Try to login with wrong password (should fail)
echo "❌ Test 6: Try to login with wrong password (should fail)"
curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "motDePasse": "wrongpassword"
  }' | jq .
echo ""

echo "✅ API tests completed!"
