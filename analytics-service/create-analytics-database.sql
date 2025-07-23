-- =====================================================
-- ANALYTICS SERVICE DATABASE SETUP
-- =====================================================

-- Create analytics_db database
CREATE DATABASE analytics_db;

-- Connect to analytics_db
\c analytics_db;

-- =====================================================
-- SLA CONFIGURATION TABLE
-- =====================================================
CREATE TABLE sla_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categorie VARCHAR(50) NOT NULL,
    priorite VARCHAR(20) NOT NULL,
    delai_premiere_reponse_heures INTEGER NOT NULL, -- First response SLA in hours
    delai_resolution_heures INTEGER NOT NULL,       -- Resolution SLA in hours
    escalade_manager_heures INTEGER,                -- When to escalate to manager
    escalade_admin_heures INTEGER,                  -- When to escalate to admin
    actif BOOLEAN DEFAULT TRUE,
    created_by UUID NOT NULL,                       -- Admin who created this config
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(categorie, priorite)
);

-- =====================================================
-- SATISFACTION SCORES TABLE
-- =====================================================
CREATE TABLE satisfaction_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,                       -- Reference to ticket
    utilisateur_id UUID NOT NULL,                  -- User who gave the rating
    technicien_id UUID NOT NULL,                   -- Technician who resolved
    team_id UUID NOT NULL,                         -- Team that handled ticket
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5), -- 1-5 rating
    commentaire TEXT,                              -- Optional feedback
    temps_resolution_satisfaisant BOOLEAN,         -- Was resolution time acceptable?
    qualite_communication_score INTEGER CHECK (qualite_communication_score >= 1 AND qualite_communication_score <= 5),
    competence_technique_score INTEGER CHECK (competence_technique_score >= 1 AND competence_technique_score <= 5),
    created_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- DAILY KPI AGGREGATIONS TABLE
-- =====================================================
CREATE TABLE daily_kpis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_kpi DATE NOT NULL,
    
    -- Global metrics
    total_tickets_created INTEGER DEFAULT 0,
    total_tickets_resolved INTEGER DEFAULT 0,
    total_tickets_closed INTEGER DEFAULT 0,
    
    -- SLA metrics
    tickets_within_sla INTEGER DEFAULT 0,
    tickets_breached_sla INTEGER DEFAULT 0,
    average_resolution_time_minutes DECIMAL(10,2),
    average_first_response_time_minutes DECIMAL(10,2),
    
    -- Assignment metrics
    total_assignments INTEGER DEFAULT 0,
    total_reassignments INTEGER DEFAULT 0,
    average_assignment_confidence DECIMAL(3,2),
    
    -- Satisfaction metrics
    total_satisfaction_responses INTEGER DEFAULT 0,
    average_satisfaction_score DECIMAL(3,2),
    
    -- Team/Technician metrics (JSON for flexibility)
    team_metrics_json TEXT,                        -- JSON with per-team metrics
    technician_metrics_json TEXT,                  -- JSON with per-technician metrics
    category_metrics_json TEXT,                    -- JSON with per-category metrics
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(date_kpi)
);

-- =====================================================
-- TEAM PERFORMANCE METRICS TABLE
-- =====================================================
CREATE TABLE team_performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    
    -- Ticket volume
    tickets_assigned INTEGER DEFAULT 0,
    tickets_resolved INTEGER DEFAULT 0,
    tickets_in_progress INTEGER DEFAULT 0,
    
    -- SLA performance
    sla_compliance_rate DECIMAL(5,2),              -- Percentage
    average_resolution_time_minutes DECIMAL(10,2),
    average_first_response_time_minutes DECIMAL(10,2),
    
    -- Workload distribution
    total_workload INTEGER DEFAULT 0,
    average_workload_per_technician DECIMAL(5,2),
    max_workload_technician INTEGER DEFAULT 0,
    min_workload_technician INTEGER DEFAULT 0,
    
    -- Satisfaction
    average_satisfaction_score DECIMAL(3,2),
    total_satisfaction_responses INTEGER DEFAULT 0,
    
    -- Reassignment rate
    reassignment_rate DECIMAL(5,2),                -- Percentage of tickets reassigned
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(team_id, date_debut, date_fin)
);

