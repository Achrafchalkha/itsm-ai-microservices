package com.itsm.analytics.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Satisfaction Score responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionScoreDTO {
    
    private UUID id;
    private UUID ticketId;
    private UUID utilisateurId;
    private UUID technicienId;
    private UUID teamId;
    private Integer score;
    private String commentaire;
    private Boolean tempsResolutionSatisfaisant;
    private Integer qualiteCommunicationScore;
    private Integer competenceTechniqueScore;
    private String satisfactionLevel;
    private LocalDateTime createdAt;
}
