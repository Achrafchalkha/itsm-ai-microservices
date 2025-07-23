package com.itsm.analytics.infrastructure.persistence.repository;

import com.itsm.analytics.domain.model.TeamPerformanceMetrics;
import com.itsm.analytics.infrastructure.persistence.entity.TeamPerformanceMetricsEntity;
import com.itsm.analytics.infrastructure.persistence.mapper.TeamPerformanceMetricsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Team Performance Metrics
 * Provides data access layer for team performance analytics
 */
@Repository
@RequiredArgsConstructor
public class JpaTeamPerformanceMetricsRepository {
    
    private final TeamPerformanceMetricsJpaRepository jpaRepository;
    private final TeamPerformanceMetricsMapper mapper;
    
    public TeamPerformanceMetrics save(TeamPerformanceMetrics teamMetrics) {
        TeamPerformanceMetricsEntity entity = mapper.toEntity(teamMetrics);
        TeamPerformanceMetricsEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    public Optional<TeamPerformanceMetrics> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    public List<TeamPerformanceMetrics> findByTeamId(UUID teamId) {
        return jpaRepository.findByTeamIdOrderByDateDebutDesc(teamId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TeamPerformanceMetrics> findByTeamIdAndPeriodeType(UUID teamId, TeamPerformanceMetrics.PeriodeType periodeType) {
        return jpaRepository.findByTeamIdAndPeriodeTypeOrderByDateDebutDesc(teamId, periodeType.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Optional<TeamPerformanceMetrics> findByTeamIdAndPeriodeTypeAndDateDebut(UUID teamId, 
                                                                                   TeamPerformanceMetrics.PeriodeType periodeType, 
                                                                                   LocalDate dateDebut) {
        return jpaRepository.findByTeamIdAndPeriodeTypeAndDateDebut(teamId, periodeType.name(), dateDebut)
                .map(mapper::toDomain);
    }
    
    public List<TeamPerformanceMetrics> findByPeriodeTypeAndDateDebutBetween(TeamPerformanceMetrics.PeriodeType periodeType,
                                                                             LocalDate startDate, 
                                                                             LocalDate endDate) {
        return jpaRepository.findByPeriodeTypeAndDateDebutBetweenOrderByDateDebutDesc(periodeType.name(), startDate, endDate)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TeamPerformanceMetrics> findLatestByPeriodeType(TeamPerformanceMetrics.PeriodeType periodeType) {
        return jpaRepository.findLatestByPeriodeType(periodeType.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Page<TeamPerformanceMetrics> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
    
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    public void deleteByTeamIdAndPeriodeTypeAndDateDebutBefore(UUID teamId, 
                                                               TeamPerformanceMetrics.PeriodeType periodeType, 
                                                               LocalDate cutoffDate) {
        jpaRepository.deleteByTeamIdAndPeriodeTypeAndDateDebutBefore(teamId, periodeType.name(), cutoffDate);
    }
    
    public long countByTeamId(UUID teamId) {
        return jpaRepository.countByTeamId(teamId);
    }
}

/**
 * Spring Data JPA Repository interface
 */
interface TeamPerformanceMetricsJpaRepository extends JpaRepository<TeamPerformanceMetricsEntity, UUID> {
    
    List<TeamPerformanceMetricsEntity> findByTeamIdOrderByDateDebutDesc(UUID teamId);
    
    List<TeamPerformanceMetricsEntity> findByTeamIdAndPeriodeTypeOrderByDateDebutDesc(UUID teamId, String periodeType);
    
    Optional<TeamPerformanceMetricsEntity> findByTeamIdAndPeriodeTypeAndDateDebut(UUID teamId, String periodeType, LocalDate dateDebut);
    
    List<TeamPerformanceMetricsEntity> findByPeriodeTypeAndDateDebutBetweenOrderByDateDebutDesc(String periodeType, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM TeamPerformanceMetricsEntity t WHERE t.periodeType = :periodeType AND t.dateDebut = (SELECT MAX(t2.dateDebut) FROM TeamPerformanceMetricsEntity t2 WHERE t2.teamId = t.teamId AND t2.periodeType = :periodeType)")
    List<TeamPerformanceMetricsEntity> findLatestByPeriodeType(@Param("periodeType") String periodeType);
    
    void deleteByTeamIdAndPeriodeTypeAndDateDebutBefore(UUID teamId, String periodeType, LocalDate cutoffDate);
    
    long countByTeamId(UUID teamId);
    
    @Query("SELECT AVG(t.slaComplianceRate) FROM TeamPerformanceMetricsEntity t WHERE t.periodeType = :periodeType AND t.dateDebut BETWEEN :startDate AND :endDate")
    Double getAverageSlaComplianceRate(@Param("periodeType") String periodeType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(t.averageResolutionTimeMinutes) FROM TeamPerformanceMetricsEntity t WHERE t.periodeType = :periodeType AND t.dateDebut BETWEEN :startDate AND :endDate")
    Double getAverageResolutionTime(@Param("periodeType") String periodeType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(t.averageSatisfactionScore) FROM TeamPerformanceMetricsEntity t WHERE t.periodeType = :periodeType AND t.dateDebut BETWEEN :startDate AND :endDate")
    Double getAverageSatisfactionScore(@Param("periodeType") String periodeType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM TeamPerformanceMetricsEntity t WHERE t.periodeType = :periodeType ORDER BY t.slaComplianceRate DESC")
    List<TeamPerformanceMetricsEntity> findTopPerformingTeams(@Param("periodeType") String periodeType, Pageable pageable);
}
