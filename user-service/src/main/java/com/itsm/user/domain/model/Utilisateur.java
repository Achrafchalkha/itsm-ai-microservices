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
}
