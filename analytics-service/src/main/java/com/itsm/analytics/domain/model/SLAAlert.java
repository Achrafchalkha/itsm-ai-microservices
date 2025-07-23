package com.itsm.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for SLA Alerts
 * Represents alerts when tickets approach or breach SLA deadlines
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAAlert {
    
    private UUID id;
    private UUID ticketId;
    private AlertType alertType;
    private AlertLevel alertLevel;
    
    // Alert details
    private LocalDateTime slaDeadline;
    private Integer timeRemainingMinutes;
    private UUID escalatedTo;
    private LocalDateTime escalatedAt;
    
    // Resolution
    private boolean resolved;
    private LocalDateTime resolvedAt;
    private String resolutionAction;
    
    private LocalDateTime createdAt;
    
    /**
     * Factory method to create SLA alert
     */
    public static SLAAlert creerAlerte(UUID ticketId, AlertType type, AlertLevel level, 
                                      LocalDateTime slaDeadline, int timeRemainingMinutes) {
        return SLAAlert.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .alertType(type)
                .alertLevel(level)
                .slaDeadline(slaDeadline)
                .timeRemainingMinutes(timeRemainingMinutes)
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Escalate alert to manager or admin
     */
    public void escalader(UUID escalatedTo) {
        this.escalatedTo = escalatedTo;
        this.escalatedAt = LocalDateTime.now();
    }
    
    /**
     * Resolve alert with action taken
     */
    public void resoudre(String action) {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionAction = action;
    }
    
    /**
     * Check if alert is overdue (negative time remaining)
     */
    public boolean estEnRetard() {
        return timeRemainingMinutes != null && timeRemainingMinutes < 0;
    }
    
    /**
     * Check if alert is critical (very little time remaining)
     */
    public boolean estCritique() {
        return alertType == AlertType.CRITICAL || 
               (timeRemainingMinutes != null && timeRemainingMinutes <= 60); // Less than 1 hour
    }
    
    /**
     * Get urgency score for prioritization
     */
    public int getScoreUrgence() {
        int score = 0;
        
        // Base score by alert type
        score += switch (alertType) {
            case APPROACHING -> 1;
            case BREACHED -> 3;
            case CRITICAL -> 5;
        };
        
        // Additional score by alert level
        score += switch (alertLevel) {
            case MANAGER -> 1;
            case ADMIN -> 2;
        };
        
        // Additional score if overdue
        if (estEnRetard()) {
            score += Math.min(Math.abs(timeRemainingMinutes) / 60, 10); // Max 10 points for hours overdue
        }
        
        return score;
    }
    
    /**
     * Get alert priority for display
     */
    public AlertPriority getPriorite() {
        if (estCritique() || estEnRetard()) return AlertPriority.CRITIQUE;
        if (alertType == AlertType.BREACHED) return AlertPriority.HAUTE;
        if (alertLevel == AlertLevel.ADMIN) return AlertPriority.MOYENNE;
        return AlertPriority.NORMALE;
    }
    
    /**
     * Enum for alert types
     */
    public enum AlertType {
        APPROACHING("Approche de la limite SLA"),
        BREACHED("SLA dépassé"),
        CRITICAL("SLA critique");
        
        private final String description;
        
        AlertType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Enum for alert levels
     */
    public enum AlertLevel {
        MANAGER("Manager"),
        ADMIN("Administrateur");
        
        private final String description;
        
        AlertLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Enum for alert priorities
     */
    public enum AlertPriority {
        CRITIQUE("Critique"),
        HAUTE("Haute"),
        MOYENNE("Moyenne"),
        NORMALE("Normale");
        
        private final String description;
        
        AlertPriority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
