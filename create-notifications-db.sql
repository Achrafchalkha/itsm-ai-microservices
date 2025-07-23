-- Script de création de la base de données notifications_db
-- Exécuter en tant que postgres

-- Se connecter à la base postgres
\c postgres;

-- Créer la base de données notifications_db si elle n'existe pas
SELECT 'CREATE DATABASE notifications_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notifications_db')\gexec

-- Vérifier que la base a été créée
\l notifications_db

-- Tester la connexion
\c notifications_db;

-- Afficher un message de succès
SELECT 'Base de données notifications_db créée avec succès!' as message;
