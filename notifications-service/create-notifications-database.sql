-- =====================================================
-- NOTIFICATIONS SERVICE DATABASE SETUP
-- =====================================================

-- Create notifications_db database
CREATE DATABASE notifications_db;

-- Connect to notifications_db
\c notifications_db;

-- =====================================================
-- NOTIFICATIONS TABLE
-- =====================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'TICKET_ASSIGNED', 'TICKET_REASSIGNED', 'TICKET_UPDATED', 
        'ASSIGNMENT_FAILED', 'SLA_WARNING', 'TEAM_MEMBER_ADDED',
        'SYSTEM_ALERT', 'CUSTOM'
    )),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data_json TEXT, -- Additional contextual data
    read_status BOOLEAN DEFAULT FALSE,
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    channel VARCHAR(20) DEFAULT 'DASHBOARD' CHECK (channel IN ('DASHBOARD', 'EMAIL', 'BOTH')),
    created_at TIMESTAMP DEFAULT NOW(),
    read_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    -- Context information for quick access
    ticket_id UUID,
    assignment_id UUID,
    related_user_id UUID -- For notifications about other users
);

-- =====================================================
-- NOTIFICATION PREFERENCES TABLE
-- =====================================================
CREATE TABLE notification_preferences (
    user_id UUID PRIMARY KEY,
    email_enabled BOOLEAN DEFAULT TRUE,
    dashboard_enabled BOOLEAN DEFAULT TRUE,
    email_address VARCHAR(255),
    
    -- Notification type preferences
    ticket_assigned_email BOOLEAN DEFAULT TRUE,
    ticket_reassigned_email BOOLEAN DEFAULT TRUE,
    ticket_updated_email BOOLEAN DEFAULT FALSE,
    assignment_failed_email BOOLEAN DEFAULT TRUE,
    sla_warning_email BOOLEAN DEFAULT TRUE,
    team_member_added_email BOOLEAN DEFAULT FALSE,
    
    -- Dashboard preferences
    ticket_assigned_dashboard BOOLEAN DEFAULT TRUE,
    ticket_reassigned_dashboard BOOLEAN DEFAULT TRUE,
    ticket_updated_dashboard BOOLEAN DEFAULT TRUE,
    assignment_failed_dashboard BOOLEAN DEFAULT TRUE,
    sla_warning_dashboard BOOLEAN DEFAULT TRUE,
    team_member_added_dashboard BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- EMAIL DELIVERY LOG TABLE
-- =====================================================
CREATE TABLE email_delivery_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notifications(id),
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100),
    delivery_status VARCHAR(20) DEFAULT 'PENDING' CHECK (delivery_status IN (
        'PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED'
    )),
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- NOTIFICATION TEMPLATES TABLE
-- =====================================================
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    subject_template VARCHAR(255) NOT NULL,
    body_template TEXT NOT NULL,
    email_template_path VARCHAR(255), -- Path to Thymeleaf template
    variables_json TEXT, -- Expected variables for template
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- WEBSOCKET SESSIONS TABLE (for real-time notifications)
-- =====================================================
CREATE TABLE websocket_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    connected_at TIMESTAMP DEFAULT NOW(),
    last_activity TIMESTAMP DEFAULT NOW(),
    user_agent TEXT,
    ip_address INET
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_read_status ON notifications(read_status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_priority ON notifications(priority);
CREATE INDEX idx_notifications_ticket_id ON notifications(ticket_id);
CREATE INDEX idx_notifications_assignment_id ON notifications(assignment_id);
CREATE INDEX idx_notifications_expires_at ON notifications(expires_at);

CREATE INDEX idx_email_delivery_notification_id ON email_delivery_log(notification_id);
CREATE INDEX idx_email_delivery_status ON email_delivery_log(delivery_status);
CREATE INDEX idx_email_delivery_sent_at ON email_delivery_log(sent_at);

CREATE INDEX idx_websocket_sessions_user_id ON websocket_sessions(user_id);
CREATE INDEX idx_websocket_sessions_session_id ON websocket_sessions(session_id);
CREATE INDEX idx_websocket_sessions_last_activity ON websocket_sessions(last_activity);

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

CREATE TRIGGER update_notification_preferences_updated_at 
    BEFORE UPDATE ON notification_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_templates_updated_at 
    BEFORE UPDATE ON notification_templates 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- DEFAULT NOTIFICATION TEMPLATES
-- =====================================================
INSERT INTO notification_templates (name, type, subject_template, body_template, email_template_path, variables_json) VALUES
('assignment-notification', 'TICKET_ASSIGNED', 
 'Ticket #{ticketId} assigned to you', 
 'You have been assigned to ticket #{ticketId}: {ticketTitle}',
 'assignment-notification',
 '["ticketId", "ticketTitle", "ticketDescription", "ticketPriority", "assignedBy"]'),

('reassignment-notification', 'TICKET_REASSIGNED',
 'Ticket #{ticketId} reassigned to you',
 'Ticket #{ticketId} has been reassigned to you from {previousTechnician}',
 'reassignment-notification', 
 '["ticketId", "ticketTitle", "previousTechnician", "reassignedBy", "reason"]'),

('assignment-failure-notification', 'ASSIGNMENT_FAILED',
 'Assignment failed for ticket #{ticketId}',
 'Automatic assignment failed for ticket #{ticketId}. Manual intervention required.',
 'assignment-failure-notification',
 '["ticketId", "ticketTitle", "failureReason", "ticketCategory"]'),

('sla-warning-notification', 'SLA_WARNING',
 'SLA Warning: Ticket #{ticketId}',
 'Ticket #{ticketId} is approaching SLA deadline',
 'sla-warning-notification',
 '["ticketId", "ticketTitle", "slaDeadline", "timeRemaining"]');

-- =====================================================
-- CLEANUP FUNCTION FOR OLD NOTIFICATIONS
-- =====================================================
CREATE OR REPLACE FUNCTION cleanup_old_notifications()
RETURNS void AS $$
BEGIN
    -- Delete notifications older than retention period
    DELETE FROM notifications 
    WHERE created_at < NOW() - INTERVAL '30 days'
    AND read_status = TRUE;
    
    -- Delete expired notifications
    DELETE FROM notifications 
    WHERE expires_at IS NOT NULL 
    AND expires_at < NOW();
    
    -- Delete old email delivery logs
    DELETE FROM email_delivery_log 
    WHERE created_at < NOW() - INTERVAL '90 days';
    
    -- Delete inactive websocket sessions
    DELETE FROM websocket_sessions 
    WHERE last_activity < NOW() - INTERVAL '1 day';
END;
$$ LANGUAGE plpgsql;

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

-- Check default templates
SELECT name, type, subject_template FROM notification_templates;

-- =====================================================
-- GRANTS (if needed for specific user)
-- =====================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO notifications_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO notifications_user;
