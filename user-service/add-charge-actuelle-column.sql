-- Migration script to add charge_actuelle column to utilisateurs table
-- This column tracks the current workload (number of active tickets) for each technician
-- Required for assignment-service intelligent ticket assignment

-- Connect to user_db database
-- psql -U postgres -h localhost -p 5432 -d user_db

-- Add the charge_actuelle column
ALTER TABLE utilisateurs 
ADD COLUMN IF NOT EXISTS charge_actuelle INTEGER DEFAULT 0 NOT NULL;

-- Add comment to document the column purpose
COMMENT ON COLUMN utilisateurs.charge_actuelle IS 'Number of active tickets currently assigned to this technician (for assignment-service)';

-- Update existing records to have 0 workload (safe default)
UPDATE utilisateurs 
SET charge_actuelle = 0 
WHERE charge_actuelle IS NULL;

-- Add index for performance (assignment-service will query by workload frequently)
CREATE INDEX IF NOT EXISTS idx_utilisateurs_charge_actuelle 
ON utilisateurs(charge_actuelle);

-- Add composite index for team + workload queries (most common assignment query)
CREATE INDEX IF NOT EXISTS idx_utilisateurs_team_charge 
ON utilisateurs(team_id, charge_actuelle) 
WHERE role = 'TECHNICIEN' AND actif = true;

-- Add composite index for role + active + workload (for available technicians query)
CREATE INDEX IF NOT EXISTS idx_utilisateurs_role_actif_charge 
ON utilisateurs(role, actif, charge_actuelle) 
WHERE role = 'TECHNICIEN';

-- Verify the column was added successfully
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'utilisateurs' 
AND column_name = 'charge_actuelle';

-- Show sample data with the new column
SELECT 
    id,
    nom,
    prenom,
    email,
    role,
    team_id,
    charge_actuelle,
    actif
FROM utilisateurs 
WHERE role IN ('TECHNICIEN', 'MANAGER')
ORDER BY role, charge_actuelle;

-- Show indexes on the table
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'utilisateurs'
AND indexname LIKE '%charge%';

COMMIT;