-- =====================================================
-- TECHNICIAN PERFORMANCE METRICS TABLE
-- =====================================================
CREATE TABLE technician_performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    technician_id UUID NOT NULL,
    team_id UUID NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    
    -- Ticket handling
    tickets_assigned INTEGER DEFAULT 0,
    tickets_resolved INTEGER DEFAULT 0,
    tickets_in_progress INTEGER DEFAULT 0,
    current_workload INTEGER DEFAULT 0,
    
    -- Performance metrics
    average_resolution_time_minutes DECIMAL(10,2),
    average_first_response_time_minutes DECIMAL(10,2),
    sla_compliance_rate DECIMAL(5,2),
    
    -- Satisfaction
    average_satisfaction_score DECIMAL(3,2),
    total_satisfaction_responses INTEGER DEFAULT 0,
    
    -- Skill matching (from assignment-service)
    average_assignment_confidence DECIMAL(3,2),
    total_ai_assignments INTEGER DEFAULT 0,
    
    -- Reassignments
    tickets_reassigned_from INTEGER DEFAULT 0,     -- Tickets taken away
    tickets_reassigned_to INTEGER DEFAULT 0,       -- Tickets received
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(technician_id, date_debut, date_fin)
);

-- =====================================================
-- SLA ALERTS TABLE
-- =====================================================
CREATE TABLE sla_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    alert_type VARCHAR(20) NOT NULL CHECK (alert_type IN ('APPROACHING', 'BREACHED', 'CRITICAL')),
    alert_level VARCHAR(20) NOT NULL CHECK (alert_level IN ('MANAGER', 'ADMIN')),
    
    -- Alert details
    sla_deadline TIMESTAMP NOT NULL,
    time_remaining_minutes INTEGER,                 -- Negative if breached
    escalated_to UUID,                             -- Manager/Admin notified
    escalated_at TIMESTAMP,
    
    -- Resolution
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    resolution_action VARCHAR(100),                -- What action was taken
    
    created_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- MONTHLY REPORTS TABLE
-- =====================================================
CREATE TABLE monthly_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    year INTEGER NOT NULL,
    month INTEGER NOT NULL CHECK (month >= 1 AND month <= 12),
    
    -- Global KPIs
    total_tickets INTEGER DEFAULT 0,
    tickets_resolved INTEGER DEFAULT 0,
    resolution_rate DECIMAL(5,2),
    average_resolution_time_hours DECIMAL(10,2),
    sla_compliance_rate DECIMAL(5,2),
    
    -- Satisfaction
    average_satisfaction_score DECIMAL(3,2),
    satisfaction_response_rate DECIMAL(5,2),
    
    -- Team performance
    best_performing_team_id UUID,
    worst_performing_team_id UUID,
    
    -- Technician performance
    top_performer_technician_id UUID,
    most_improved_technician_id UUID,
    
    -- Trends (compared to previous month)
    ticket_volume_trend DECIMAL(5,2),              -- Percentage change
    resolution_time_trend DECIMAL(5,2),
    satisfaction_trend DECIMAL(5,2),
    
    -- Detailed metrics (JSON)
    detailed_metrics_json TEXT,
    
    generated_at TIMESTAMP DEFAULT NOW(),
    generated_by UUID,                             -- Admin who generated report
    
    UNIQUE(year, month)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- SLA Configurations
CREATE INDEX idx_sla_config_category_priority ON sla_configurations(categorie, priorite);
CREATE INDEX idx_sla_config_actif ON sla_configurations(actif);

-- Satisfaction Scores
CREATE INDEX idx_satisfaction_ticket_id ON satisfaction_scores(ticket_id);
CREATE INDEX idx_satisfaction_technician_id ON satisfaction_scores(technicien_id);
CREATE INDEX idx_satisfaction_team_id ON satisfaction_scores(team_id);
CREATE INDEX idx_satisfaction_created_at ON satisfaction_scores(created_at);

-- Daily KPIs
CREATE INDEX idx_daily_kpis_date ON daily_kpis(date_kpi);

-- Team Performance
CREATE INDEX idx_team_performance_team_id ON team_performance_metrics(team_id);
CREATE INDEX idx_team_performance_dates ON team_performance_metrics(date_debut, date_fin);

