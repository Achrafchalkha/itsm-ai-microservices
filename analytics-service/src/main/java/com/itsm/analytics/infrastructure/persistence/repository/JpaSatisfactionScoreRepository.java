package com.itsm.analytics.infrastructure.persistence.repository;

import com.itsm.analytics.domain.model.SatisfactionScore;
import com.itsm.analytics.infrastructure.persistence.entity.SatisfactionScoreEntity;
import com.itsm.analytics.infrastructure.persistence.mapper.SatisfactionScoreMapper;
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
 * JPA Repository for Satisfaction Score
 * Provides data access layer for satisfaction scores and feedback
 */
@Repository
@RequiredArgsConstructor
public class JpaSatisfactionScoreRepository {
    
    private final SatisfactionScoreJpaRepository jpaRepository;
    private final SatisfactionScoreMapper mapper;
    
    public SatisfactionScore save(SatisfactionScore satisfactionScore) {
        SatisfactionScoreEntity entity = mapper.toEntity(satisfactionScore);
        SatisfactionScoreEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    public Optional<SatisfactionScore> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    public List<SatisfactionScore> findByTicketId(UUID ticketId) {
        return jpaRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SatisfactionScore> findByTechnicienId(UUID technicienId) {
        return jpaRepository.findByTechnicienIdOrderByCreatedAtDesc(technicienId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SatisfactionScore> findByTeamId(UUID teamId) {
        return jpaRepository.findByTeamIdOrderByCreatedAtDesc(teamId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SatisfactionScore> findByTechnicienIdAndCreatedAtBetween(UUID technicienId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByTechnicienIdAndCreatedAtBetweenOrderByCreatedAtDesc(technicienId, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SatisfactionScore> findByTeamIdAndCreatedAtBetween(UUID teamId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByTeamIdAndCreatedAtBetweenOrderByCreatedAtDesc(teamId, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SatisfactionScore> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public List<SatisfactionScore> findByScore(Integer score) {
        return jpaRepository.findByScoreOrderByCreatedAtDesc(score)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    public Page<SatisfactionScore> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
    
    public boolean existsByTicketId(UUID ticketId) {
        return jpaRepository.existsByTicketId(ticketId);
    }
    
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    public long countByTechnicienId(UUID technicienId) {
        return jpaRepository.countByTechnicienId(technicienId);
    }
    
    public long countByTeamId(UUID teamId) {
        return jpaRepository.countByTeamId(teamId);
    }
    
    public long countByScore(Integer score) {
        return jpaRepository.countByScore(score);
    }
}

/**
 * Spring Data JPA Repository interface
 */
interface SatisfactionScoreJpaRepository extends JpaRepository<SatisfactionScoreEntity, UUID> {
    
    List<SatisfactionScoreEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
    
    List<SatisfactionScoreEntity> findByTechnicienIdOrderByCreatedAtDesc(UUID technicienId);
    
    List<SatisfactionScoreEntity> findByTeamIdOrderByCreatedAtDesc(UUID teamId);
    
    List<SatisfactionScoreEntity> findByTechnicienIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID technicienId, LocalDateTime start, LocalDateTime end);
    
    List<SatisfactionScoreEntity> findByTeamIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID teamId, LocalDateTime start, LocalDateTime end);
    
    List<SatisfactionScoreEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    List<SatisfactionScoreEntity> findByScoreOrderByCreatedAtDesc(Integer score);
    
    boolean existsByTicketId(UUID ticketId);
    
    long countByTechnicienId(UUID technicienId);
    
    long countByTeamId(UUID teamId);
    
    long countByScore(Integer score);
    
    @Query("SELECT AVG(s.score) FROM SatisfactionScoreEntity s WHERE s.technicienId = :technicienId")
    Double getAverageScoreByTechnicienId(@Param("technicienId") UUID technicienId);
    
    @Query("SELECT AVG(s.score) FROM SatisfactionScoreEntity s WHERE s.teamId = :teamId")
    Double getAverageScoreByTeamId(@Param("teamId") UUID teamId);
    
    @Query("SELECT AVG(s.score) FROM SatisfactionScoreEntity s WHERE s.createdAt BETWEEN :start AND :end")
    Double getAverageScoreBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT AVG(s.score) FROM SatisfactionScoreEntity s WHERE s.technicienId = :technicienId AND s.createdAt BETWEEN :start AND :end")
    Double getAverageScoreByTechnicienIdBetween(@Param("technicienId") UUID technicienId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT AVG(s.score) FROM SatisfactionScoreEntity s WHERE s.teamId = :teamId AND s.createdAt BETWEEN :start AND :end")
    Double getAverageScoreByTeamIdBetween(@Param("teamId") UUID teamId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(s) FROM SatisfactionScoreEntity s WHERE s.score >= 4")
    long countPositiveScores();
    
    @Query("SELECT COUNT(s) FROM SatisfactionScoreEntity s WHERE s.score = 3")
    long countNeutralScores();
    
    @Query("SELECT COUNT(s) FROM SatisfactionScoreEntity s WHERE s.score <= 2")
    long countNegativeScores();
    
    @Query("SELECT COUNT(s) FROM SatisfactionScoreEntity s WHERE s.technicienId = :technicienId AND s.score >= 4")
    long countPositiveScoresByTechnicienId(@Param("technicienId") UUID technicienId);
    
    @Query("SELECT COUNT(s) FROM SatisfactionScoreEntity s WHERE s.teamId = :teamId AND s.score >= 4")
    long countPositiveScoresByTeamId(@Param("teamId") UUID teamId);
    
    @Query("SELECT s.score, COUNT(s) FROM SatisfactionScoreEntity s WHERE s.createdAt BETWEEN :start AND :end GROUP BY s.score ORDER BY s.score")
    List<Object[]> getScoreDistributionBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s.technicienId, AVG(s.score) as avgScore, COUNT(s) as totalResponses FROM SatisfactionScoreEntity s WHERE s.createdAt BETWEEN :start AND :end GROUP BY s.technicienId ORDER BY avgScore DESC")
    List<Object[]> getTechnicianSatisfactionRankingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT s.teamId, AVG(s.score) as avgScore, COUNT(s) as totalResponses FROM SatisfactionScoreEntity s WHERE s.createdAt BETWEEN :start AND :end GROUP BY s.teamId ORDER BY avgScore DESC")
    List<Object[]> getTeamSatisfactionRankingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT DATE(s.createdAt) as date, AVG(s.score) as avgScore, COUNT(s) as totalResponses FROM SatisfactionScoreEntity s WHERE s.createdAt BETWEEN :start AND :end GROUP BY DATE(s.createdAt) ORDER BY date")
    List<Object[]> getDailySatisfactionTrendBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
