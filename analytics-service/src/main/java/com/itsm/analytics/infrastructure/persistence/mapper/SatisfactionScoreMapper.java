package com.itsm.analytics.infrastructure.persistence.mapper;

import com.itsm.analytics.domain.model.SatisfactionScore;
import com.itsm.analytics.infrastructure.persistence.entity.SatisfactionScoreEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Satisfaction Score domain model and JPA entity
 */
@Component
public class SatisfactionScoreMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public SatisfactionScoreEntity toEntity(SatisfactionScore domain) {
        if (domain == null) {
            return null;
        }
        
        return SatisfactionScoreEntity.builder()
                .id(domain.getId())
                .ticketId(domain.getTicketId())
                .utilisateurId(domain.getUtilisateurId())
                .technicienId(domain.getTechnicienId())
                .teamId(domain.getTeamId())
                .score(domain.getScore())
                .commentaire(domain.getCommentaire())
                .tempsResolutionSatisfaisant(domain.getTempsResolutionSatisfaisant())
                .qualiteCommunicationScore(domain.getQualiteCommunicationScore())
                .competenceTechniqueScore(domain.getCompetenceTechniqueScore())
                .createdAt(domain.getCreatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public SatisfactionScore toDomain(SatisfactionScoreEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return SatisfactionScore.builder()
                .id(entity.getId())
                .ticketId(entity.getTicketId())
                .utilisateurId(entity.getUtilisateurId())
                .technicienId(entity.getTechnicienId())
                .teamId(entity.getTeamId())
                .score(entity.getScore())
                .commentaire(entity.getCommentaire())
                .tempsResolutionSatisfaisant(entity.getTempsResolutionSatisfaisant())
                .qualiteCommunicationScore(entity.getQualiteCommunicationScore())
                .competenceTechniqueScore(entity.getCompetenceTechniqueScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
