package com.itsm.ticket.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Ticket information
 * Used in REST API responses for ticket data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    
    private UUID id;
    private String titre;
    private String description;
    private String statut;
    private String priorite;
    private String categorie;
    
    // Assignment information
    private UUID utilisateurId;
    private UUID technicienId;
    private UUID teamId;
    
    // SLA and timing information
    private LocalDateTime dateLimiteSla;
    private LocalDateTime datePremiereReponse;
    private Boolean slaRespecte;
    private Integer tempsResolutionMinutes;
    private Integer tempsPremiereReponseMinutes;
    
    // Analytics information
    private Integer nombreReassignations;
    
    // Metadata
    private Boolean enableNlp;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateFermeture;
    private Boolean actif;
    
    // Resolution information
    private String commentaireResolution;
    
    // Additional fields for technician view
    private String statutSla;
    private String urgencyLevel;
    private Boolean isOverdue;
    private Integer minutesUntilSlaDeadline;
    
    /**
     * Check if ticket is overdue
     */
    public Boolean getIsOverdue() {
        if (dateLimiteSla == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dateLimiteSla) && 
               !"RESOLU".equals(statut) && !"FERME".equals(statut);
    }
    
    /**
     * Get minutes until SLA deadline
     */
    public Integer getMinutesUntilSlaDeadline() {
        if (dateLimiteSla == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateLimiteSla)) {
            return 0; // Already overdue
        }
        return (int) java.time.temporal.ChronoUnit.MINUTES.between(now, dateLimiteSla);
    }
    
    /**
     * Get urgency level based on priority and SLA status
     */
    public String getUrgencyLevel() {
        if (getIsOverdue()) {
            return "CRITICAL";
        }
        
        if (dateLimiteSla != null) {
            Integer minutesLeft = getMinutesUntilSlaDeadline();
            if (minutesLeft != null && minutesLeft < 60) {
                return "URGENT";
            }
        }
        
        return switch (priorite) {
            case "CRITIQUE" -> "CRITICAL";
            case "HAUTE" -> "HIGH";
            case "NORMALE" -> "MEDIUM";
            case "BASSE" -> "LOW";
            default -> "MEDIUM";
        };
    }
}
