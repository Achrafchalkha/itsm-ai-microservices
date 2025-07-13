-- Complete SQL script to create the hierarchical ITSM structure
-- Run this after setting up the database schema

-- Connect to user_db
\c user_db;

-- Clear existing data (optional - remove if you want to keep existing data)
DELETE FROM competences;
DELETE FROM team_members;
DELETE FROM team_categories;
DELETE FROM teams;
DELETE FROM users;

-- ========================================
-- 1. CREATE ADMIN (DSI ALTEN)
-- ========================================
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
('00000000-0000-0000-0000-000000000001', 'Admin', 'DSI', 'admin@alten.com', 'ADMIN', NULL, NULL, NULL, 'Paris - Siège', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- ========================================
-- 2. CREATE MANAGERS (Created by ADMIN)
-- ========================================
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
-- Manager Infrastructure
('00000000-0000-0000-0000-000000000101', 'Durand', 'Pierre', 'pierre.durand@alten.com', 'MANAGER', '00000000-0000-0000-0000-000000000001', NULL, NULL, 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Manager Applications  
('00000000-0000-0000-0000-000000000102', 'Moreau', 'Claire', 'claire.moreau@alten.com', 'MANAGER', '00000000-0000-0000-0000-000000000001', NULL, NULL, 'Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Manager Sécurité
('00000000-0000-0000-0000-000000000103', 'Bernard', 'Marc', 'marc.bernard@alten.com', 'MANAGER', '00000000-0000-0000-0000-000000000001', NULL, NULL, 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- ========================================
-- 3. CREATE TEAMS (Managed by MANAGERs)
-- ========================================
INSERT INTO teams (id, nom, description, manager_id, date_creation, date_modification, actif) VALUES
-- Équipe Infrastructure
('00000000-0000-0000-0000-000000000201', 'Équipe Infrastructure', 'Gestion réseau, systèmes et support desktop', '00000000-0000-0000-0000-000000000101', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Équipe Applications
('00000000-0000-0000-0000-000000000202', 'Équipe Applications', 'Support ERP, CRM et applications métier', '00000000-0000-0000-0000-000000000102', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Équipe Sécurité
('00000000-0000-0000-0000-000000000203', 'Équipe Sécurité', 'Analyse sécurité et administration des identités', '00000000-0000-0000-0000-000000000103', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- ========================================
-- 4. DEFINE TEAM CATEGORIES
-- ========================================
INSERT INTO team_categories (team_id, categorie) VALUES
-- Infrastructure team categories
('00000000-0000-0000-0000-000000000201', 'Réseau'),
('00000000-0000-0000-0000-000000000201', 'Système'),
('00000000-0000-0000-0000-000000000201', 'Desktop'),
('00000000-0000-0000-0000-000000000201', 'Matériel'),
-- Applications team categories
('00000000-0000-0000-0000-000000000202', 'ERP'),
('00000000-0000-0000-0000-000000000202', 'CRM'),
('00000000-0000-0000-0000-000000000202', 'Applications'),
-- Security team categories
('00000000-0000-0000-0000-000000000203', 'Sécurité'),
('00000000-0000-0000-0000-000000000203', 'Identités'),
('00000000-0000-0000-0000-000000000203', 'Audit');

-- ========================================
-- 5. CREATE TECHNICIANS (Created by MANAGERs)
-- ========================================

-- Infrastructure Team Technicians
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
-- Spécialiste réseau
('00000000-0000-0000-0000-000000000301', 'Dupont', 'Jean', 'jean.dupont@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Spécialiste système
('00000000-0000-0000-0000-000000000302', 'Martin', 'Marie', 'marie.martin@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Support desktop
('00000000-0000-0000-0000-000000000303', 'Petit', 'Luc', 'luc.petit@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Applications Team Technicians
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
-- Support ERP
('00000000-0000-0000-0000-000000000304', 'Roux', 'Anne', 'anne.roux@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000202', 'DISPONIBLE', 'Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Support CRM
('00000000-0000-0000-0000-000000000305', 'Blanc', 'Paul', 'paul.blanc@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000202', 'DISPONIBLE', 'Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Security Team Technicians
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
-- Analyste sécurité
('00000000-0000-0000-0000-000000000306', 'Noir', 'Julie', 'julie.noir@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000203', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
-- Admin identités
('00000000-0000-0000-0000-000000000307', 'Vert', 'Thomas', 'thomas.vert@alten.com', 'TECHNICIEN', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000203', 'DISPONIBLE', 'Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- ========================================
-- 6. ADD TEAM MEMBERS
-- ========================================
INSERT INTO team_members (team_id, member_id) VALUES
-- Infrastructure team
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000301'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000302'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000303'),
-- Applications team
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000304'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000305'),
-- Security team
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000306'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000307');

-- ========================================
-- 7. CREATE SAMPLE END USERS (Self-registered)
-- ========================================
INSERT INTO users (id, nom, prenom, email, role, manager_id, team_id, statut_technicien, localisation, date_creation, date_modification, actif) VALUES
('00000000-0000-0000-0000-000000000401', 'Leroy', 'Sophie', 'sophie.leroy@alten.com', 'UTILISATEUR', NULL, NULL, NULL, 'Marseille', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('00000000-0000-0000-0000-000000000402', 'Garcia', 'Miguel', 'miguel.garcia@alten.com', 'UTILISATEUR', NULL, NULL, NULL, 'Toulouse', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('00000000-0000-0000-0000-000000000403', 'Lopez', 'Carmen', 'carmen.lopez@alten.com', 'UTILISATEUR', NULL, NULL, NULL, 'Nice', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- ========================================
-- 8. ADD COMPETENCIES FOR TECHNICIANS
-- ========================================

-- Infrastructure Team Competencies
INSERT INTO competences (id, nom, description, categorie, niveau, user_id) VALUES
-- Jean Dupont (Spécialiste réseau)
('00000000-0000-0000-0000-000000000501', 'Cisco', 'Configuration routeurs et switches Cisco', 'Réseau', 'EXPERT', '00000000-0000-0000-0000-000000000301'),
('00000000-0000-0000-0000-000000000502', 'TCP/IP', 'Protocoles réseau et diagnostic', 'Réseau', 'EXPERT', '00000000-0000-0000-0000-000000000301'),
('00000000-0000-0000-0000-000000000503', 'Firewall', 'Configuration pare-feu', 'Sécurité', 'SENIOR', '00000000-0000-0000-0000-000000000301'),

-- Marie Martin (Spécialiste système)
('00000000-0000-0000-0000-000000000504', 'Windows Server', 'Administration Windows Server', 'Système', 'EXPERT', '00000000-0000-0000-0000-000000000302'),
('00000000-0000-0000-0000-000000000505', 'Linux', 'Administration systèmes Linux', 'Système', 'SENIOR', '00000000-0000-0000-0000-000000000302'),
('00000000-0000-0000-0000-000000000506', 'VMware', 'Virtualisation VMware', 'Système', 'SENIOR', '00000000-0000-0000-0000-000000000302'),

-- Luc Petit (Support desktop)
('00000000-0000-0000-0000-000000000507', 'Windows 10/11', 'Support poste de travail Windows', 'Desktop', 'SENIOR', '00000000-0000-0000-0000-000000000303'),
('00000000-0000-0000-0000-000000000508', 'Office 365', 'Support Microsoft Office', 'Applications', 'SENIOR', '00000000-0000-0000-0000-000000000303'),
('00000000-0000-0000-0000-000000000509', 'Imprimantes', 'Installation et maintenance imprimantes', 'Matériel', 'JUNIOR', '00000000-0000-0000-0000-000000000303');

-- Applications Team Competencies
INSERT INTO competences (id, nom, description, categorie, niveau, user_id) VALUES
-- Anne Roux (Support ERP)
('00000000-0000-0000-0000-000000000510', 'SAP', 'Support utilisateur SAP', 'ERP', 'EXPERT', '00000000-0000-0000-0000-000000000304'),
('00000000-0000-0000-0000-000000000511', 'Oracle EBS', 'Support Oracle E-Business Suite', 'ERP', 'SENIOR', '00000000-0000-0000-0000-000000000304'),

-- Paul Blanc (Support CRM)
('00000000-0000-0000-0000-000000000512', 'Salesforce', 'Administration Salesforce', 'CRM', 'EXPERT', '00000000-0000-0000-0000-000000000305'),
('00000000-0000-0000-0000-000000000513', 'Microsoft Dynamics', 'Support Dynamics CRM', 'CRM', 'SENIOR', '00000000-0000-0000-0000-000000000305');

-- Security Team Competencies
INSERT INTO competences (id, nom, description, categorie, niveau, user_id) VALUES
-- Julie Noir (Analyste sécurité)
('00000000-0000-0000-0000-000000000514', 'Analyse vulnérabilités', 'Audit et analyse de sécurité', 'Sécurité', 'EXPERT', '00000000-0000-0000-0000-000000000306'),
('00000000-0000-0000-0000-000000000515', 'SIEM', 'Gestion des événements de sécurité', 'Sécurité', 'SENIOR', '00000000-0000-0000-0000-000000000306'),

-- Thomas Vert (Admin identités)
('00000000-0000-0000-0000-000000000516', 'Active Directory', 'Administration AD et Azure AD', 'Identités', 'EXPERT', '00000000-0000-0000-0000-000000000307'),
('00000000-0000-0000-0000-000000000517', 'LDAP', 'Gestion annuaires LDAP', 'Identités', 'SENIOR', '00000000-0000-0000-0000-000000000307');

-- ========================================
-- 9. VERIFICATION QUERIES
-- ========================================

-- Verify the hierarchy
SELECT 'VERIFICATION: Hierarchical Structure' as info;

SELECT 
    'ADMIN' as level,
    u.nom,
    u.prenom,
    u.email,
    u.role
FROM users u 
WHERE u.role = 'ADMIN';

SELECT 
    'MANAGERS' as level,
    u.nom,
    u.prenom,
    u.email,
    u.role,
    admin.nom as created_by_admin
FROM users u
LEFT JOIN users admin ON u.manager_id = admin.id
WHERE u.role = 'MANAGER';

SELECT 
    'TEAMS' as level,
    t.nom as team_name,
    t.description,
    m.nom as manager_name
FROM teams t
LEFT JOIN users m ON t.manager_id = m.id;

SELECT 
    'TECHNICIANS' as level,
    u.nom,
    u.prenom,
    u.email,
    u.statut_technicien,
    m.nom as manager_name,
    t.nom as team_name
FROM users u
LEFT JOIN users m ON u.manager_id = m.id
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.role = 'TECHNICIEN'
ORDER BY t.nom, u.nom;

SELECT 
    'COMPETENCIES' as level,
    u.nom as technician_name,
    c.nom as competence,
    c.niveau,
    c.categorie
FROM competences c
LEFT JOIN users u ON c.user_id = u.id
ORDER BY u.nom, c.nom;

-- Summary statistics
SELECT 
    'SUMMARY' as info,
    COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as admins,
    COUNT(CASE WHEN role = 'MANAGER' THEN 1 END) as managers,
    COUNT(CASE WHEN role = 'TECHNICIEN' THEN 1 END) as technicians,
    COUNT(CASE WHEN role = 'UTILISATEUR' THEN 1 END) as end_users,
    COUNT(*) as total_users
FROM users;

COMMIT;
