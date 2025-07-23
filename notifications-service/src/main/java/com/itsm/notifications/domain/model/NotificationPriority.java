package com.itsm.notifications.domain.model;

/**
 * Enum representing notification priority levels
 */
public enum NotificationPriority {
    
    /**
     * Low priority - informational notifications
     */
    LOW("Low", 1),
    
    /**
     * Normal priority - standard notifications
     */
    NORMAL("Normal", 2),
    
    /**
     * High priority - important notifications
     */
    HIGH("High", 3),
    
    /**
     * Urgent priority - critical notifications requiring immediate attention
     */
    URGENT("Urgent", 4);
    
    private final String displayName;
    private final int level;
    
    NotificationPriority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Get priority based on ticket priority
     */
    public static NotificationPriority fromTicketPriority(String ticketPriority) {
        if (ticketPriority == null) return NORMAL;
        
        return switch (ticketPriority.toUpperCase()) {
            case "CRITIQUE", "URGENT" -> URGENT;
            case "HAUTE", "HIGH" -> HIGH;
            case "BASSE", "LOW" -> LOW;
            default -> NORMAL;
        };
    }
    
    /**
     * Check if this priority requires immediate notification
     */
    public boolean requiresImmediateNotification() {
        return this == URGENT || this == HIGH;
    }
}
