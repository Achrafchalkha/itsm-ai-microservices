# 📮 Postman Testing Guide for Auth Service

## 🚀 Quick Setup

### 1. Import the Collection
1. Open Postman
2. Click **Import** button
3. Select the file: `Auth-Service-Postman-Collection.json`
4. The collection will be imported with all test cases

### 2. Start the Auth Service
```bash
# Make sure you're in the auth-service directory
mvn spring-boot:run
```

Wait for the message: `Started AuthServiceApplication in X seconds`

## 📋 Test Scenarios

### **Test 1: Register a New User** ✅
- **Method**: POST
- **URL**: `http://localhost:8081/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "motDePasse": "password123"
}
```
- **Expected Response**: 201 Created
- **Response Body**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": "uuid-here",
  "email": "john.doe@example.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "UTILISATEUR"
}
```

### **Test 2: Login User** ✅
- **Method**: POST
- **URL**: `http://localhost:8081/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "email": "john.doe@example.com",
  "motDePasse": "password123"
}
```
- **Expected Response**: 200 OK
- **Response Body**: Same as registration

### **Test 3: Get User Profile (Protected)** 🔒
- **Method**: GET
- **URL**: `http://localhost:8081/user/profile`
- **Headers**: `Authorization: Bearer YOUR_JWT_TOKEN`
- **Expected Response**: 200 OK
- **Response Body**:
```json
{
  "id": "uuid-here",
  "email": "john.doe@example.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "UTILISATEUR",
  "actif": true
}
```

### **Test 4: Register Duplicate User** ❌
- **Method**: POST
- **URL**: `http://localhost:8081/auth/register`
- **Body**: Same as Test 1
- **Expected Response**: 400 Bad Request
- **Response Body**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Un utilisateur avec cet email existe déjà"
}
```

### **Test 5: Login with Wrong Password** ❌
- **Method**: POST
- **URL**: `http://localhost:8081/auth/login`
- **Body**:
```json
{
  "email": "john.doe@example.com",
  "motDePasse": "wrongpassword"
}
```
- **Expected Response**: 400 Bad Request

### **Test 6: Access Protected Route Without Token** ❌
- **Method**: GET
- **URL**: `http://localhost:8081/user/profile`
- **Headers**: No Authorization header
- **Expected Response**: 401 Unauthorized

## 🔧 Manual Testing Steps

### Step 1: Register a User
1. Open Postman
2. Create a new POST request to `http://localhost:8081/auth/register`
3. Set header: `Content-Type: application/json`
4. Add the registration JSON body
5. Send the request
6. **Copy the token** from the response

### Step 2: Test Protected Endpoint
1. Create a new GET request to `http://localhost:8081/user/profile`
2. Add header: `Authorization: Bearer YOUR_COPIED_TOKEN`
3. Send the request
4. You should get the user profile

### Step 3: Test Login
1. Create a new POST request to `http://localhost:8081/auth/login`
2. Set header: `Content-Type: application/json`
3. Add the login JSON body
4. Send the request
5. You should get a new token

## 🐛 Troubleshooting

### Connection Refused (Port 8081)
- Make sure the Spring Boot application is running
- Check the console for "Started AuthServiceApplication"
- Verify no other application is using port 8081

### 401 Unauthorized on Protected Routes
- Make sure you're using the correct token
- Check that the Authorization header format is: `Bearer YOUR_TOKEN`
- Tokens expire after 24 hours by default

### 400 Bad Request on Registration
- Check that all required fields are provided
- Verify email format is valid
- Make sure password is at least 8 characters

### Database Errors
- Ensure PostgreSQL is running
- Verify database connection in application.properties
- Check that the `utilisateurs` table exists

## 📊 Expected Kafka Events

When you register a user, check the application logs for:
```
INFO - Événement UserCreated publié pour l'utilisateur: [user-id]
```

If you have Kafka running, the event will be published to the `user-created` topic.

## 🔍 Validation Rules

### Registration Validation:
- **nom**: Required, not blank
- **prenom**: Required, not blank  
- **email**: Required, valid email format, unique
- **motDePasse**: Required, minimum 8 characters

### Login Validation:
- **email**: Required, valid email format
- **motDePasse**: Required, not blank

## 🎯 Success Criteria

✅ **All tests should pass if:**
- User registration works and returns JWT token
- Login works with correct credentials
- Protected routes require valid JWT token
- Invalid requests return appropriate error codes
- Duplicate email registration is rejected
- Wrong password login is rejected

## 📝 Notes

- **Role Restriction**: Registration only creates users with `UTILISATEUR` role
- **Token Expiration**: JWT tokens expire after 24 hours
- **Password Security**: Passwords are hashed with BCrypt
- **Database**: Uses PostgreSQL with automatic table creation
