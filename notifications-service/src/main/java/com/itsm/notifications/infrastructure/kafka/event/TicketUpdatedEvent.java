package com.itsm.notifications.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received when a ticket is updated
 * Used for notifications to users and managers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdatedEvent {

    private UUID ticketId;
    private String titre;
    private String description;
    private String statut;
    private String priorite;
    private String categorie;
    private UUID utilisateurId;
    private UUID technicianId;
    private UUID teamId;
    private String updateReason;
    private LocalDateTime updatedAt;
    private String updatedBy; // "TECHNICIAN" or "SYSTEM" or "MANAGER"
}
