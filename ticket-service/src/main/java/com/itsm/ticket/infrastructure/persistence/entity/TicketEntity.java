package com.itsm.ticket.infrastructure.persistence.entity;

import com.itsm.ticket.domain.model.PrioriteTicket;
import com.itsm.ticket.domain.model.StatutSLA;
import com.itsm.ticket.domain.model.StatutTicket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Ticket with complete SLA and analytics tracking
 * Contains all columns required for assignment-service and analytics-service
 */
@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    // Basic ticket information
    @Column(name = "titre", nullable = false, length = 255)
    private String titre;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutTicket statut;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false, length = 20)
    private PrioriteTicket priorite;
    
    @Column(name = "categorie", nullable = false, length = 50)
    private String categorie;  // RESEAU, SYSTEME, MATERIEL, etc.
    
    // User and assignment information
    @Column(name = "utilisateur_id", nullable = false, columnDefinition = "UUID")
    private UUID utilisateurId;  // User who created the ticket
    
    @Column(name = "technicien_id", columnDefinition = "UUID")
    private UUID technicienId;   // Assigned technician
    
    @Column(name = "team_id", columnDefinition = "UUID")
    private UUID teamId;         // Assigned team
    
    // ✅ SLA TRACKING COLUMNS (Required for analytics-service)
    @Column(name = "date_limite_sla")
    private LocalDateTime dateLimiteSla;  // SLA deadline
    
    @Column(name = "date_premiere_reponse")
    private LocalDateTime datePremiereReponse;  // First response timestamp
    
    @Column(name = "sla_respecte")
    private Boolean slaRespecte;  // Was SLA met? (null = in progress)
    
    @Column(name = "temps_resolution_minutes")
    private Integer tempsResolutionMinutes;  // Total resolution time
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_sla", length = 20)
    @Builder.Default
    private StatutSLA statutSla = StatutSLA.DANS_LES_TEMPS;  // Current SLA status
    
    // ✅ ANALYTICS COLUMNS (Required for performance tracking)
    @Column(name = "nombre_reassignations")
    @Builder.Default
    private Integer nombreReassignations = 0;  // Number of times reassigned
    
    @Column(name = "temps_premiere_reponse_minutes")
    private Integer tempsPremiereReponseMinutes;  // Time to first response
    
    // Assignment-service integration
    @Column(name = "enable_nlp")
    @Builder.Default
    private Boolean enableNlp = true;  // Enable NLP processing for assignment
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @UpdateTimestamp
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;
    
    @Column(name = "date_fermeture")
    private LocalDateTime dateFermeture;  // When ticket was closed
    
    // Additional metadata
    @Column(name = "commentaire_resolution", columnDefinition = "TEXT")
    private String commentaireResolution;  // Resolution notes
    
    @Column(name = "fichiers_attaches", columnDefinition = "TEXT")
    private String fichiersAttaches;  // JSON array of attached files
    
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;  // Soft delete flag
}
