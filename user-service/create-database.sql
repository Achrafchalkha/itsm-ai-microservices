-- Create the user_db database for the user-service
-- Run this script as postgres superuser

-- Create database
CREATE DATABASE user_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'French_France.1252'
    LC_CTYPE = 'French_France.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;

-- Connect to the new database
\c user_db;

-- Create schema if needed (optional)
-- CREATE SCHEMA IF NOT EXISTS user_service;

-- The tables will be created automatically by Hibernate when the application starts
-- with spring.jpa.hibernate.ddl-auto=update

COMMENT ON DATABASE user_db IS 'Database for ITSM User Service - manages users, teams, and competencies';
