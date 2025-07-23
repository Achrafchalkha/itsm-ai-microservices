package com.itsm.notifications.domain.model;

/**
 * Enum representing notification delivery channels
 */
public enum NotificationChannel {
    
    /**
     * Dashboard notification only
     */
    DASHBOARD("Dashboard"),
    
    /**
     * Email notification only
     */
    EMAIL("Email"),
    
    /**
     * Both dashboard and email
     */
    BOTH("Dashboard & Email");
    
    private final String displayName;
    
    NotificationChannel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this channel includes dashboard
     */
    public boolean includesDashboard() {
        return this == DASHBOARD || this == BOTH;
    }
    
    /**
     * Check if this channel includes email
     */
    public boolean includesEmail() {
        return this == EMAIL || this == BOTH;
    }
}
