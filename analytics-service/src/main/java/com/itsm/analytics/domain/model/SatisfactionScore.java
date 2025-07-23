package com.itsm.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Satisfaction Score
 * Represents user feedback on resolved tickets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionScore {
    
    private UUID id;
    private UUID ticketId;
    private UUID utilisateurId;
    private UUID technicienId;
    private UUID teamId;
    private Integer score;                          // 1-5 rating
    private String commentaire;
    private Boolean tempsResolutionSatisfaisant;
    private Integer qualiteCommunicationScore;     // 1-5 rating
    private Integer competenceTechniqueScore;      // 1-5 rating
    private LocalDateTime createdAt;
    
    /**
     * Factory method to create satisfaction score
     */
    public static SatisfactionScore creerScore(UUID ticketId, UUID utilisateurId, 
                                              UUID technicienId, UUID teamId, 
                                              int score, String commentaire) {
        return SatisfactionScore.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .utilisateurId(utilisateurId)
                .technicienId(technicienId)
                .teamId(teamId)
                .score(score)
                .commentaire(commentaire)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Set detailed scores
     */
    public void definirScoresDetailles(boolean tempsResolutionSatisfaisant,
                                      int qualiteCommunication, int competenceTechnique) {
        this.tempsResolutionSatisfaisant = tempsResolutionSatisfaisant;
        this.qualiteCommunicationScore = qualiteCommunication;
        this.competenceTechniqueScore = competenceTechnique;
    }
    
    /**
     * Check if score is positive (4-5)
     */
    public boolean estPositif() {
        return this.score != null && this.score >= 4;
    }
    
    /**
     * Check if score is negative (1-2)
     */
    public boolean estNegatif() {
        return this.score != null && this.score <= 2;
    }
    
    /**
     * Check if score is neutral (3)
     */
    public boolean estNeutre() {
        return this.score != null && this.score == 3;
    }
    
    /**
     * Get overall satisfaction level
     */
    public SatisfactionLevel getNiveauSatisfaction() {
        if (score == null) return SatisfactionLevel.NON_EVALUE;
        
        return switch (score) {
            case 1 -> SatisfactionLevel.TRES_INSATISFAIT;
            case 2 -> SatisfactionLevel.INSATISFAIT;
            case 3 -> SatisfactionLevel.NEUTRE;
            case 4 -> SatisfactionLevel.SATISFAIT;
            case 5 -> SatisfactionLevel.TRES_SATISFAIT;
            default -> SatisfactionLevel.NON_EVALUE;
        };
    }
    
    /**
     * Calculate average of detailed scores
     */
    public Double getScoreMoyenDetaille() {
        if (qualiteCommunicationScore == null || competenceTechniqueScore == null) {
            return null;
        }
        return (qualiteCommunicationScore + competenceTechniqueScore) / 2.0;
    }
    
    /**
     * Enum for satisfaction levels
     */
    public enum SatisfactionLevel {
        TRES_INSATISFAIT("Très insatisfait"),
        INSATISFAIT("Insatisfait"),
        NEUTRE("Neutre"),
        SATISFAIT("Satisfait"),
        TRES_SATISFAIT("Très satisfait"),
        NON_EVALUE("Non évalué");
        
        private final String description;
        
        SatisfactionLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
