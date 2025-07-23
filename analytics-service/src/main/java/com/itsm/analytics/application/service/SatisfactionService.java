package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.SatisfactionScore;
import com.itsm.analytics.infrastructure.persistence.repository.JpaSatisfactionScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing satisfaction scores and feedback
 * Handles satisfaction tracking and aggregation for analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SatisfactionService {
    
    private final JpaSatisfactionScoreRepository satisfactionScoreRepository;
    
    /**
     * Create satisfaction score for a resolved ticket
     */
    public SatisfactionScore createSatisfactionScore(UUID ticketId, UUID utilisateurId, 
                                                    UUID technicienId, UUID teamId, 
                                                    int score, String commentaire) {
        
        log.info("Creating satisfaction score for ticket: {} - Score: {}", ticketId, score);
        
        try {
            // Check if satisfaction score already exists for this ticket
            if (satisfactionScoreRepository.existsByTicketId(ticketId)) {
                throw new IllegalArgumentException("Satisfaction score already exists for ticket: " + ticketId);
            }
            
            // Validate score
            if (score < 1 || score > 5) {
                throw new IllegalArgumentException("Score must be between 1 and 5");
            }
            
            SatisfactionScore satisfactionScore = SatisfactionScore.creerScore(
                    ticketId, utilisateurId, technicienId, teamId, score, commentaire);
            
            SatisfactionScore saved = satisfactionScoreRepository.save(satisfactionScore);
            
            log.info("Created satisfaction score: {} for ticket: {}", saved.getId(), ticketId);
            return saved;
            
        } catch (Exception e) {
            log.error("Error creating satisfaction score for ticket {}: {}", ticketId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Update satisfaction score with detailed ratings
     */
    public SatisfactionScore updateSatisfactionScoreDetails(UUID satisfactionId, 
                                                           boolean tempsResolutionSatisfaisant,
                                                           int qualiteCommunication, 
                                                           int competenceTechnique) {
        
        log.info("Updating satisfaction score details: {}", satisfactionId);
        
        try {
            SatisfactionScore satisfactionScore = satisfactionScoreRepository.findById(satisfactionId)
                    .orElseThrow(() -> new IllegalArgumentException("Satisfaction score not found: " + satisfactionId));
            
            // Validate detailed scores
            if (qualiteCommunication < 1 || qualiteCommunication > 5) {
                throw new IllegalArgumentException("Quality communication score must be between 1 and 5");
            }
            if (competenceTechnique < 1 || competenceTechnique > 5) {
                throw new IllegalArgumentException("Technical competence score must be between 1 and 5");
            }
            
            satisfactionScore.definirScoresDetailles(tempsResolutionSatisfaisant, qualiteCommunication, competenceTechnique);
            
            SatisfactionScore updated = satisfactionScoreRepository.save(satisfactionScore);
            
            log.info("Updated satisfaction score details: {}", satisfactionId);
            return updated;
            
        } catch (Exception e) {
            log.error("Error updating satisfaction score details {}: {}", satisfactionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get satisfaction scores for a ticket
     */
    @Transactional(readOnly = true)
    public List<SatisfactionScore> getSatisfactionScoresByTicket(UUID ticketId) {
        return satisfactionScoreRepository.findByTicketId(ticketId);
    }
    
    /**
     * Get satisfaction scores for a technician
     */
    @Transactional(readOnly = true)
    public List<SatisfactionScore> getSatisfactionScoresByTechnician(UUID technicienId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return satisfactionScoreRepository.findByTechnicienIdAndCreatedAtBetween(technicienId, startDateTime, endDateTime);
    }
    
    /**
     * Get satisfaction scores for a team
     */
    @Transactional(readOnly = true)
    public List<SatisfactionScore> getSatisfactionScoresByTeam(UUID teamId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return satisfactionScoreRepository.findByTeamIdAndCreatedAtBetween(teamId, startDateTime, endDateTime);
    }
    
    /**
     * Calculate average satisfaction score for a technician
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAverageSatisfactionForTechnician(UUID technicienId, LocalDate startDate, LocalDate endDate) {
        List<SatisfactionScore> scores = getSatisfactionScoresByTechnician(technicienId, startDate, endDate);
        
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double average = scores.stream()
                .mapToInt(SatisfactionScore::getScore)
                .average()
                .orElse(0.0);
        
        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate average satisfaction score for a team
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAverageSatisfactionForTeam(UUID teamId, LocalDate startDate, LocalDate endDate) {
        List<SatisfactionScore> scores = getSatisfactionScoresByTeam(teamId, startDate, endDate);
        
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double average = scores.stream()
                .mapToInt(SatisfactionScore::getScore)
                .average()
                .orElse(0.0);
        
        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get satisfaction distribution for a period
     */
    @Transactional(readOnly = true)
    public Map<Integer, Long> getSatisfactionDistribution(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<SatisfactionScore> scores = satisfactionScoreRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        return scores.stream()
                .collect(Collectors.groupingBy(
                        SatisfactionScore::getScore,
                        Collectors.counting()
                ));
    }
    
    /**
     * Get satisfaction statistics for a technician
     */
    @Transactional(readOnly = true)
    public SatisfactionStatistics getSatisfactionStatisticsForTechnician(UUID technicienId, LocalDate startDate, LocalDate endDate) {
        List<SatisfactionScore> scores = getSatisfactionScoresByTechnician(technicienId, startDate, endDate);
        
        return calculateSatisfactionStatistics(scores);
    }
    
    /**
     * Get satisfaction statistics for a team
     */
    @Transactional(readOnly = true)
    public SatisfactionStatistics getSatisfactionStatisticsForTeam(UUID teamId, LocalDate startDate, LocalDate endDate) {
        List<SatisfactionScore> scores = getSatisfactionScoresByTeam(teamId, startDate, endDate);
        
        return calculateSatisfactionStatistics(scores);
    }
    
    /**
     * Aggregate daily satisfaction scores
     */
    public void aggregateDailySatisfactionScores(LocalDate date) {
        log.debug("Aggregating daily satisfaction scores for date: {}", date);
        
        try {
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(23, 59, 59);
            
            List<SatisfactionScore> dailyScores = satisfactionScoreRepository.findByCreatedAtBetween(startDateTime, endDateTime);
            
            if (!dailyScores.isEmpty()) {
                // Calculate daily aggregations
                double averageScore = dailyScores.stream()
                        .mapToInt(SatisfactionScore::getScore)
                        .average()
                        .orElse(0.0);
                
                int totalResponses = dailyScores.size();
                
                // This would update the daily KPI with satisfaction metrics
                log.debug("Daily satisfaction aggregation for {}: {} responses, average score: {}", 
                         date, totalResponses, averageScore);
            }
            
        } catch (Exception e) {
            log.error("Error aggregating daily satisfaction scores for {}: {}", date, e.getMessage(), e);
        }
    }
    
    /**
     * Update team satisfaction metrics
     */
    public void updateTeamSatisfactionMetrics(LocalDate date) {
        log.debug("Updating team satisfaction metrics for date: {}", date);
        
        try {
            // This would update team-specific satisfaction metrics
            // Implementation would depend on how team metrics are stored
            
        } catch (Exception e) {
            log.error("Error updating team satisfaction metrics for {}: {}", date, e.getMessage(), e);
        }
    }
    
    /**
     * Update technician satisfaction metrics
     */
    public void updateTechnicianSatisfactionMetrics(LocalDate date) {
        log.debug("Updating technician satisfaction metrics for date: {}", date);
        
        try {
            // This would update technician-specific satisfaction metrics
            // Implementation would depend on how technician metrics are stored
            
        } catch (Exception e) {
            log.error("Error updating technician satisfaction metrics for {}: {}", date, e.getMessage(), e);
        }
    }
    
    /**
     * Get top performing technicians by satisfaction
     */
    @Transactional(readOnly = true)
    public List<TechnicianSatisfactionRanking> getTopTechniciansBySatisfaction(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // This would calculate rankings based on satisfaction scores
        // For now, return empty list
        return List.of();
    }
    
    /**
     * Get satisfaction trends over time
     */
    @Transactional(readOnly = true)
    public List<SatisfactionTrend> getSatisfactionTrends(LocalDate startDate, LocalDate endDate, String groupBy) {
        // This would calculate satisfaction trends grouped by day/week/month
        // For now, return empty list
        return List.of();
    }
    
    // Private helper methods
    
    /**
     * Calculate satisfaction statistics from a list of scores
     */
    private SatisfactionStatistics calculateSatisfactionStatistics(List<SatisfactionScore> scores) {
        if (scores.isEmpty()) {
            return SatisfactionStatistics.builder()
                    .totalResponses(0)
                    .averageScore(BigDecimal.ZERO)
                    .positiveCount(0)
                    .neutralCount(0)
                    .negativeCount(0)
                    .build();
        }
        
        double averageScore = scores.stream()
                .mapToInt(SatisfactionScore::getScore)
                .average()
                .orElse(0.0);
        
        long positiveCount = scores.stream().filter(SatisfactionScore::estPositif).count();
        long neutralCount = scores.stream().filter(SatisfactionScore::estNeutre).count();
        long negativeCount = scores.stream().filter(SatisfactionScore::estNegatif).count();
        
        Map<Integer, Long> distribution = scores.stream()
                .collect(Collectors.groupingBy(
                        SatisfactionScore::getScore,
                        Collectors.counting()
                ));
        
        return SatisfactionStatistics.builder()
                .totalResponses(scores.size())
                .averageScore(BigDecimal.valueOf(averageScore).setScale(2, RoundingMode.HALF_UP))
                .positiveCount((int) positiveCount)
                .neutralCount((int) neutralCount)
                .negativeCount((int) negativeCount)
                .distribution(distribution)
                .build();
    }
    
    // Inner classes for statistics
    
    public static class SatisfactionStatistics {
        private int totalResponses;
        private BigDecimal averageScore;
        private int positiveCount;
        private int neutralCount;
        private int negativeCount;
        private Map<Integer, Long> distribution;
        
        public static SatisfactionStatisticsBuilder builder() {
            return new SatisfactionStatisticsBuilder();
        }
        
        // Getters and builder class would be here
        public static class SatisfactionStatisticsBuilder {
            private int totalResponses;
            private BigDecimal averageScore;
            private int positiveCount;
            private int neutralCount;
            private int negativeCount;
            private Map<Integer, Long> distribution;
            
            public SatisfactionStatisticsBuilder totalResponses(int totalResponses) {
                this.totalResponses = totalResponses;
                return this;
            }
            
            public SatisfactionStatisticsBuilder averageScore(BigDecimal averageScore) {
                this.averageScore = averageScore;
                return this;
            }
            
            public SatisfactionStatisticsBuilder positiveCount(int positiveCount) {
                this.positiveCount = positiveCount;
                return this;
            }
            
            public SatisfactionStatisticsBuilder neutralCount(int neutralCount) {
                this.neutralCount = neutralCount;
                return this;
            }
            
            public SatisfactionStatisticsBuilder negativeCount(int negativeCount) {
                this.negativeCount = negativeCount;
                return this;
            }
            
            public SatisfactionStatisticsBuilder distribution(Map<Integer, Long> distribution) {
                this.distribution = distribution;
                return this;
            }
            
            public SatisfactionStatistics build() {
                SatisfactionStatistics stats = new SatisfactionStatistics();
                stats.totalResponses = this.totalResponses;
                stats.averageScore = this.averageScore;
                stats.positiveCount = this.positiveCount;
                stats.neutralCount = this.neutralCount;
                stats.negativeCount = this.negativeCount;
                stats.distribution = this.distribution;
                return stats;
            }
        }
    }
    
    public static class TechnicianSatisfactionRanking {
        private UUID technicienId;
        private String nom;
        private String prenom;
        private BigDecimal averageScore;
        private int totalResponses;
        private int rank;
        
        // Getters and setters would be here
    }
    
    public static class SatisfactionTrend {
        private LocalDate date;
        private BigDecimal averageScore;
        private int totalResponses;
        
        // Getters and setters would be here
    }
}
