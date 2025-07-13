package com.itsm.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain model representing a user's business profile in the ITSM platform
 * Hierarchical structure: ADMIN -> MANAGER -> TECHNICIEN, UTILISATEUR (self-register)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;                        // Same ID as in auth-service
    private String nom;
    private String prenom;
    private String email;
    private Role role;

    // Hierarchical relationships
    private UUID managerId;                 // If TECHNICIEN, reference to MANAGER
    private UUID teamId;                    // Team assignment

    // Business-specific attributes
    private StatutTechnicien statutTechnicien;  // Only relevant for TECHNICIEN role
    private String localisation;               // Physical location for on-site interventions

    @Builder.Default
    private List<Competence> competences = new ArrayList<>();  // Technical skills (for TECHNICIEN)

    // Metadata
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
    
    /**
     * Factory method to create a user profile from auth service event
     */
    public static User creerDepuisAuthService(UUID authUserId, String nom, String prenom,
                                            String email, Role role, LocalDateTime dateCreation) {
        return User.builder()
                .id(authUserId)
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .role(role)
                .managerId(null)     // To be set when assigned to manager
                .teamId(null)        // To be assigned later
                .statutTechnicien(role == Role.TECHNICIEN ? StatutTechnicien.DISPONIBLE : null)
                .localisation(null)  // To be set later by manager
                .competences(new ArrayList<>())
                .dateCreation(dateCreation)
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }
    
    /**
     * Add a competence to the user
     */
    public void ajouterCompetence(Competence competence) {
        if (this.role != Role.TECHNICIEN) {
            throw new IllegalStateException("Seuls les techniciens peuvent avoir des compétences");
        }
        this.competences.add(competence);
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Remove a competence from the user
     */
    public void supprimerCompetence(UUID competenceId) {
        this.competences.removeIf(c -> c.getId().equals(competenceId));
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Update technician status
     */
    public void changerStatut(StatutTechnicien nouveauStatut) {
        if (this.role != Role.TECHNICIEN) {
            throw new IllegalStateException("Seuls les techniciens ont un statut");
        }
        this.statutTechnicien = nouveauStatut;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Assign to a team
     */
    public void assignerEquipe(UUID teamId) {
        this.teamId = teamId;
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Assign to a manager (for TECHNICIEN)
     */
    public void assignerManager(UUID managerId) {
        if (this.role != Role.TECHNICIEN) {
            throw new IllegalStateException("Seuls les techniciens peuvent être assignés à un manager");
        }
        this.managerId = managerId;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Update location
     */
    public void changerLocalisation(String nouvelleLocalisation) {
        this.localisation = nouvelleLocalisation;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Check if user is available for assignment (for technicians)
     */
    public boolean estDisponiblePourAssignation() {
        return this.role == Role.TECHNICIEN && 
               this.actif && 
               this.statutTechnicien == StatutTechnicien.DISPONIBLE;
    }
    
    /**
     * Check if user has a specific competence with minimum level
     */
    public boolean aCompetence(String nomCompetence, NiveauCompetence niveauMinimum) {
        return this.competences.stream()
                .anyMatch(c -> c.correspondA(nomCompetence, niveauMinimum));
    }

    /**
     * Check if user has a specific competence with minimum numeric level (for compatibility)
     */
    public boolean aCompetence(String nomCompetence, int niveauMinimum) {
        NiveauCompetence niveau = NiveauCompetence.values()[Math.max(0, Math.min(niveauMinimum - 1, 2))];
        return aCompetence(nomCompetence, niveau);
    }

    /**
     * Get competence level for a specific skill (numeric)
     */
    public int getNiveauCompetence(String nomCompetence) {
        return this.competences.stream()
                .filter(c -> c.getNom().equalsIgnoreCase(nomCompetence))
                .mapToInt(Competence::getNiveauNumerique)
                .max()
                .orElse(0);
    }

    /**
     * Get competence level for a specific skill (enum)
     */
    public NiveauCompetence getNiveauCompetenceEnum(String nomCompetence) {
        return this.competences.stream()
                .filter(c -> c.getNom().equalsIgnoreCase(nomCompetence))
                .map(Competence::getNiveau)
                .max((n1, n2) -> Integer.compare(n1.ordinal(), n2.ordinal()))
                .orElse(null);
    }
    
    /**
     * Update modification timestamp
     */
    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }
}
