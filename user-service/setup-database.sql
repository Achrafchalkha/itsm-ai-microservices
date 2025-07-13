-- Database setup script for user-service
-- Creates the user_db database and necessary tables

-- Create database (run this as postgres superuser)
CREATE DATABASE user_db;

-- Connect to user_db
\c user_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'TECHNICIEN', 'UTILISATEUR')),
    manager_id UUID,
    team_id UUID,
    statut_technicien VARCHAR(20) CHECK (statut_technicien IN ('DISPONIBLE', 'OCCUPE', 'ABSENT', 'HORS_LIGNE')),
    localisation VARCHAR(255),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create teams table
CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    manager_id UUID NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create team_categories table
CREATE TABLE IF NOT EXISTS team_categories (
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    categorie VARCHAR(100) NOT NULL,
    PRIMARY KEY (team_id, categorie)
);

-- Create team_members table
CREATE TABLE IF NOT EXISTS team_members (
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    member_id UUID NOT NULL,
    PRIMARY KEY (team_id, member_id)
);

-- Create competences table
CREATE TABLE IF NOT EXISTS competences (
    id UUID PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    categorie VARCHAR(50),
    niveau VARCHAR(20) NOT NULL CHECK (niveau IN ('JUNIOR', 'SENIOR', 'EXPERT')),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_manager_id ON users(manager_id);
CREATE INDEX IF NOT EXISTS idx_users_team_id ON users(team_id);
CREATE INDEX IF NOT EXISTS idx_users_statut_technicien ON users(statut_technicien);
CREATE INDEX IF NOT EXISTS idx_users_localisation ON users(localisation);
CREATE INDEX IF NOT EXISTS idx_users_actif ON users(actif);

CREATE INDEX IF NOT EXISTS idx_teams_manager_id ON teams(manager_id);
CREATE INDEX IF NOT EXISTS idx_teams_actif ON teams(actif);

CREATE INDEX IF NOT EXISTS idx_competences_user_id ON competences(user_id);
CREATE INDEX IF NOT EXISTS idx_competences_nom ON competences(nom);
CREATE INDEX IF NOT EXISTS idx_competences_categorie ON competences(categorie);
CREATE INDEX IF NOT EXISTS idx_competences_niveau ON competences(niveau);

-- Create a function to automatically update date_modification
CREATE OR REPLACE FUNCTION update_date_modification()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_modification = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update date_modification on users table
CREATE TRIGGER trigger_update_users_date_modification
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_date_modification();

-- Insert sample data for testing (following hierarchical workflow)

-- 1. Insert ADMIN (DSI ALTEN)
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Admin', 'DSI', 'dsi@alten.com', 'ADMIN', NULL, NULL, NULL, 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert MANAGERs (created by ADMIN)
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
('550e8400-e29b-41d4-a716-446655440003', 'Durand', 'Pierre', 'pierre.durand@alten.com', 'MANAGER', '550e8400-e29b-41d4-a716-446655440000', NULL, NULL, 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440005', 'Moreau', 'Claire', 'claire.moreau@alten.com', 'MANAGER', '550e8400-e29b-41d4-a716-446655440000', NULL, NULL, 'Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440006', 'Bernard', 'Marc', 'marc.bernard@alten.com', 'MANAGER', '550e8400-e29b-41d4-a716-446655440000', NULL, NULL, 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert teams (managed by MANAGERs)
INSERT INTO teams (id, nom, description, manager_id, date_creation, date_modification, actif) VALUES
('770e8400-e29b-41d4-a716-446655440001', 'Équipe Infrastructure', 'Gestion réseau, systèmes et matériel', '550e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('770e8400-e29b-41d4-a716-446655440002', 'Équipe Applications', 'Support ERP, CRM et applications métier', '550e8400-e29b-41d4-a716-446655440005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('770e8400-e29b-41d4-a716-446655440003', 'Équipe Sécurité', 'Sécurité informatique et gestion des identités', '550e8400-e29b-41d4-a716-446655440006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (id) DO NOTHING;

-- 4. Insert team categories
INSERT INTO team_categories (team_id, categorie) VALUES
('770e8400-e29b-41d4-a716-446655440001', 'Réseau'),
('770e8400-e29b-41d4-a716-446655440001', 'Système'),
('770e8400-e29b-41d4-a716-446655440001', 'Matériel'),
('770e8400-e29b-41d4-a716-446655440002', 'ERP'),
('770e8400-e29b-41d4-a716-446655440002', 'CRM'),
('770e8400-e29b-41d4-a716-446655440002', 'Applications'),
('770e8400-e29b-41d4-a716-446655440003', 'Sécurité'),
('770e8400-e29b-41d4-a716-446655440003', 'Identités')
ON CONFLICT (team_id, categorie) DO NOTHING;

-- 5. Insert TECHNICIENs (created by MANAGERs)
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
-- Infrastructure team
('550e8400-e29b-41d4-a716-446655440001', 'Dupont', 'Jean', 'jean.dupont@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440002', 'Martin', 'Marie', 'marie.martin@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440007', 'Petit', 'Luc', 'luc.petit@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Applications team
('550e8400-e29b-41d4-a716-446655440008', 'Roux', 'Anne', 'anne.roux@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440002', 'DISPONIBLE', 'Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440009', 'Blanc', 'Paul', 'paul.blanc@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440002', 'DISPONIBLE', 'Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Security team
('550e8400-e29b-41d4-a716-446655440010', 'Noir', 'Julie', 'julie.noir@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440003', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440011', 'Vert', 'Thomas', 'thomas.vert@alten.com', 'TECHNICIEN', '550e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440003', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (id) DO NOTHING;

-- 6. Insert team members
INSERT INTO team_members (team_id, member_id) VALUES
('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001'),
('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002'),
('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440007'),
('770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440008'),
('770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440009'),
('770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440010'),
('770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440011')
ON CONFLICT (team_id, member_id) DO NOTHING;

-- 7. Insert UTILISATEURs (self-registered)
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
('550e8400-e29b-41d4-a716-446655440004', 'Leroy', 'Sophie', 'sophie.leroy@alten.com', 'UTILISATEUR', NULL, NULL, NULL, 'Marseille', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440012', 'Garcia', 'Miguel', 'miguel.garcia@alten.com', 'UTILISATEUR', NULL, NULL, NULL, 'Toulouse', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (id) DO NOTHING;

-- 8. Insert sample competences for technicians
INSERT INTO competences (id, nom, description, categorie, niveau, user_id) VALUES
-- Infrastructure team competences
('660e8400-e29b-41d4-a716-446655440001', 'Réseau', 'Configuration et dépannage réseau', 'Infrastructure', 'SENIOR', '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440002', 'Windows', 'Administration système Windows', 'Infrastructure', 'EXPERT', '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440003', 'Linux', 'Administration système Linux', 'Infrastructure', 'SENIOR', '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655440004', 'Imprimantes', 'Installation et maintenance imprimantes', 'Infrastructure', 'JUNIOR', '550e8400-e29b-41d4-a716-446655440007'),
-- Applications team competences
('660e8400-e29b-41d4-a716-446655440005', 'SAP', 'Support ERP SAP', 'Applications', 'EXPERT', '550e8400-e29b-41d4-a716-446655440008'),
('660e8400-e29b-41d4-a716-446655440006', 'Salesforce', 'Support CRM Salesforce', 'Applications', 'SENIOR', '550e8400-e29b-41d4-a716-446655440009'),
-- Security team competences
('660e8400-e29b-41d4-a716-446655440007', 'Analyse sécurité', 'Analyse des menaces et vulnérabilités', 'Sécurité', 'EXPERT', '550e8400-e29b-41d4-a716-446655440010'),
('660e8400-e29b-41d4-a716-446655440008', 'Active Directory', 'Gestion des identités et accès', 'Sécurité', 'SENIOR', '550e8400-e29b-41d4-a716-446655440011')
ON CONFLICT (id) DO NOTHING;

-- Grant permissions (adjust as needed)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
