package com.itsm.assignment.infrastructure.persistence.repository;

import com.itsm.assignment.domain.model.AssignmentStatus;
import com.itsm.assignment.domain.model.AssignmentStrategy;
import com.itsm.assignment.infrastructure.persistence.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Assignment entities
 */
@Repository
public interface JpaAssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {
    
    /**
     * Find assignment by ticket ID
     */
    Optional<AssignmentEntity> findByTicketId(UUID ticketId);
    
    /**
     * Find active assignment by ticket ID
     */
    Optional<AssignmentEntity> findByTicketIdAndStatus(UUID ticketId, AssignmentStatus status);
    
    /**
     * Find all assignments for a technician
     */
    List<AssignmentEntity> findByTechnicianIdOrderByAssignedAtDesc(UUID technicianId);
    
    /**
     * Find active assignments for a technician
     */
    List<AssignmentEntity> findByTechnicianIdAndStatus(UUID technicianId, AssignmentStatus status);
    
    /**
     * Find all assignments for a team
     */
    List<AssignmentEntity> findByTeamIdOrderByAssignedAtDesc(UUID teamId);
    
    /**
     * Find assignments by strategy
     */
    List<AssignmentEntity> findByStrategy(AssignmentStrategy strategy);
    
    /**
     * Find assignments within date range
     */
    List<AssignmentEntity> findByAssignedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count active assignments for a technician
     */
    @Query("SELECT COUNT(a) FROM AssignmentEntity a WHERE a.technicianId = :technicianId AND a.status = 'ACTIVE'")
    long countActiveTechnicianAssignments(@Param("technicianId") UUID technicianId);
    
    /**
     * Count assignments by team and status
     */
    @Query("SELECT COUNT(a) FROM AssignmentEntity a WHERE a.teamId = :teamId AND a.status = :status")
    long countTeamAssignmentsByStatus(@Param("teamId") UUID teamId, @Param("status") AssignmentStatus status);
    
    /**
     * Find assignments with high confidence scores
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE a.confidenceScore >= :threshold ORDER BY a.confidenceScore DESC")
    List<AssignmentEntity> findHighConfidenceAssignments(@Param("threshold") BigDecimal threshold);
    
    /**
     * Find assignments that were reassigned
     */
    List<AssignmentEntity> findByStatusAndReassignedByIsNotNull(AssignmentStatus status);
    
    /**
     * Get assignment statistics by strategy
     */
    @Query("SELECT a.strategy, COUNT(a), AVG(a.confidenceScore) FROM AssignmentEntity a GROUP BY a.strategy")
    List<Object[]> getAssignmentStatsByStrategy();
    
    /**
     * Get technician workload statistics
     */
    @Query("SELECT a.technicianId, COUNT(a) FROM AssignmentEntity a WHERE a.status = 'ACTIVE' GROUP BY a.technicianId")
    List<Object[]> getTechnicianWorkloadStats();
    
    /**
     * Find assignments needing attention (low confidence or old)
     */
    @Query("SELECT a FROM AssignmentEntity a WHERE " +
           "(a.confidenceScore < :confidenceThreshold OR a.assignedAt < :oldDate) " +
           "AND a.status = 'ACTIVE' ORDER BY a.confidenceScore ASC, a.assignedAt ASC")
    List<AssignmentEntity> findAssignmentsNeedingAttention(
            @Param("confidenceThreshold") BigDecimal confidenceThreshold,
            @Param("oldDate") LocalDateTime oldDate);
}
