-- Script de création de la base de données ticket_db
-- Exécuter en tant que postgres

-- Se connecter à la base postgres
\c postgres;

-- Créer la base de données ticket_db si elle n'existe pas
SELECT 'CREATE DATABASE ticket_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ticket_db')\gexec

-- Vérifier que la base a été créée
\l ticket_db

-- Tester la connexion
\c ticket_db;

-- Afficher un message de succès
SELECT 'Base de données ticket_db créée avec succès!' as message;
