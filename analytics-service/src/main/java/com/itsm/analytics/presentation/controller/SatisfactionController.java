package com.itsm.analytics.presentation.controller;

import com.itsm.analytics.application.service.SatisfactionService;
import com.itsm.analytics.domain.model.SatisfactionScore;
import com.itsm.analytics.presentation.dto.CreateSatisfactionScoreRequest;
import com.itsm.analytics.presentation.dto.SatisfactionScoreDTO;
import com.itsm.analytics.presentation.dto.UpdateSatisfactionScoreRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for satisfaction score management
 * Allows users to provide feedback on resolved tickets
 */
@RestController
@RequestMapping("/api/analytics/satisfaction")
@RequiredArgsConstructor
@Slf4j
public class SatisfactionController {
    
    private final SatisfactionService satisfactionService;
    
    /**
     * Create satisfaction score for a resolved ticket
     * Only the ticket requester can provide satisfaction feedback
     */
    @PostMapping
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SatisfactionScoreDTO> createSatisfactionScore(
            @Valid @RequestBody CreateSatisfactionScoreRequest request,
            Authentication authentication) {
        
        log.info("Creating satisfaction score for ticket: {}", request.getTicketId());
        
        try {
            UUID utilisateurId = extractUserIdFromAuth(authentication);
            
            SatisfactionScore satisfactionScore = satisfactionService.createSatisfactionScore(
                    request.getTicketId(),
                    utilisateurId,
                    request.getTechnicienId(),
                    request.getTeamId(),
                    request.getScore(),
                    request.getCommentaire()
            );
            
            SatisfactionScoreDTO response = convertToDTO(satisfactionScore);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid satisfaction score request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating satisfaction score: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update satisfaction score with detailed ratings
     */
    @PutMapping("/{satisfactionId}/details")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SatisfactionScoreDTO> updateSatisfactionScoreDetails(
            @PathVariable UUID satisfactionId,
            @Valid @RequestBody UpdateSatisfactionScoreRequest request) {
        
        log.info("Updating satisfaction score details: {}", satisfactionId);
        
        try {
            SatisfactionScore satisfactionScore = satisfactionService.updateSatisfactionScoreDetails(
                    satisfactionId,
                    request.getTempsResolutionSatisfaisant(),
                    request.getQualiteCommunicationScore(),
                    request.getCompetenceTechniqueScore()
            );
            
            SatisfactionScoreDTO response = convertToDTO(satisfactionScore);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid satisfaction score update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating satisfaction score details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction scores for a ticket
     */
    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or hasRole('TECHNICIEN')")
    public ResponseEntity<List<SatisfactionScoreDTO>> getSatisfactionScoresByTicket(
            @PathVariable UUID ticketId) {
        
        log.debug("Getting satisfaction scores for ticket: {}", ticketId);
        
        try {
            List<SatisfactionScore> scores = satisfactionService.getSatisfactionScoresByTicket(ticketId);
            
            List<SatisfactionScoreDTO> response = scores.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction scores for ticket {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction scores for a technician
     */
    @GetMapping("/technician/{technicienId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SatisfactionScoreDTO>> getSatisfactionScoresByTechnician(
            @PathVariable UUID technicienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting satisfaction scores for technician: {} from {} to {}", technicienId, startDate, endDate);
        
        try {
            List<SatisfactionScore> scores = satisfactionService.getSatisfactionScoresByTechnician(
                    technicienId, startDate, endDate);
            
            List<SatisfactionScoreDTO> response = scores.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction scores for technician {}: {}", technicienId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction scores for a team
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SatisfactionScoreDTO>> getSatisfactionScoresByTeam(
            @PathVariable UUID teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting satisfaction scores for team: {} from {} to {}", teamId, startDate, endDate);
        
        try {
            List<SatisfactionScore> scores = satisfactionService.getSatisfactionScoresByTeam(
                    teamId, startDate, endDate);
            
            List<SatisfactionScoreDTO> response = scores.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction scores for team {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get average satisfaction score for a technician
     */
    @GetMapping("/technician/{technicienId}/average")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAverageSatisfactionForTechnician(
            @PathVariable UUID technicienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting average satisfaction for technician: {} from {} to {}", technicienId, startDate, endDate);
        
        try {
            BigDecimal averageScore = satisfactionService.calculateAverageSatisfactionForTechnician(
                    technicienId, startDate, endDate);
            
            Map<String, Object> response = Map.of(
                    "technicienId", technicienId,
                    "averageScore", averageScore,
                    "period", Map.of("start", startDate, "end", endDate)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting average satisfaction for technician {}: {}", technicienId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get average satisfaction score for a team
     */
    @GetMapping("/team/{teamId}/average")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAverageSatisfactionForTeam(
            @PathVariable UUID teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting average satisfaction for team: {} from {} to {}", teamId, startDate, endDate);
        
        try {
            BigDecimal averageScore = satisfactionService.calculateAverageSatisfactionForTeam(
                    teamId, startDate, endDate);
            
            Map<String, Object> response = Map.of(
                    "teamId", teamId,
                    "averageScore", averageScore,
                    "period", Map.of("start", startDate, "end", endDate)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting average satisfaction for team {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction distribution for a period
     */
    @GetMapping("/distribution")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSatisfactionDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting satisfaction distribution from {} to {}", startDate, endDate);
        
        try {
            Map<Integer, Long> distribution = satisfactionService.getSatisfactionDistribution(startDate, endDate);
            
            Map<String, Object> response = Map.of(
                    "distribution", distribution,
                    "period", Map.of("start", startDate, "end", endDate)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction distribution: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction statistics for a technician
     */
    @GetMapping("/technician/{technicienId}/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SatisfactionService.SatisfactionStatistics> getSatisfactionStatisticsForTechnician(
            @PathVariable UUID technicienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting satisfaction statistics for technician: {} from {} to {}", technicienId, startDate, endDate);
        
        try {
            SatisfactionService.SatisfactionStatistics statistics = 
                    satisfactionService.getSatisfactionStatisticsForTechnician(technicienId, startDate, endDate);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction statistics for technician {}: {}", technicienId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get satisfaction statistics for a team
     */
    @GetMapping("/team/{teamId}/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<SatisfactionService.SatisfactionStatistics> getSatisfactionStatisticsForTeam(
            @PathVariable UUID teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting satisfaction statistics for team: {} from {} to {}", teamId, startDate, endDate);
        
        try {
            SatisfactionService.SatisfactionStatistics statistics = 
                    satisfactionService.getSatisfactionStatisticsForTeam(teamId, startDate, endDate);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error getting satisfaction statistics for team {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Private helper methods
    
    /**
     * Convert domain model to DTO
     */
    private SatisfactionScoreDTO convertToDTO(SatisfactionScore satisfactionScore) {
        return SatisfactionScoreDTO.builder()
                .id(satisfactionScore.getId())
                .ticketId(satisfactionScore.getTicketId())
                .utilisateurId(satisfactionScore.getUtilisateurId())
                .technicienId(satisfactionScore.getTechnicienId())
                .teamId(satisfactionScore.getTeamId())
                .score(satisfactionScore.getScore())
                .commentaire(satisfactionScore.getCommentaire())
                .tempsResolutionSatisfaisant(satisfactionScore.getTempsResolutionSatisfaisant())
                .qualiteCommunicationScore(satisfactionScore.getQualiteCommunicationScore())
                .competenceTechniqueScore(satisfactionScore.getCompetenceTechniqueScore())
                .satisfactionLevel(satisfactionScore.getNiveauSatisfaction().name())
                .createdAt(satisfactionScore.getCreatedAt())
                .build();
    }
    
    /**
     * Extract user ID from authentication
     */
    private UUID extractUserIdFromAuth(Authentication authentication) {
        // This would extract the user ID from JWT token
        // For now, return a placeholder
        return UUID.randomUUID();
    }
}
