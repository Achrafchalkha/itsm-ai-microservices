package com.itsm.notifications.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received when a ticket is assigned to a technician
 * Triggers notification creation for the assigned technician
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreatedEvent {
    
    private UUID assignmentId;
    private UUID ticketId;
    private UUID technicianId;
    private UUID teamId;
    private String assignmentStrategy;
    private java.math.BigDecimal confidenceScore;
    private String assignmentReason;
    private LocalDateTime assignedAt;
    
    // Ticket context for notifications
    private String ticketTitre;
    private String ticketDescription;
    private String ticketCategorie;
    private String ticketPriorite;
    private UUID ticketUtilisateurId;
    
    // Technician context for notifications
    private String technicianNom;
    private String technicianPrenom;
    private String technicianEmail;
    
    // Team context
    private String teamName;
    private UUID managerId;
    private String managerEmail;
}
