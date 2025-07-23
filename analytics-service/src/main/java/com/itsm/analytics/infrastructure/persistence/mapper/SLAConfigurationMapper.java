package com.itsm.analytics.infrastructure.persistence.mapper;

import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.infrastructure.persistence.entity.SLAConfigurationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between SLA Configuration domain model and JPA entity
 */
@Component
public class SLAConfigurationMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public SLAConfigurationEntity toEntity(SLAConfiguration domain) {
        if (domain == null) {
            return null;
        }
        
        return SLAConfigurationEntity.builder()
                .id(domain.getId())
                .categorie(domain.getCategorie())
                .priorite(domain.getPriorite())
                .delaiPremiereReponseHeures(domain.getDelaiPremiereReponseHeures())
                .delaiResolutionHeures(domain.getDelaiResolutionHeures())
                .escaladeManagerHeures(domain.getEscaladeManagerHeures())
                .escaladeAdminHeures(domain.getEscaladeAdminHeures())
                .actif(domain.isActif())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public SLAConfiguration toDomain(SLAConfigurationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return SLAConfiguration.builder()
                .id(entity.getId())
                .categorie(entity.getCategorie())
                .priorite(entity.getPriorite())
                .delaiPremiereReponseHeures(entity.getDelaiPremiereReponseHeures())
                .delaiResolutionHeures(entity.getDelaiResolutionHeures())
                .escaladeManagerHeures(entity.getEscaladeManagerHeures())
                .escaladeAdminHeures(entity.getEscaladeAdminHeures())
                .actif(entity.getActif())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
