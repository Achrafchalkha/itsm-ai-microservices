-- Script simplifié de création des bases de données
-- Exécuter en tant que postgres

-- Créer ticket_db
DROP DATABASE IF EXISTS ticket_db;
CREATE DATABASE ticket_db;

-- Créer assignment_db  
DROP DATABASE IF EXISTS assignment_db;
CREATE DATABASE assignment_db;

-- Vérifier les bases créées
\l