-- Technician Performance
CREATE INDEX idx_technician_performance_tech_id ON technician_performance_metrics(technician_id);
CREATE INDEX idx_technician_performance_team_id ON technician_performance_metrics(team_id);
CREATE INDEX idx_technician_performance_dates ON technician_performance_metrics(date_debut, date_fin);

-- SLA Alerts
CREATE INDEX idx_sla_alerts_ticket_id ON sla_alerts(ticket_id);
CREATE INDEX idx_sla_alerts_type_level ON sla_alerts(alert_type, alert_level);
CREATE INDEX idx_sla_alerts_resolved ON sla_alerts(resolved);
CREATE INDEX idx_sla_alerts_created_at ON sla_alerts(created_at);

-- Monthly Reports
CREATE INDEX idx_monthly_reports_year_month ON monthly_reports(year, month);

-- =====================================================
-- TRIGGERS FOR AUTOMATIC UPDATES
-- =====================================================

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
CREATE TRIGGER update_sla_configurations_updated_at BEFORE UPDATE ON sla_configurations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_daily_kpis_updated_at BEFORE UPDATE ON daily_kpis FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_team_performance_updated_at BEFORE UPDATE ON team_performance_metrics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_technician_performance_updated_at BEFORE UPDATE ON technician_performance_metrics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- DEFAULT SLA CONFIGURATIONS
-- =====================================================

-- Insert default SLA configurations for common category/priority combinations
INSERT INTO sla_configurations (categorie, priorite, delai_premiere_reponse_heures, delai_resolution_heures, escalade_manager_heures, escalade_admin_heures, created_by) VALUES
('RESEAU', 'URGENTE', 1, 4, 2, 3, '00000000-0000-0000-0000-000000000000'),
('RESEAU', 'HAUTE', 2, 8, 4, 6, '00000000-0000-0000-0000-000000000000'),
('RESEAU', 'NORMALE', 4, 24, 12, 18, '00000000-0000-0000-0000-000000000000'),
('RESEAU', 'BASSE', 8, 72, 36, 48, '00000000-0000-0000-0000-000000000000'),

('SYSTEME', 'URGENTE', 1, 4, 2, 3, '00000000-0000-0000-0000-000000000000'),
('SYSTEME', 'HAUTE', 2, 8, 4, 6, '00000000-0000-0000-0000-000000000000'),
('SYSTEME', 'NORMALE', 4, 24, 12, 18, '00000000-0000-0000-0000-000000000000'),
('SYSTEME', 'BASSE', 8, 72, 36, 48, '00000000-0000-0000-0000-000000000000'),

('LOGICIEL', 'URGENTE', 2, 6, 3, 4, '00000000-0000-0000-0000-000000000000'),
('LOGICIEL', 'HAUTE', 4, 12, 6, 8, '00000000-0000-0000-0000-000000000000'),
('LOGICIEL', 'NORMALE', 8, 48, 24, 36, '00000000-0000-0000-0000-000000000000'),
('LOGICIEL', 'BASSE', 12, 120, 72, 96, '00000000-0000-0000-0000-000000000000'),

('MATERIEL', 'URGENTE', 1, 4, 2, 3, '00000000-0000-0000-0000-000000000000'),
('MATERIEL', 'HAUTE', 2, 8, 4, 6, '00000000-0000-0000-0000-000000000000'),
('MATERIEL', 'NORMALE', 4, 24, 12, 18, '00000000-0000-0000-0000-000000000000'),
('MATERIEL', 'BASSE', 8, 72, 36, 48, '00000000-0000-0000-0000-000000000000');

-- Add comments for documentation
COMMENT ON TABLE sla_configurations IS 'SLA configuration by category and priority - managed by ADMIN';
COMMENT ON TABLE satisfaction_scores IS 'User satisfaction ratings for resolved tickets';
COMMENT ON TABLE daily_kpis IS 'Daily aggregated KPIs for performance monitoring';
COMMENT ON TABLE team_performance_metrics IS 'Team performance metrics for MANAGER dashboards';
COMMENT ON TABLE technician_performance_metrics IS 'Individual technician performance metrics';
COMMENT ON TABLE sla_alerts IS 'SLA breach alerts and escalations';
COMMENT ON TABLE monthly_reports IS 'Monthly performance reports for ADMIN';

-- Grant permissions (adjust as needed for your setup)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO analytics_service_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO analytics_service_user;
