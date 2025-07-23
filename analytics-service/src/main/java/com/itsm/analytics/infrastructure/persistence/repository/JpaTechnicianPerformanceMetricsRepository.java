package com.itsm.analytics.infrastructure.persistence.repository;

import com.itsm.analytics.domain.model.TechnicianPerformanceMetrics;
import com.itsm.analytics.infrastructure.persistence.entity.TechnicianPerformanceMetricsEntity;
import com.itsm.analytics.infrastructure.persistence.mapper.TechnicianPerformanceMetricsMapper;
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
 * JPA Repository for Technician Performance Metrics
 * Provides data access layer for technician performance analytics
 */
@Repository
@RequiredArgsConstructor
public class JpaTechnicianPerformanceMetricsRepository {
    
    private final TechnicianPerformanceMetricsJpaRepository jpaRepository;
    private final TechnicianPerformanceMetricsMapper mapper;
    
    public TechnicianPerformanceMetrics save(TechnicianPerformanceMetrics technicianMetrics) {
        TechnicianPerformanceMetricsEntity entity = mapper.toEntity(technicianMetrics);
        TechnicianPerformanceMetricsEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    public Optional<TechnicianPerformanceMetrics> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    public List<TechnicianPerformanceMetrics> findByTechnicianId(UUID technicianId) {
        return jpaRepository.findByTechnicianIdOrderByDateDebutDesc(technicianId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TechnicianPerformanceMetrics> findByTeamId(UUID teamId) {
        return jpaRepository.findByTeamIdOrderByDateDebutDesc(teamId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TechnicianPerformanceMetrics> findByTechnicianIdAndPeriodeType(UUID technicianId, 
                                                                               TechnicianPerformanceMetrics.PeriodeType periodeType) {
        return jpaRepository.findByTechnicianIdAndPeriodeTypeOrderByDateDebutDesc(technicianId, periodeType.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TechnicianPerformanceMetrics> findByTeamIdAndPeriodeType(UUID teamId, 
                                                                         TechnicianPerformanceMetrics.PeriodeType periodeType) {
        return jpaRepository.findByTeamIdAndPeriodeTypeOrderByPerformanceScoreDesc(teamId, periodeType.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Optional<TechnicianPerformanceMetrics> findByTechnicianIdAndPeriodeTypeAndDateDebut(UUID technicianId,
                                                                                               TechnicianPerformanceMetrics.PeriodeType periodeType,
                                                                                               LocalDate dateDebut) {
        return jpaRepository.findByTechnicianIdAndPeriodeTypeAndDateDebut(technicianId, periodeType.name(), dateDebut)
                .map(mapper::toDomain);
    }
    
    public List<TechnicianPerformanceMetrics> findByPeriodeTypeAndDateDebutBetween(TechnicianPerformanceMetrics.PeriodeType periodeType,
                                                                                   LocalDate startDate,
                                                                                   LocalDate endDate) {
        return jpaRepository.findByPeriodeTypeAndDateDebutBetweenOrderByDateDebutDesc(periodeType.name(), startDate, endDate)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TechnicianPerformanceMetrics> findTopPerformers(UUID teamId, 
                                                               TechnicianPerformanceMetrics.PeriodeType periodeType, 
                                                               int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return jpaRepository.findTopPerformers(teamId, periodeType.name(), pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<TechnicianPerformanceMetrics> findLatestByTeamId(UUID teamId, TechnicianPerformanceMetrics.PeriodeType periodeType) {
        return jpaRepository.findLatestByTeamId(teamId, periodeType.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Page<TechnicianPerformanceMetrics> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
    
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    public void deleteByTechnicianIdAndPeriodeTypeAndDateDebutBefore(UUID technicianId,
                                                                     TechnicianPerformanceMetrics.PeriodeType periodeType,
                                                                     LocalDate cutoffDate) {
        jpaRepository.deleteByTechnicianIdAndPeriodeTypeAndDateDebutBefore(technicianId, periodeType.name(), cutoffDate);
    }
    
    public long countByTechnicianId(UUID technicianId) {
        return jpaRepository.countByTechnicianId(technicianId);
    }
    
    public long countByTeamId(UUID teamId) {
        return jpaRepository.countByTeamId(teamId);
    }
}

/**
 * Spring Data JPA Repository interface
 */
interface TechnicianPerformanceMetricsJpaRepository extends JpaRepository<TechnicianPerformanceMetricsEntity, UUID> {
    
    List<TechnicianPerformanceMetricsEntity> findByTechnicianIdOrderByDateDebutDesc(UUID technicianId);
    
    List<TechnicianPerformanceMetricsEntity> findByTeamIdOrderByDateDebutDesc(UUID teamId);
    
    List<TechnicianPerformanceMetricsEntity> findByTechnicianIdAndPeriodeTypeOrderByDateDebutDesc(UUID technicianId, String periodeType);
    
    List<TechnicianPerformanceMetricsEntity> findByTeamIdAndPeriodeTypeOrderByPerformanceScoreDesc(UUID teamId, String periodeType);
    
    Optional<TechnicianPerformanceMetricsEntity> findByTechnicianIdAndPeriodeTypeAndDateDebut(UUID technicianId, String periodeType, LocalDate dateDebut);
    
    List<TechnicianPerformanceMetricsEntity> findByPeriodeTypeAndDateDebutBetweenOrderByDateDebutDesc(String periodeType, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM TechnicianPerformanceMetricsEntity t WHERE t.teamId = :teamId AND t.periodeType = :periodeType ORDER BY t.performanceScore DESC")
    List<TechnicianPerformanceMetricsEntity> findTopPerformers(@Param("teamId") UUID teamId, @Param("periodeType") String periodeType, Pageable pageable);
    
    @Query("SELECT t FROM TechnicianPerformanceMetricsEntity t WHERE t.teamId = :teamId AND t.periodeType = :periodeType AND t.dateDebut = (SELECT MAX(t2.dateDebut) FROM TechnicianPerformanceMetricsEntity t2 WHERE t2.technicianId = t.technicianId AND t2.periodeType = :periodeType)")
    List<TechnicianPerformanceMetricsEntity> findLatestByTeamId(@Param("teamId") UUID teamId, @Param("periodeType") String periodeType);
    
    void deleteByTechnicianIdAndPeriodeTypeAndDateDebutBefore(UUID technicianId, String periodeType, LocalDate cutoffDate);
    
    long countByTechnicianId(UUID technicianId);
    
    long countByTeamId(UUID teamId);
    
    @Query("SELECT AVG(t.slaComplianceRate) FROM TechnicianPerformanceMetricsEntity t WHERE t.teamId = :teamId AND t.periodeType = :periodeType AND t.dateDebut BETWEEN :startDate AND :endDate")
    Double getAverageSlaComplianceRateByTeam(@Param("teamId") UUID teamId, @Param("periodeType") String periodeType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(t.averageResolutionTimeMinutes) FROM TechnicianPerformanceMetricsEntity t WHERE t.teamId = :teamId AND t.periodeType = :periodeType AND t.dateDebut BETWEEN :startDate AND :endDate")
    Double getAverageResolutionTimeByTeam(@Param("teamId") UUID teamId, @Param("periodeType") String periodeType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(t.averageSatisfactionScore) FROM TechnicianPerformanceMetricsEntity t WHERE t.teamId = :teamId AND t.periodeType = :periodeType AND t.dateDebut BETWEEN :startDate AND :endDate")
    Double getAverageSatisfactionScoreByTeam(@Param("teamId") UUID teamId, @Param("periodeType") String periodeType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM TechnicianPerformanceMetricsEntity t WHERE t.periodeType = :periodeType ORDER BY t.performanceScore DESC")
    List<TechnicianPerformanceMetricsEntity> findGlobalTopPerformers(@Param("periodeType") String periodeType, Pageable pageable);
}
