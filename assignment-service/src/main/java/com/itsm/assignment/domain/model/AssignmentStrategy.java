package com.itsm.assignment.domain.model;

/**
 * Enum representing different assignment strategies
 * Used to determine how tickets are assigned to technicians
 */
public enum AssignmentStrategy {
    
    /**
     * Assign to technician with lowest current workload
     */
    LEAST_WORKLOAD("Assign to technician with lowest workload"),
    
    /**
     * Assign based on best skill match with ticket requirements
     */
    BEST_SKILL("Assign based on best skill match"),
    
    /**
     * Hybrid approach combining workload and skill matching
     */
    HYBRID("Hybrid approach: workload + skill matching");
    
    private final String description;
    
    AssignmentStrategy(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get default strategy from configuration
     */
    public static AssignmentStrategy getDefault() {
        return HYBRID;
    }
}
