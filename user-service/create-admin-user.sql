-- Create ADMIN user in AUTH-SERVICE database (auth_db)
-- This is the correct place for user authentication data
-- Connect: psql -U postgres -h localhost -d auth_db -f create-admin-user.sql

-- Insert ADMIN user into utilisateurs table (auth-service)
INSERT INTO utilisateurs (
    id,
    nom,
    prenom,
    email,
    mot_de_passe,  -- This should be encrypted
    role,
    actif,
    date_creation,
    date_modification
) VALUES (
    gen_random_uuid(),
    'Admin',
    'System',
    'admin@itsm.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTUDnDdYiDUiT4h7DcoFhfePOjKjPOIm',  -- BCrypt for "admin123"
    'ADMIN',
    true,
    NOW(),
    NOW()
);

-- Verify the ADMIN user was created
SELECT
    id,
    nom,
    prenom,
    email,
    role,
    actif,
    date_creation
FROM utilisateurs
WHERE role = 'ADMIN';

-- Show all users in auth database
SELECT
    nom || ' ' || prenom as full_name,
    email,
    role,
    actif
FROM utilisateurs
ORDER BY date_creation;

-- Note: Password is BCrypt hash of "admin123"
-- You can generate new hash using: https://bcrypt-generator.com/
