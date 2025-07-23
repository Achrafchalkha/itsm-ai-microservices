package com.itsm.analytics.infrastructure.persistence.repository;

import com.itsm.analytics.domain.model.DailyKPI;
import com.itsm.analytics.infrastructure.persistence.entity.DailyKPIEntity;
import com.itsm.analytics.infrastructure.persistence.mapper.DailyKPIMapper;
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
 * JPA Repository for Daily KPI
 * Provides data access layer for daily KPI aggregations
 */
@Repository
@RequiredArgsConstructor
public class JpaDailyKPIRepository {
    
    private final DailyKPIJpaRepository jpaRepository;
    private final DailyKPIMapper mapper;
    
    public DailyKPI save(DailyKPI dailyKPI) {
        DailyKPIEntity entity = mapper.toEntity(dailyKPI);
        DailyKPIEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    public Optional<DailyKPI> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    public Optional<DailyKPI> findByDateKpi(LocalDate date) {
        return jpaRepository.findByDateKpi(date)
                .map(mapper::toDomain);
    }
    
    public List<DailyKPI> findByDateKpiBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByDateKpiBetweenOrderByDateKpiAsc(startDate, endDate)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<DailyKPI> findByDateKpiAfter(LocalDate date) {
        return jpaRepository.findByDateKpiAfterOrderByDateKpiAsc(date)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Page<DailyKPI> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
    
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    public void deleteByDateKpiBefore(LocalDate date) {
        jpaRepository.deleteByDateKpiBefore(date);
    }
    
    public long countByDateKpiBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.countByDateKpiBetween(startDate, endDate);
    }
}

/**
 * Spring Data JPA Repository interface
 */
interface DailyKPIJpaRepository extends JpaRepository<DailyKPIEntity, UUID> {
    
    Optional<DailyKPIEntity> findByDateKpi(LocalDate dateKpi);
    
    List<DailyKPIEntity> findByDateKpiBetweenOrderByDateKpiAsc(LocalDate startDate, LocalDate endDate);
    
    List<DailyKPIEntity> findByDateKpiAfterOrderByDateKpiAsc(LocalDate date);
    
    void deleteByDateKpiBefore(LocalDate date);
    
    long countByDateKpiBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(d.totalTicketsCreated) FROM DailyKPIEntity d WHERE d.dateKpi BETWEEN :startDate AND :endDate")
    Long sumTotalTicketsCreatedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(d.totalTicketsResolved) FROM DailyKPIEntity d WHERE d.dateKpi BETWEEN :startDate AND :endDate")
    Long sumTotalTicketsResolvedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(d.ticketsWithinSla) FROM DailyKPIEntity d WHERE d.dateKpi BETWEEN :startDate AND :endDate")
    Long sumTicketsWithinSlaBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(d.ticketsBreachedSla) FROM DailyKPIEntity d WHERE d.dateKpi BETWEEN :startDate AND :endDate")
    Long sumTicketsBreachedSlaBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(d.averageResolutionTimeMinutes) FROM DailyKPIEntity d WHERE d.dateKpi BETWEEN :startDate AND :endDate AND d.averageResolutionTimeMinutes IS NOT NULL")
    Double avgResolutionTimeBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(d.averageSatisfactionScore) FROM DailyKPIEntity d WHERE d.dateKpi BETWEEN :startDate AND :endDate AND d.averageSatisfactionScore IS NOT NULL")
    Double avgSatisfactionScoreBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
