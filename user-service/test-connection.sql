-- Test connection to user_db database
-- Run: psql -U postgres -h localhost -d user_db -f test-connection.sql

-- Check if we're connected to the right database
SELECT current_database() as connected_database;

-- Check PostgreSQL version
SELECT version();

-- List existing tables (should be empty initially)
\dt

-- Show database info
\l user_db
