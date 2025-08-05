package com.itsm.notifications.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event received when a technician adds a note to a ticket
 * Used for notifications to the ticket owner (user)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketNoteAddedEvent {

    private UUID ticketId;
    private String ticketTitre;
    private UUID utilisateurId; // Ticket owner who should receive notification
    private UUID technicienId; // Technician who added the note
    private String technicienNom;
    private String technicienPrenom;
    private String note;
    private LocalDateTime addedAt;
}
