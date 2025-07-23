package com.itsm.ticket.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Domain model for Ticket with SLA and analytics capabilities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    private UUID id;
    private String titre;
    private String description;
    private StatutTicket statut;
    private PrioriteTicket priorite;
    private String categorie;
    
    // Assignment
    private UUID utilisateurId;
    private UUID technicienId;
    private UUID teamId;
    
    // SLA tracking
    private LocalDateTime dateLimiteSla;
    private LocalDateTime datePremiereReponse;
    private Boolean slaRespecte;
    private Integer tempsResolutionMinutes;
    private StatutSLA statutSla;
    
    // Analytics
    private Integer nombreReassignations;
    private Integer tempsPremiereReponseMinutes;
    
    // Assignment-service
    private Boolean enableNlp;
    
    // Timestamps
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime dateFermeture;
    
    // Additional
    private String commentaireResolution;
    private String fichiersAttaches;
    private Boolean actif;
    
    /**
     * Factory method to create a new ticket
     */
    public static Ticket creerTicket(String titre, String description, PrioriteTicket priorite,
                                   String categorie, UUID utilisateurId) {
        return Ticket.builder()
                .id(UUID.randomUUID())
                .titre(titre)
                .description(description)
                .statut(StatutTicket.EN_COURS)  // Changed: New tickets start as EN_COURS
                .priorite(priorite)
                .categorie(categorie)
                .utilisateurId(utilisateurId)
                .statutSla(StatutSLA.DANS_LES_TEMPS)
                .nombreReassignations(0)
                .enableNlp(true)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }
    
    /**
     * Assign ticket to technician
     */
    public void assignerTechnicien(UUID technicienId, UUID teamId) {
        this.technicienId = technicienId;
        this.teamId = teamId;
        // Keep current status (EN_COURS) - don't change it during assignment
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Reassign ticket (increment reassignment counter)
     */
    public void reassigner(UUID nouveauTechnicienId, UUID nouvelleTeamId) {
        this.technicienId = nouveauTechnicienId;
        this.teamId = nouvelleTeamId;
        this.nombreReassignations++;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Mark first response received
     */
    public void marquerPremiereReponse() {
        if (this.datePremiereReponse == null) {
            this.datePremiereReponse = LocalDateTime.now();
            this.tempsPremiereReponseMinutes = (int) ChronoUnit.MINUTES.between(
                this.dateCreation, this.datePremiereReponse);
            this.dateModification = LocalDateTime.now();
        }
    }
    
    /**
     * Resolve ticket
     */
    public void resoudre(String commentaireResolution) {
        this.statut = StatutTicket.RESOLU;
        this.commentaireResolution = commentaireResolution;
        this.dateModification = LocalDateTime.now();
        calculerTempsResolution();
        evaluerSLA();
    }
    
    /**
     * Close ticket
     */
    public void fermer() {
        this.statut = StatutTicket.FERME;
        this.dateFermeture = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        if (this.tempsResolutionMinutes == null) {
            calculerTempsResolution();
        }
        evaluerSLA();
    }
    
    /**
     * Calculate resolution time
     */
    private void calculerTempsResolution() {
        LocalDateTime finResolution = this.dateFermeture != null ? 
            this.dateFermeture : LocalDateTime.now();
        this.tempsResolutionMinutes = (int) ChronoUnit.MINUTES.between(
            this.dateCreation, finResolution);
    }
    
    /**
     * Evaluate SLA compliance
     */
    private void evaluerSLA() {
        if (this.dateLimiteSla != null) {
            LocalDateTime maintenant = LocalDateTime.now();
            if (this.statut == StatutTicket.FERME || this.statut == StatutTicket.RESOLU) {
                // Ticket is resolved/closed
                LocalDateTime finResolution = this.dateFermeture != null ? 
                    this.dateFermeture : maintenant;
                this.slaRespecte = finResolution.isBefore(this.dateLimiteSla) || 
                                 finResolution.isEqual(this.dateLimiteSla);
                this.statutSla = this.slaRespecte ? StatutSLA.DANS_LES_TEMPS : StatutSLA.EN_RETARD;
            } else {
                // Ticket still open
                if (maintenant.isAfter(this.dateLimiteSla)) {
                    this.statutSla = StatutSLA.EN_RETARD;
                } else if (maintenant.plusHours(2).isAfter(this.dateLimiteSla)) {
                    this.statutSla = StatutSLA.CRITIQUE;
                } else {
                    this.statutSla = StatutSLA.DANS_LES_TEMPS;
                }
            }
        }
    }
    
    /**
     * Check if ticket is overdue
     */
    public boolean estEnRetard() {
        return this.dateLimiteSla != null && 
               LocalDateTime.now().isAfter(this.dateLimiteSla) &&
               (this.statut != StatutTicket.FERME && this.statut != StatutTicket.RESOLU);
    }
    
    /**
     * Update modification timestamp
     */
    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }
}
