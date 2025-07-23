package com.itsm.ticket.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a ticket is created for assignment-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreatedEvent {

    private UUID ticketId;
    private String titre;
    private String description;
    private String categorie;
    private String priorite;
    private String statut;
    private UUID utilisateurId;
    private String utilisateurEmail;
    private boolean enableNlp;
    private LocalDateTime dateCreation;

    // NLP Analysis Results
    private Double nlpConfidence;
    private String nlpReasoning;

    // Assignment hints for assignment-service
    private String requiredSkills;
    private String urgencyLevel;
}
