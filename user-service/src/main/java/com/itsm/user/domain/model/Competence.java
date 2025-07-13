package com.itsm.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain model representing a technical competency/skill
 * Used for intelligent ticket assignment based on technician expertise
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Competence {
    private UUID id;
    private String nom;                     // Skill name (e.g., "Réseau", "Windows", "SAP", "ERP")
    private String description;             // Detailed description of the skill
    private String categorie;              // Category (e.g., "Infrastructure", "Applications", "Sécurité")
    private NiveauCompetence niveau;       // Expertise level (JUNIOR, SENIOR, EXPERT)

    /**
     * Factory method to create a new competence
     */
    public static Competence creerCompetence(String nom, String description, String categorie, NiveauCompetence niveau) {
        return Competence.builder()
                .id(UUID.randomUUID())
                .nom(nom)
                .description(description)
                .categorie(categorie)
                .niveau(niveau)
                .build();
    }

    /**
     * Check if this competence matches a required skill
     */
    public boolean correspondA(String skillRequired, NiveauCompetence niveauMinimum) {
        return this.nom.equalsIgnoreCase(skillRequired) &&
               this.niveau.ordinal() >= niveauMinimum.ordinal();
    }

    /**
     * Get numeric level for compatibility
     */
    public int getNiveauNumerique() {
        return niveau.ordinal() + 1; // JUNIOR=1, SENIOR=2, EXPERT=3
    }
}
