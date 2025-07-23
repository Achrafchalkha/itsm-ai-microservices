package com.itsm.assignment.presentation.controller;

import com.itsm.assignment.application.service.AssignmentService;
import com.itsm.assignment.domain.model.Assignment;
import com.itsm.assignment.infrastructure.persistence.entity.AssignmentEntity;
import com.itsm.assignment.infrastructure.persistence.repository.JpaAssignmentRepository;
import com.itsm.assignment.presentation.dto.AssignmentDTO;
import com.itsm.assignment.presentation.dto.ManualAssignmentRequest;
import com.itsm.assignment.presentation.dto.ReassignmentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for assignment operations
 * Provides endpoints for manual assignment, reassignment, and assignment queries
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    private final JpaAssignmentRepository assignmentRepository;
    
    /**
     * Manually assign a ticket to a technician
     * Only managers and admins can perform manual assignments
     */
    @PostMapping("/manual")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentDTO> manualAssignment(@Valid @RequestBody ManualAssignmentRequest request) {
        log.info("Manual assignment request for ticket {} to technician {}", 
                request.getTicketId(), request.getTechnicianId());
        
        try {
            Assignment assignment = assignmentService.manualAssignment(
                    request.getTicketId(),
                    request.getTechnicianId(),
                    request.getAssignedBy(),
                    request.getReason()
            );
            
            AssignmentDTO response = convertToDTO(assignment);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid manual assignment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Assignment conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error processing manual assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Reassign a ticket to a different technician
     * Only managers and admins can perform reassignments
     */
    @PutMapping("/{assignmentId}/reassign")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentDTO> reassignTicket(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody ReassignmentRequest request) {
        
        log.info("Reassignment request for assignment {} to technician {}", 
                assignmentId, request.getNewTechnicianId());
        
        try {
            // Get current assignment to find ticket ID
            AssignmentEntity currentAssignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
            
            Assignment newAssignment = assignmentService.reassignTicket(
                    currentAssignment.getTicketId(),
                    request.getNewTechnicianId(),
                    request.getReassignedBy(),
                    request.getReason()
            );
            
            AssignmentDTO response = convertToDTO(newAssignment);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid reassignment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing reassignment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get assignment by ticket ID
     */
    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentDTO> getAssignmentByTicket(@PathVariable UUID ticketId) {
        log.debug("Getting assignment for ticket: {}", ticketId);
        
        return assignmentRepository.findByTicketId(ticketId)
                .map(this::convertEntityToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get assignments for a technician
     */
    @GetMapping("/technician/{technicianId}")
    @PreAuthorize("hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AssignmentDTO>> getTechnicianAssignments(
            @PathVariable UUID technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting assignments for technician: {}", technicianId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<AssignmentEntity> assignments = assignmentRepository
                .findAll(pageable); // Would need custom query for technician filter
        
        List<AssignmentDTO> response = assignments.getContent().stream()
                .filter(a -> a.getTechnicianId().equals(technicianId))
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get assignments for a team
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AssignmentDTO>> getTeamAssignments(
            @PathVariable UUID teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting assignments for team: {}", teamId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        List<AssignmentEntity> assignments = assignmentRepository.findByTeamIdOrderByAssignedAtDesc(teamId);
        
        // Apply pagination manually (in real implementation, use proper repository method)
        int start = page * size;
        int end = Math.min(start + size, assignments.size());
        
        List<AssignmentDTO> response = assignments.subList(start, end).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get assignment statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AssignmentStatsDTO> getAssignmentStats() {
        log.debug("Getting assignment statistics");
        
        try {
            long totalAssignments = assignmentRepository.count();
            List<Object[]> strategyStats = assignmentRepository.getAssignmentStatsByStrategy();
            List<Object[]> workloadStats = assignmentRepository.getTechnicianWorkloadStats();
            
            AssignmentStatsDTO stats = AssignmentStatsDTO.builder()
                    .totalAssignments(totalAssignments)
                    .activeAssignments(assignmentRepository.countTeamAssignmentsByStatus(
                            null, com.itsm.assignment.domain.model.AssignmentStatus.ACTIVE))
                    .averageConfidenceScore(calculateAverageConfidence())
                    .strategyDistribution(convertStrategyStats(strategyStats))
                    .technicianWorkload(convertWorkloadStats(workloadStats))
                    .build();
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting assignment statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get assignments needing attention (low confidence or old)
     */
    @GetMapping("/attention")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsNeedingAttention() {
        log.debug("Getting assignments needing attention");
        
        LocalDateTime oldDate = LocalDateTime.now().minusDays(7); // Assignments older than 7 days
        BigDecimal confidenceThreshold = BigDecimal.valueOf(0.6); // Low confidence threshold

        List<AssignmentEntity> assignments = assignmentRepository
                .findAssignmentsNeedingAttention(confidenceThreshold, oldDate);
        
        List<AssignmentDTO> response = assignments.stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Convert Assignment domain model to DTO
     */
    private AssignmentDTO convertToDTO(Assignment assignment) {
        return AssignmentDTO.builder()
                .id(assignment.getId())
                .ticketId(assignment.getTicketId())
                .technicianId(assignment.getTechnicianId())
                .teamId(assignment.getTeamId())
                .strategy(assignment.getStrategy().name())
                .confidenceScore(assignment.getConfidenceScore())
                .assignmentReason(assignment.getAssignmentReason())
                .assignedAt(assignment.getAssignedAt())
                .status(assignment.getStatus().name())
                .reassignedBy(assignment.getReassignedBy())
                .reassignmentReason(assignment.getReassignmentReason())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert AssignmentEntity to DTO
     */
    private AssignmentDTO convertEntityToDTO(AssignmentEntity entity) {
        return AssignmentDTO.builder()
                .id(entity.getId())
                .ticketId(entity.getTicketId())
                .technicianId(entity.getTechnicianId())
                .teamId(entity.getTeamId())
                .strategy(entity.getStrategy().name())
                .confidenceScore(entity.getConfidenceScore())
                .assignmentReason(entity.getAssignmentReason())
                .assignedAt(entity.getAssignedAt())
                .status(entity.getStatus().name())
                .reassignedBy(entity.getReassignedBy())
                .reassignmentReason(entity.getReassignmentReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * Calculate average confidence score
     */
    private double calculateAverageConfidence() {
        return assignmentRepository.findAll().stream()
                .filter(a -> a.getConfidenceScore() != null)
                .mapToDouble(a -> a.getConfidenceScore().doubleValue())
                .average()
                .orElse(0.0);
    }
    
    /**
     * Convert strategy statistics
     */
    private java.util.Map<String, Object> convertStrategyStats(List<Object[]> strategyStats) {
        return strategyStats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> row[0].toString(),
                        row -> java.util.Map.of(
                                "count", row[1],
                                "avgConfidence", row[2]
                        )
                ));
    }
    
    /**
     * Convert workload statistics
     */
    private java.util.Map<UUID, Long> convertWorkloadStats(List<Object[]> workloadStats) {
        return workloadStats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }
    
    /**
     * DTO for assignment statistics
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignmentStatsDTO {
        private long totalAssignments;
        private long activeAssignments;
        private double averageConfidenceScore;
        private java.util.Map<String, Object> strategyDistribution;
        private java.util.Map<UUID, Long> technicianWorkload;
    }
}
