-- Script de création des bases de données pour le système ITSM
-- Exécuter en tant que superuser PostgreSQL

-- =====================================================
-- 1. TICKET-SERVICE DATABASE (ticket_db)
-- =====================================================

-- Créer la base de données ticket_db
DROP DATABASE IF EXISTS ticket_db;
CREATE DATABASE ticket_db;

-- Créer l'utilisateur pour ticket-service (si n'existe pas)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ticket_user') THEN
        CREATE USER ticket_user WITH PASSWORD 'achrafmas03';
    END IF;
END
$$;

-- Accorder tous les privilèges sur ticket_db
GRANT ALL PRIVILEGES ON DATABASE ticket_db TO ticket_user;

-- Se connecter à ticket_db pour créer les tables
\c ticket_db;

-- Accorder les privilèges sur le schéma public
GRANT ALL ON SCHEMA public TO ticket_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ticket_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ticket_user;

-- =====================================================
-- 2. ASSIGNMENT-SERVICE DATABASE (assignment_db)
-- =====================================================

-- Revenir à la base postgres pour créer assignment_db
\c postgres;

-- Créer la base de données assignment_db
DROP DATABASE IF EXISTS assignment_db;
CREATE DATABASE assignment_db;

-- Créer l'utilisateur pour assignment-service (si n'existe pas)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'assignment_user') THEN
        CREATE USER assignment_user WITH PASSWORD 'achrafmas03';
    END IF;
END
$$;

-- Accorder tous les privilèges sur assignment_db
GRANT ALL PRIVILEGES ON DATABASE assignment_db TO assignment_user;

-- Se connecter à assignment_db pour configurer les privilèges
\c assignment_db;

-- Accorder les privilèges sur le schéma public
GRANT ALL ON SCHEMA public TO assignment_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO assignment_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO assignment_user;

-- =====================================================
-- 3. VÉRIFICATION DES BASES DE DONNÉES
-- =====================================================

-- Revenir à postgres pour vérifier
\c postgres;

-- Lister les bases de données créées
SELECT datname, datowner FROM pg_database WHERE datname IN ('ticket_db', 'assignment_db');

-- Lister les utilisateurs créés
SELECT rolname FROM pg_roles WHERE rolname IN ('ticket_user', 'assignment_user');

-- =====================================================
-- 4. INSTRUCTIONS D'EXÉCUTION
-- =====================================================

/*
Pour exécuter ce script :

1. Se connecter en tant que superuser PostgreSQL :
   psql -U postgres -h localhost

2. Exécuter le script :
   \i setup-databases.sql

3. Ou en une ligne :
   psql -U postgres -h localhost -f setup-databases.sql

4. Vérifier les connexions :
   psql -U ticket_user -d ticket_db -h localhost
   psql -U assignment_user -d assignment_db -h localhost
*/
