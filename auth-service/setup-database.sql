-- Setup script for auth-service database
-- Run this script in your PostgreSQL instance

-- Create the database
CREATE DATABASE auth_db;

-- Connect to the database (you'll need to run \c auth_db after creating it)
-- The tables will be created automatically by Hibernate when the application starts

-- Optional: Create a specific user for the application (if you want)
-- CREATE USER auth_user WITH PASSWORD 'auth_password';
-- GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;

-- Verify database creation
SELECT datname FROM pg_database WHERE datname = 'auth_db';
