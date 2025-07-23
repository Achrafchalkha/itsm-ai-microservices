package com.itsm.assignment.domain.model;

/**
 * Enum representing assignment status
 */
public enum AssignmentStatus {
    
    /**
     * Assignment is currently active
     */
    ACTIVE("Assignment is active"),
    
    /**
     * Assignment has been reassigned to another technician
     */
    REASSIGNED("Assignment has been reassigned"),
    
    /**
     * Assignment is completed (ticket resolved)
     */
    COMPLETED("Assignment completed"),
    
    /**
     * Assignment was cancelled
     */
    CANCELLED("Assignment cancelled");
    
    private final String description;
    
    AssignmentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
