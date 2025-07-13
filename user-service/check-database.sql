-- Database verification script for user-service
-- Run this to check if the database is properly set up

-- Check if database exists
SELECT datname FROM pg_database WHERE datname = 'user_db';

-- Connect to user_db
\c user_db;

-- Check tables
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- Check users table structure
\d users;

-- Check competences table structure
\d competences;

-- Check indexes
SELECT indexname, tablename FROM pg_indexes WHERE schemaname = 'public' ORDER BY tablename, indexname;

-- Check sample data
SELECT 'Users count' as check_type, count(*) as count FROM users
UNION ALL
SELECT 'Competences count' as check_type, count(*) as count FROM competences
UNION ALL
SELECT 'Technicians count' as check_type, count(*) as count FROM users WHERE role = 'TECHNICIEN'
UNION ALL
SELECT 'Available technicians count' as check_type, count(*) as count FROM users WHERE role = 'TECHNICIEN' AND statut_technicien = 'DISPONIBLE';

-- Check user-competence relationships
SELECT 
    u.nom, 
    u.prenom, 
    u.role, 
    u.statut_technicien,
    c.nom as competence,
    c.niveau_expertise
FROM users u
LEFT JOIN competences c ON u.id = c.user_id
ORDER BY u.nom, c.nom;
