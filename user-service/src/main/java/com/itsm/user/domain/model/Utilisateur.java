package com.itsm.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Utilisateur in user-service
 * Represents business user profiles (no password - that's in auth-service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    
    private UUID id;                    // Same ID as in auth-service
    private String nom;
    private String prenom;
    private String email;
    private Role role;
    private UUID teamId;                // Reference to team
    private String localisation;
    private String telephone;
    private String specialite;
    private String competencesJson;     // JSON storage for competencies
    private Integer chargeActuelle;     // Number of active tickets assigned (for assignment-service)
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;

    /**
     * Factory method to create a new manager
     */
    public static Utilisateur creerManager(String nom, String prenom, String email, String localisation, String telephone, String specialite) {
        return Utilisateur.builder()
                .id(UUID.randomUUID())
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .role(Role.MANAGER)
                .localisation(localisation)
                .telephone(telephone)
                .specialite(specialite)
                .chargeActuelle(0)  // Managers don't handle tickets directly
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }

    /**
     * Factory method to create a new technician
     */
    public static Utilisateur creerTechnicien(String nom, String prenom, String email, UUID teamId, String localisation, String telephone, String specialite, String competencesJson) {
        return Utilisateur.builder()
                .id(UUID.randomUUID())
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .role(Role.TECHNICIEN)
                .teamId(teamId)
                .localisation(localisation)
                .telephone(telephone)
                .specialite(specialite)
                .competencesJson(competencesJson)
                .chargeActuelle(0)  // New technicians start with 0 tickets
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }

    /**
     * Update modification timestamp
     */
    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Assign to team
     */
    public void assignerEquipe(UUID teamId) {
        this.teamId = teamId;
        mettreAJour();
    }

    /**
     * Update competencies (for technicians)
     */
    public void mettreAJourCompetences(String competencesJson) {
        this.competencesJson = competencesJson;
        mettreAJour();
    }

    /**
     * Deactivate user
     */
    public void desactiver() {
        this.actif = false;
        mettreAJour();
    }

    /**
     * Activate user
     */
    public void activer() {
        this.actif = true;
        mettreAJour();
    }

    /**
     * Increment workload (when ticket is assigned)
     */
    public void incrementerCharge() {
        if (this.chargeActuelle == null) {
            this.chargeActuelle = 0;
        }
        this.chargeActuelle++;
        mettreAJour();
    }

    /**
     * Decrement workload (when ticket is completed/closed)
     */
    public void decrementerCharge() {
        if (this.chargeActuelle == null) {
            this.chargeActuelle = 0;
        }
        if (this.chargeActuelle > 0) {
            this.chargeActuelle--;
        }
        mettreAJour();
    }

    /**
     * Set workload to specific value
     */
    public void definirCharge(int charge) {
        this.chargeActuelle = Math.max(0, charge);  // Ensure non-negative
        mettreAJour();
    }

    /**
     * Check if technician is available (active and low workload)
     */
    public boolean estDisponible() {
        return this.actif && (this.chargeActuelle == null || this.chargeActuelle < 10);  // Max 10 tickets
    }

    /**
     * Get workload level description
     */
    public String getNiveauCharge() {
        if (this.chargeActuelle == null || this.chargeActuelle == 0) {
            return "LIBRE";
        } else if (this.chargeActuelle <= 3) {
            return "FAIBLE";
        } else if (this.chargeActuelle <= 7) {
            return "MODERE";
        } else if (this.chargeActuelle <= 10) {
            return "ELEVE";
        } else {
            return "SATURE";
        }
    }
}
