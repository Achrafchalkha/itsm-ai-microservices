package com.itsm.analytics.infrastructure.persistence.mapper;

import com.itsm.analytics.domain.model.SLAAlert;
import com.itsm.analytics.infrastructure.persistence.entity.SLAAlertEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between SLA Alert domain model and JPA entity
 */
@Component
public class SLAAlertMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public SLAAlertEntity toEntity(SLAAlert domain) {
        if (domain == null) {
            return null;
        }
        
        return SLAAlertEntity.builder()
                .id(domain.getId())
                .ticketId(domain.getTicketId())
                .alertType(domain.getAlertType())
                .alertLevel(domain.getAlertLevel())
                .slaDeadline(domain.getSlaDeadline())
                .timeRemainingMinutes(domain.getTimeRemainingMinutes())
                .escalatedTo(domain.getEscalatedTo())
                .escalatedAt(domain.getEscalatedAt())
                .resolved(domain.isResolved())
                .resolvedAt(domain.getResolvedAt())
                .resolutionAction(domain.getResolutionAction())
                .createdAt(domain.getCreatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public SLAAlert toDomain(SLAAlertEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return SLAAlert.builder()
                .id(entity.getId())
                .ticketId(entity.getTicketId())
                .alertType(entity.getAlertType())
                .alertLevel(entity.getAlertLevel())
                .slaDeadline(entity.getSlaDeadline())
                .timeRemainingMinutes(entity.getTimeRemainingMinutes())
                .escalatedTo(entity.getEscalatedTo())
                .escalatedAt(entity.getEscalatedAt())
                .resolved(entity.getResolved())
                .resolvedAt(entity.getResolvedAt())
                .resolutionAction(entity.getResolutionAction())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
