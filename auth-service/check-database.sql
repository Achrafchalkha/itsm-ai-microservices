-- SQL Commands to Check Auth Service Database

-- 1. Check if the utilisateurs table exists
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'utilisateurs'
ORDER BY ordinal_position;

-- 2. Count total users
SELECT COUNT(*) as total_users FROM utilisateurs;

-- 3. View all users (without passwords)
SELECT 
    id,
    nom,
    prenom,
    email,
    role,
    actif,
    date_creation,
    date_modification
FROM utilisateurs
ORDER BY date_creation DESC;

-- 4. Check users by role
SELECT role, COUNT(*) as count 
FROM utilisateurs 
GROUP BY role;

-- 5. Check recent registrations (last 24 hours)
SELECT 
    nom,
    prenom,
    email,
    role,
    date_creation
FROM utilisateurs 
WHERE date_creation >= NOW() - INTERVAL '1 day'
ORDER BY date_creation DESC;

-- 6. Check if email is unique (should have unique constraint)
SELECT email, COUNT(*) as count
FROM utilisateurs
GROUP BY email
HAVING COUNT(*) > 1;

-- 7. Check table structure
\d utilisateurs;

-- 8. Check indexes on the table
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'utilisateurs';

-- 9. Check database size
SELECT 
    pg_size_pretty(pg_database_size(current_database())) as database_size;

-- 10. Check table size
SELECT 
    pg_size_pretty(pg_total_relation_size('utilisateurs')) as table_size;
