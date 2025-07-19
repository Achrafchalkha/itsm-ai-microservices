-- Create ticket_db database and tickets table with ALL SLA and analytics columns
-- Required for assignment-service and analytics-service integration

-- Create database (run as postgres superuser)
-- CREATE DATABASE ticket_db OWNER postgres;

-- Connect to ticket_db database
-- \c ticket_db;

-- Create tickets table with complete SLA and analytics tracking
CREATE TABLE IF NOT EXISTS tickets (
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Basic ticket information
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    statut VARCHAR(20) NOT NULL DEFAULT 'OUVERT',
    priorite VARCHAR(20) NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    
    -- User and assignment information
    utilisateur_id UUID NOT NULL,  -- User who created the ticket
    technicien_id UUID,            -- Assigned technician
    team_id UUID,                  -- Assigned team
    
    -- ✅ SLA TRACKING COLUMNS (Required for analytics-service)
    date_limite_sla TIMESTAMP,                    -- SLA deadline
    date_premiere_reponse TIMESTAMP,              -- First response timestamp
    sla_respecte BOOLEAN,                         -- Was SLA met? (null = in progress)
    temps_resolution_minutes INTEGER,             -- Total resolution time
    statut_sla VARCHAR(20) DEFAULT 'DANS_LES_TEMPS', -- Current SLA status
    
    -- ✅ ANALYTICS COLUMNS (Required for performance tracking)
    nombre_reassignations INTEGER DEFAULT 0,      -- Number of times reassigned
    temps_premiere_reponse_minutes INTEGER,       -- Time to first response
    
    -- Assignment-service integration
    enable_nlp BOOLEAN DEFAULT TRUE,              -- Enable NLP processing
    
    -- Timestamps
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fermeture TIMESTAMP,                     -- When ticket was closed
    
    -- Additional metadata
    commentaire_resolution TEXT,                  -- Resolution notes
    fichiers_attaches TEXT,                       -- JSON array of attached files
    actif BOOLEAN DEFAULT TRUE                    -- Soft delete flag
);

-- Add constraints
ALTER TABLE tickets ADD CONSTRAINT chk_statut 
CHECK (statut IN ('OUVERT', 'EN_ATTENTE', 'EN_COURS', 'RESOLU', 'FERME', 'ANNULE'));

ALTER TABLE tickets ADD CONSTRAINT chk_priorite 
CHECK (priorite IN ('BASSE', 'NORMALE', 'HAUTE', 'CRITIQUE'));

ALTER TABLE tickets ADD CONSTRAINT chk_statut_sla 
CHECK (statut_sla IN ('DANS_LES_TEMPS', 'EN_RETARD', 'CRITIQUE'));

ALTER TABLE tickets ADD CONSTRAINT chk_nombre_reassignations 
CHECK (nombre_reassignations >= 0);

-- Create indexes for performance (analytics and assignment queries)

-- Basic query indexes
CREATE INDEX IF NOT EXISTS idx_tickets_statut ON tickets(statut);
CREATE INDEX IF NOT EXISTS idx_tickets_priorite ON tickets(priorite);
CREATE INDEX IF NOT EXISTS idx_tickets_categorie ON tickets(categorie);
CREATE INDEX IF NOT EXISTS idx_tickets_utilisateur ON tickets(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_tickets_technicien ON tickets(technicien_id);
CREATE INDEX IF NOT EXISTS idx_tickets_team ON tickets(team_id);

-- SLA and analytics indexes
CREATE INDEX IF NOT EXISTS idx_tickets_date_limite_sla ON tickets(date_limite_sla);
CREATE INDEX IF NOT EXISTS idx_tickets_statut_sla ON tickets(statut_sla);
CREATE INDEX IF NOT EXISTS idx_tickets_sla_respecte ON tickets(sla_respecte);
CREATE INDEX IF NOT EXISTS idx_tickets_date_creation ON tickets(date_creation);
CREATE INDEX IF NOT EXISTS idx_tickets_date_fermeture ON tickets(date_fermeture);

-- Composite indexes for common analytics queries
CREATE INDEX IF NOT EXISTS idx_tickets_team_statut ON tickets(team_id, statut) WHERE actif = true;
CREATE INDEX IF NOT EXISTS idx_tickets_technicien_statut ON tickets(technicien_id, statut) WHERE actif = true;
CREATE INDEX IF NOT EXISTS idx_tickets_categorie_priorite ON tickets(categorie, priorite) WHERE actif = true;
CREATE INDEX IF NOT EXISTS idx_tickets_sla_overdue ON tickets(date_limite_sla, statut) 
WHERE statut NOT IN ('FERME', 'RESOLU') AND actif = true;

-- Performance tracking indexes
CREATE INDEX IF NOT EXISTS idx_tickets_resolution_time ON tickets(temps_resolution_minutes) 
WHERE temps_resolution_minutes IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_tickets_response_time ON tickets(temps_premiere_reponse_minutes) 
WHERE temps_premiere_reponse_minutes IS NOT NULL;

-- Add comments for documentation
COMMENT ON TABLE tickets IS 'Tickets table with complete SLA tracking and analytics support';
COMMENT ON COLUMN tickets.date_limite_sla IS 'SLA deadline for ticket resolution';
COMMENT ON COLUMN tickets.date_premiere_reponse IS 'Timestamp of first response to ticket';
COMMENT ON COLUMN tickets.sla_respecte IS 'Whether SLA was met (null = in progress)';
COMMENT ON COLUMN tickets.temps_resolution_minutes IS 'Total time to resolve ticket in minutes';
COMMENT ON COLUMN tickets.statut_sla IS 'Current SLA status for monitoring';
COMMENT ON COLUMN tickets.nombre_reassignations IS 'Number of times ticket was reassigned';
COMMENT ON COLUMN tickets.temps_premiere_reponse_minutes IS 'Time to first response in minutes';
COMMENT ON COLUMN tickets.enable_nlp IS 'Enable NLP processing for intelligent assignment';

-- Create trigger to update date_modification automatically
CREATE OR REPLACE FUNCTION update_date_modification()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_modification = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_date_modification
    BEFORE UPDATE ON tickets
    FOR EACH ROW
    EXECUTE FUNCTION update_date_modification();

-- Verify table creation
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'tickets' 
ORDER BY ordinal_position;

-- Show indexes
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'tickets'
ORDER BY indexname;

COMMIT;
