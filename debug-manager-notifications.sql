-- Debug Manager Notifications - Database Check
-- Run this in PostgreSQL to check team relationships

-- 1. Check all users and their teams
SELECT 
    u.id,
    u.nom,
    u.prenom,
    u.email,
    u.role,
    u.team_id,
    t.nom as team_name,
    t.description as team_description
FROM utilisateurs u
LEFT JOIN teams t ON u.team_id = t.id
ORDER BY u.role, u.nom;

-- 2. Check managers specifically
SELECT 
    u.id as manager_id,
    u.nom,
    u.prenom,
    u.email,
    u.team_id,
    t.nom as team_name
FROM utilisateurs u
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.role = 'MANAGER'
ORDER BY u.nom;

-- 3. Check technicians and their teams
SELECT 
    u.id as technician_id,
    u.nom,
    u.prenom,
    u.email,
    u.team_id,
    t.nom as team_name
FROM utilisateurs u
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.role = 'TECHNICIAN'
ORDER BY u.team_id, u.nom;

-- 4. Check team composition (managers and technicians per team)
SELECT 
    t.id as team_id,
    t.nom as team_name,
    COUNT(CASE WHEN u.role = 'MANAGER' THEN 1 END) as manager_count,
    COUNT(CASE WHEN u.role = 'TECHNICIAN' THEN 1 END) as technician_count,
    STRING_AGG(CASE WHEN u.role = 'MANAGER' THEN u.prenom || ' ' || u.nom END, ', ') as managers,
    STRING_AGG(CASE WHEN u.role = 'TECHNICIAN' THEN u.prenom || ' ' || u.nom END, ', ') as technicians
FROM teams t
LEFT JOIN utilisateurs u ON t.id = u.team_id
GROUP BY t.id, t.nom
ORDER BY t.nom;

-- 5. Check recent tickets and their assignments
SELECT 
    t.id as ticket_id,
    t.titre,
    t.utilisateur_id as ticket_owner,
    owner.prenom || ' ' || owner.nom as owner_name,
    a.technician_id,
    tech.prenom || ' ' || tech.nom as technician_name,
    tech.team_id,
    team.nom as team_name,
    mgr.id as manager_id,
    mgr.prenom || ' ' || mgr.nom as manager_name
FROM tickets t
LEFT JOIN utilisateurs owner ON t.utilisateur_id = owner.id
LEFT JOIN assignments a ON t.id = a.ticket_id
LEFT JOIN utilisateurs tech ON a.technician_id = tech.id
LEFT JOIN teams team ON tech.team_id = team.id
LEFT JOIN utilisateurs mgr ON team.id = mgr.team_id AND mgr.role = 'MANAGER'
WHERE t.created_at > NOW() - INTERVAL '1 day'
ORDER BY t.created_at DESC
LIMIT 10;

-- 6. Check notifications for managers
SELECT 
    n.id,
    n.user_id,
    u.prenom || ' ' || u.nom as user_name,
    u.role,
    n.type,
    n.title,
    n.message,
    n.read_status,
    n.created_at
FROM notifications n
JOIN utilisateurs u ON n.user_id = u.id
WHERE u.role = 'MANAGER'
ORDER BY n.created_at DESC
LIMIT 20;

-- 7. Check if there are any notifications at all
SELECT 
    COUNT(*) as total_notifications,
    COUNT(CASE WHEN read_status = false THEN 1 END) as unread_count,
    COUNT(CASE WHEN type = 'TICKET_ASSIGNED' THEN 1 END) as assignment_notifications
FROM notifications;

-- 8. Check notification preferences for managers
SELECT 
    np.user_id,
    u.prenom || ' ' || u.nom as manager_name,
    np.ticket_assignment_enabled,
    np.ticket_status_change_enabled,
    np.ticket_note_added_enabled,
    np.preferred_channel
FROM notification_preferences np
JOIN utilisateurs u ON np.user_id = u.id
WHERE u.role = 'MANAGER';

-- 9. Find the specific manager we're testing with
SELECT 
    u.id,
    u.nom,
    u.prenom,
    u.email,
    u.role,
    u.team_id,
    t.nom as team_name
FROM utilisateurs u
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.email = 'manager1@itsm.com';

-- 10. Check if this manager has any notifications
SELECT 
    n.*
FROM notifications n
JOIN utilisateurs u ON n.user_id = u.id
WHERE u.email = 'manager1@itsm.com'
ORDER BY n.created_at DESC;
