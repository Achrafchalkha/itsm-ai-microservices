package com.itsm.analytics.infrastructure.persistence.repository;

import com.itsm.analytics.domain.model.SLAAlert;
import com.itsm.analytics.infrastructure.persistence.entity.SLAAlertEntity;
import com.itsm.analytics.infrastructure.persistence.mapper.SLAAlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for SLA Alert
 * Provides data access layer for SLA alerts and escalations
 */
@Repository
@RequiredArgsConstructor
public class JpaSLAAlertRepository {
    
    private final SLAAlertJpaRepository jpaRepository;
    private final SLAAlertMapper mapper;
    
    public SLAAlert save(SLAAlert slaAlert) {
        SLAAlertEntity entity = mapper.toEntity(slaAlert);
        SLAAlertEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    public Optional<SLAAlert> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    public List<SLAAlert> findByTicketId(UUID ticketId) {
        return jpaRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SLAAlert> findByTicketIdAndResolved(UUID ticketId, boolean resolved) {
        return jpaRepository.findByTicketIdAndResolvedOrderByCreatedAtDesc(ticketId, resolved)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SLAAlert> findByTicketIdAndAlertTypeAndResolved(UUID ticketId, SLAAlert.AlertType alertType, boolean resolved) {
        return jpaRepository.findByTicketIdAndAlertTypeAndResolvedOrderByCreatedAtDesc(ticketId, alertType, resolved)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SLAAlert> findByResolvedOrderByCreatedAtDesc(boolean resolved) {
        return jpaRepository.findByResolvedOrderByCreatedAtDesc(resolved)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SLAAlert> findByResolvedAndEscalatedAtIsNull(boolean resolved) {
        return jpaRepository.findByResolvedAndEscalatedAtIsNull(resolved)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SLAAlert> findByAlertTypeAndResolved(SLAAlert.AlertType alertType, boolean resolved) {
        return jpaRepository.findByAlertTypeAndResolvedOrderByCreatedAtDesc(alertType, resolved)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SLAAlert> findByAlertLevelAndResolved(SLAAlert.AlertLevel alertLevel, boolean resolved) {
        return jpaRepository.findByAlertLevelAndResolvedOrderByCreatedAtDesc(alertLevel, resolved)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Page<SLAAlert> findByResolved(boolean resolved, Pageable pageable) {
        return jpaRepository.findByResolvedOrderByCreatedAtDesc(resolved, pageable)
                .map(mapper::toDomain);
    }
    
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    public void deleteByResolvedAndCreatedAtBefore(boolean resolved, LocalDateTime date) {
        jpaRepository.deleteByResolvedAndCreatedAtBefore(resolved, date);
    }
    
    public long countByResolved(boolean resolved) {
        return jpaRepository.countByResolved(resolved);
    }
    
    public long countByAlertTypeAndResolved(SLAAlert.AlertType alertType, boolean resolved) {
        return jpaRepository.countByAlertTypeAndResolved(alertType, resolved);
    }
}

/**
 * Spring Data JPA Repository interface
 */
interface SLAAlertJpaRepository extends JpaRepository<SLAAlertEntity, UUID> {
    
    List<SLAAlertEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
    
    List<SLAAlertEntity> findByTicketIdAndResolvedOrderByCreatedAtDesc(UUID ticketId, Boolean resolved);
    
    List<SLAAlertEntity> findByTicketIdAndAlertTypeAndResolvedOrderByCreatedAtDesc(UUID ticketId, SLAAlert.AlertType alertType, Boolean resolved);
    
    List<SLAAlertEntity> findByResolvedOrderByCreatedAtDesc(Boolean resolved);
    
    List<SLAAlertEntity> findByResolvedAndEscalatedAtIsNull(Boolean resolved);
    
    List<SLAAlertEntity> findByAlertTypeAndResolvedOrderByCreatedAtDesc(SLAAlert.AlertType alertType, Boolean resolved);
    
    List<SLAAlertEntity> findByAlertLevelAndResolvedOrderByCreatedAtDesc(SLAAlert.AlertLevel alertLevel, Boolean resolved);
    
    Page<SLAAlertEntity> findByResolvedOrderByCreatedAtDesc(Boolean resolved, Pageable pageable);
    
    void deleteByResolvedAndCreatedAtBefore(Boolean resolved, LocalDateTime date);
    
    long countByResolved(Boolean resolved);
    
    long countByAlertTypeAndResolved(SLAAlert.AlertType alertType, Boolean resolved);
    
    @Query("SELECT COUNT(a) FROM SLAAlertEntity a WHERE a.resolved = false AND a.timeRemainingMinutes < 0")
    long countBreachedAlerts();
    
    @Query("SELECT COUNT(a) FROM SLAAlertEntity a WHERE a.resolved = false AND a.timeRemainingMinutes BETWEEN 0 AND :minutes")
    long countApproachingAlerts(@Param("minutes") int minutes);
    
    @Query("SELECT a FROM SLAAlertEntity a WHERE a.resolved = false AND a.escalatedAt IS NULL AND a.createdAt < :threshold")
    List<SLAAlertEntity> findAlertsRequiringEscalation(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT a FROM SLAAlertEntity a WHERE a.resolved = false AND a.alertType = 'CRITICAL'")
    List<SLAAlertEntity> findCriticalAlerts();
}
