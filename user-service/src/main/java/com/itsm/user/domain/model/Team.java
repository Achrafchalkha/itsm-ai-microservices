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
 * Domain model representing a team in the ITSM platform
 * Teams are managed by MANAGERs and contain TECHNICIENs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    private UUID id;
    private String nom;                     // Team name (e.g., "Équipe Infrastructure")
    private String description;             // Team description
    private UUID managerId;                 // Team manager (MANAGER role)
    
    @Builder.Default
    private List<String> categories = new ArrayList<>();  // Ticket categories handled (e.g., "Réseau", "Système")
    
    @Builder.Default
    private List<UUID> memberIds = new ArrayList<>();     // Team members (TECHNICIEN role)
    
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
    
    /**
     * Factory method to create a new team
     */
    public static Team creerEquipe(String nom, String description, UUID managerId) {
        return Team.builder()
                .id(UUID.randomUUID())
                .nom(nom)
                .description(description)
                .managerId(managerId)
                .categories(new ArrayList<>())
                .memberIds(new ArrayList<>())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }
    
    /**
     * Add a member to the team
     */
    public void ajouterMembre(UUID membreId) {
        if (!this.memberIds.contains(membreId)) {
            this.memberIds.add(membreId);
            this.dateModification = LocalDateTime.now();
        }
    }
    
    /**
     * Remove a member from the team
     */
    public void supprimerMembre(UUID membreId) {
        this.memberIds.remove(membreId);
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Add a category to the team
     */
    public void ajouterCategorie(String categorie) {
        if (!this.categories.contains(categorie)) {
            this.categories.add(categorie);
            this.dateModification = LocalDateTime.now();
        }
    }
    
    /**
     * Remove a category from the team
     */
    public void supprimerCategorie(String categorie) {
        this.categories.remove(categorie);
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Check if team handles a specific category
     */
    public boolean gereCategorieTicket(String categorie) {
        return this.categories.contains(categorie);
    }
    
    /**
     * Check if user is a member of this team
     */
    public boolean contientMembre(UUID membreId) {
        return this.memberIds.contains(membreId);
    }
    
    /**
     * Get number of team members
     */
    public int getNombreMembres() {
        return this.memberIds.size();
    }
    
    /**
     * Update modification timestamp
     */
    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }
}
