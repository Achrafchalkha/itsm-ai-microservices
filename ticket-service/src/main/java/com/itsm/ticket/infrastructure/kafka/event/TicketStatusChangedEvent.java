package com.itsm.ticket.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a ticket status changes
 * Used for notifications to the ticket owner (user)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusChangedEvent {

    private UUID ticketId;
    private String ticketTitre;
    private UUID utilisateurId; // Ticket owner who should receive notification
    private UUID technicienId; // Technician who changed the status
    private String technicienNom;
    private String technicienPrenom;
    private String oldStatus;
    private String newStatus;
    private String changeReason;
    private LocalDateTime changedAt;
}
