# 🗄️ Database Commands Guide

## 📋 **Quick Database Check Commands**

### **Connect to PostgreSQL**
```bash
# Using psql command line
psql -h localhost -p 5432 -U postgres -d postgres
# Enter your password: achrafmas03
```

### **1. Check if Table Exists**
```sql
-- Check table structure
\d utilisateurs;

-- Or using SQL
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'utilisateurs';
```

### **2. View All Users**
```sql
-- See all registered users (without passwords)
SELECT 
    id,
    nom,
    prenom,
    email,
    role,
    actif,
    date_creation
FROM utilisateurs
ORDER BY date_creation DESC;
```

### **3. Count Users**
```sql
-- Total number of users
SELECT COUNT(*) as total_users FROM utilisateurs;

-- Users by role
SELECT role, COUNT(*) as count 
FROM utilisateurs 
GROUP BY role;
```

### **4. Check Recent Registrations**
```sql
-- Users registered today
SELECT 
    nom,
    prenom,
    email,
    role,
    date_creation
FROM utilisateurs 
WHERE date_creation::date = CURRENT_DATE
ORDER BY date_creation DESC;
```

### **5. Verify Email Uniqueness**
```sql
-- Check for duplicate emails (should be empty)
SELECT email, COUNT(*) as count
FROM utilisateurs
GROUP BY email
HAVING COUNT(*) > 1;
```

## 🔍 **Expected Table Structure**

Your `utilisateurs` table should look like this:

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | uuid | NO | Primary key |
| nom | varchar(255) | NO | Last name |
| prenom | varchar(255) | NO | First name |
| email | varchar(255) | NO | Email (unique) |
| mot_de_passe_hashe | varchar(255) | NO | Hashed password |
| role | varchar(255) | NO | User role (UTILISATEUR, TECHNICIEN, MANAGER) |
| date_creation | timestamp | NO | Creation date |
| date_modification | timestamp | NO | Last modification date |
| actif | boolean | NO | Active status |

## 📊 **Sample Data Check**

After registering a user, you should see data like:

```sql
-- Example query result
SELECT * FROM utilisateurs LIMIT 1;

-- Expected result:
id                  | 550e8400-e29b-41d4-a716-446655440000
nom                 | Doe
prenom              | John
email               | john.doe@example.com
mot_de_passe_hashe  | $2a$10$... (BCrypt hash)
role                | UTILISATEUR
date_creation       | 2025-01-15 14:30:00.123
date_modification   | 2025-01-15 14:30:00.123
actif               | true
```

## 🛠️ **Useful Maintenance Commands**

### **Clear All Test Data**
```sql
-- ⚠️ WARNING: This deletes all users!
DELETE FROM utilisateurs WHERE email LIKE '%@example.com';

-- Or delete all users (be careful!)
-- DELETE FROM utilisateurs;
```

### **Check Database Size**
```sql
-- Database size
SELECT pg_size_pretty(pg_database_size('postgres')) as database_size;

-- Table size
SELECT pg_size_pretty(pg_total_relation_size('utilisateurs')) as table_size;
```

### **Check Constraints**
```sql
-- View table constraints
SELECT 
    conname as constraint_name,
    contype as constraint_type
FROM pg_constraint 
WHERE conrelid = 'utilisateurs'::regclass;
```

## 🔧 **Troubleshooting Database Issues**

### **If Table Doesn't Exist**
```sql
-- Manually create the table (if needed)
CREATE TABLE utilisateurs (
    id uuid PRIMARY KEY,
    nom varchar(255) NOT NULL,
    prenom varchar(255) NOT NULL,
    email varchar(255) NOT NULL UNIQUE,
    mot_de_passe_hashe varchar(255) NOT NULL,
    role varchar(255) NOT NULL CHECK (role IN ('UTILISATEUR','TECHNICIEN','MANAGER')),
    date_creation timestamp NOT NULL,
    date_modification timestamp NOT NULL,
    actif boolean NOT NULL DEFAULT true
);
```

### **If Connection Fails**
1. Check if PostgreSQL is running
2. Verify credentials in application.properties
3. Check if database 'postgres' exists
4. Ensure PostgreSQL is listening on port 5432

## 📝 **Quick Test Procedure**

1. **Connect to database**:
   ```bash
   psql -h localhost -p 5432 -U postgres -d postgres
   ```

2. **Check table exists**:
   ```sql
   \d utilisateurs;
   ```

3. **Register a user** via Postman

4. **Verify user was created**:
   ```sql
   SELECT COUNT(*) FROM utilisateurs;
   SELECT nom, prenom, email, role FROM utilisateurs ORDER BY date_creation DESC LIMIT 5;
   ```

5. **Check password is hashed**:
   ```sql
   SELECT email, LEFT(mot_de_passe_hashe, 20) as password_hash_preview FROM utilisateurs;
   ```

The password should start with `$2a$10$` (BCrypt hash) and NOT be plain text!
