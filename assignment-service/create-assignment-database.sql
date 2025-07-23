-- =====================================================
-- ASSIGNMENT SERVICE DATABASE SETUP
-- =====================================================

-- Create assignment_db database
CREATE DATABASE assignment_db;

-- Connect to assignment_db
\c assignment_db;

-- =====================================================
-- ASSIGNMENTS TABLE
-- =====================================================
CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    technician_id UUID NOT NULL,
    team_id UUID NOT NULL,
    assignment_strategy VARCHAR(20) NOT NULL CHECK (assignment_strategy IN ('LEAST_WORKLOAD', 'BEST_SKILL', 'HYBRID')),
    confidence_score DECIMAL(3,2) CHECK (confidence_score >= 0.00 AND confidence_score <= 1.00),
    assignment_reason TEXT,
    nlp_analysis_json TEXT, -- JSON result from Gemini analysis
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REASSIGNED', 'COMPLETED', 'CANCELLED')),
    reassigned_by UUID, -- Manager who reassigned
    reassignment_reason TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- ASSIGNMENT HISTORY TABLE
-- =====================================================
CREATE TABLE assignment_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES assignments(id),
    previous_technician_id UUID,
    new_technician_id UUID NOT NULL,
    reassigned_by UUID NOT NULL,
    reassignment_reason TEXT,
    reassigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    assignment_strategy VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(3,2)
);

-- =====================================================
-- ASSIGNMENT METRICS TABLE (for analytics)
-- =====================================================
CREATE TABLE assignment_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES assignments(id),
    metric_type VARCHAR(50) NOT NULL, -- 'ASSIGNMENT_TIME', 'RESOLUTION_TIME', 'SATISFACTION_SCORE'
    metric_value DECIMAL(10,2) NOT NULL,
    measured_at TIMESTAMP NOT NULL DEFAULT NOW(),
    additional_data_json TEXT
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX idx_assignments_ticket_id ON assignments(ticket_id);
CREATE INDEX idx_assignments_technician_id ON assignments(technician_id);
CREATE INDEX idx_assignments_team_id ON assignments(team_id);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_assigned_at ON assignments(assigned_at);
CREATE INDEX idx_assignments_strategy ON assignments(assignment_strategy);

CREATE INDEX idx_assignment_history_assignment_id ON assignment_history(assignment_id);
CREATE INDEX idx_assignment_history_reassigned_at ON assignment_history(reassigned_at);

CREATE INDEX idx_assignment_metrics_assignment_id ON assignment_metrics(assignment_id);
CREATE INDEX idx_assignment_metrics_type ON assignment_metrics(metric_type);
CREATE INDEX idx_assignment_metrics_measured_at ON assignment_metrics(measured_at);

-- =====================================================
-- TRIGGERS FOR UPDATED_AT
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_assignments_updated_at 
    BEFORE UPDATE ON assignments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SAMPLE DATA FOR TESTING
-- =====================================================
-- Note: This will be populated by the application
-- Sample assignment strategies and their usage

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
-- Check tables created
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Check indexes
SELECT indexname, tablename FROM pg_indexes 
WHERE schemaname = 'public' 
ORDER BY tablename, indexname;

-- =====================================================
-- GRANTS (if needed for specific user)
-- =====================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO assignment_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO assignment_user;
