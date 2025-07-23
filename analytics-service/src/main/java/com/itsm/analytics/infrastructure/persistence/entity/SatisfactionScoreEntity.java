package com.itsm.analytics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Satisfaction Score
 * Maps to satisfaction_scores table in analytics_db
 */
@Entity
@Table(name = "satisfaction_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionScoreEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "ticket_id", nullable = false, columnDefinition = "UUID")
    private UUID ticketId;
    
    @Column(name = "utilisateur_id", nullable = false, columnDefinition = "UUID")
    private UUID utilisateurId;
    
    @Column(name = "technicien_id", nullable = false, columnDefinition = "UUID")
    private UUID technicienId;
    
    @Column(name = "team_id", nullable = false, columnDefinition = "UUID")
    private UUID teamId;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
    
    @Column(name = "temps_resolution_satisfaisant")
    private Boolean tempsResolutionSatisfaisant;
    
    @Column(name = "qualite_communication_score")
    private Integer qualiteCommunicationScore;
    
    @Column(name = "competence_technique_score")
    private Integer competenceTechniqueScore;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
