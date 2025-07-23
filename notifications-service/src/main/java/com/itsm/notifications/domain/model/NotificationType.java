package com.itsm.notifications.domain.model;

/**
 * Enum representing different types of notifications in the ITSM system
 */
public enum NotificationType {
    
    /**
     * Notification when a ticket is assigned to a technician
     */
    TICKET_ASSIGNED("Ticket assigned", "A ticket has been assigned to you"),
    
    /**
     * Notification when a ticket is reassigned to a different technician
     */
    TICKET_REASSIGNED("Ticket reassigned", "A ticket has been reassigned to you"),
    
    /**
     * Notification when a ticket is updated
     */
    TICKET_UPDATED("Ticket updated", "A ticket you're involved with has been updated"),
    
    /**
     * Notification when automatic assignment fails
     */
    ASSIGNMENT_FAILED("Assignment failed", "Automatic assignment failed - manual intervention required"),
    
    /**
     * Notification for SLA warnings
     */
    SLA_WARNING("SLA warning", "A ticket is approaching its SLA deadline"),
    
    /**
     * Notification when a new team member is added
     */
    TEAM_MEMBER_ADDED("Team member added", "A new member has been added to your team"),
    
    /**
     * System-wide alerts
     */
    SYSTEM_ALERT("System alert", "Important system notification"),
    
    /**
     * Custom notifications
     */
    CUSTOM("Custom notification", "Custom notification message");
    
    private final String displayName;
    private final String defaultMessage;
    
    NotificationType(String displayName, String defaultMessage) {
        this.displayName = displayName;
        this.defaultMessage = defaultMessage;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    /**
     * Check if this notification type should be sent via email by default
     */
    public boolean isEmailByDefault() {
        return switch (this) {
            case TICKET_ASSIGNED, TICKET_REASSIGNED, ASSIGNMENT_FAILED, SLA_WARNING -> true;
            case TICKET_UPDATED, TEAM_MEMBER_ADDED, SYSTEM_ALERT, CUSTOM -> false;
        };
    }
    
    /**
     * Check if this notification type should be shown on dashboard by default
     */
    public boolean isDashboardByDefault() {
        return true; // All notifications shown on dashboard by default
    }
}
