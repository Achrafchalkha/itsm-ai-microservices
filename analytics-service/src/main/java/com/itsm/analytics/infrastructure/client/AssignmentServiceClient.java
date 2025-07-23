package com.itsm.analytics.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST client for Assignment Service
 * Provides access to assignment metrics and statistics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${services.assignment-service.url:http://localhost:8084}")
    private String assignmentServiceUrl;
    
    /**
     * Get assignment statistics for a period
     */
    public AssignmentStatistics getAssignmentStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            String url = assignmentServiceUrl + "/api/assignments/statistics?startDate={startDate}&endDate={endDate}";
            
            ResponseEntity<AssignmentStatistics> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    AssignmentStatistics.class,
                    startDate,
                    endDate
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting assignment statistics: {}", e.getMessage(), e);
            return createDefaultAssignmentStatistics();
        }
    }
    
    /**
     * Get assignment statistics for a specific team
     */
    public AssignmentStatistics getTeamAssignmentStatistics(UUID teamId, LocalDate startDate, LocalDate endDate) {
        try {
            String url = assignmentServiceUrl + "/api/assignments/statistics/team/{teamId}?startDate={startDate}&endDate={endDate}";
            
            ResponseEntity<AssignmentStatistics> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    AssignmentStatistics.class,
                    teamId,
                    startDate,
                    endDate
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting team assignment statistics for team {}: {}", teamId, e.getMessage(), e);
            return createDefaultAssignmentStatistics();
        }
    }
    
    /**
     * Get assignment statistics for a specific technician
     */
    public AssignmentStatistics getTechnicianAssignmentStatistics(UUID technicienId, LocalDate startDate, LocalDate endDate) {
        try {
            String url = assignmentServiceUrl + "/api/assignments/statistics/technician/{technicienId}?startDate={startDate}&endDate={endDate}";
            
            ResponseEntity<AssignmentStatistics> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    AssignmentStatistics.class,
                    technicienId,
                    startDate,
                    endDate
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting technician assignment statistics for technician {}: {}", technicienId, e.getMessage(), e);
            return createDefaultAssignmentStatistics();
        }
    }
    
    /**
     * Get assignment strategy performance
     */
    public List<StrategyPerformance> getStrategyPerformance(LocalDate startDate, LocalDate endDate) {
        try {
            String url = assignmentServiceUrl + "/api/assignments/strategies/performance?startDate={startDate}&endDate={endDate}";
            
            ResponseEntity<List<StrategyPerformance>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<StrategyPerformance>>() {},
                    startDate,
                    endDate
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting strategy performance: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get assignment failures for analysis
     */
    public List<AssignmentFailure> getAssignmentFailures(LocalDate startDate, LocalDate endDate) {
        try {
            String url = assignmentServiceUrl + "/api/assignments/failures?startDate={startDate}&endDate={endDate}";
            
            ResponseEntity<List<AssignmentFailure>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AssignmentFailure>>() {},
                    startDate,
                    endDate
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting assignment failures: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get workload distribution for teams
     */
    public List<WorkloadDistribution> getWorkloadDistribution() {
        try {
            String url = assignmentServiceUrl + "/api/assignments/workload/distribution";
            
            ResponseEntity<List<WorkloadDistribution>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<WorkloadDistribution>>() {}
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting workload distribution: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get confidence score trends
     */
    public List<ConfidenceScoreTrend> getConfidenceScoreTrends(LocalDate startDate, LocalDate endDate) {
        try {
            String url = assignmentServiceUrl + "/api/assignments/confidence/trends?startDate={startDate}&endDate={endDate}";
            
            ResponseEntity<List<ConfidenceScoreTrend>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ConfidenceScoreTrend>>() {},
                    startDate,
                    endDate
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting confidence score trends: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    // Helper method to create default statistics when service is unavailable
    private AssignmentStatistics createDefaultAssignmentStatistics() {
        return AssignmentStatistics.builder()
                .totalAssignments(0)
                .successfulAssignments(0)
                .failedAssignments(0)
                .reassignments(0)
                .averageConfidenceScore(BigDecimal.ZERO)
                .averageAssignmentTime(BigDecimal.ZERO)
                .build();
    }
    
    // Inner classes for response DTOs
    
    public static class AssignmentStatistics {
        private Integer totalAssignments;
        private Integer successfulAssignments;
        private Integer failedAssignments;
        private Integer reassignments;
        private BigDecimal averageConfidenceScore;
        private BigDecimal averageAssignmentTime;
        
        public static AssignmentStatisticsBuilder builder() {
            return new AssignmentStatisticsBuilder();
        }
        
        // Getters and setters
        public Integer getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
        
        public Integer getSuccessfulAssignments() { return successfulAssignments; }
        public void setSuccessfulAssignments(Integer successfulAssignments) { this.successfulAssignments = successfulAssignments; }
        
        public Integer getFailedAssignments() { return failedAssignments; }
        public void setFailedAssignments(Integer failedAssignments) { this.failedAssignments = failedAssignments; }
        
        public Integer getReassignments() { return reassignments; }
        public void setReassignments(Integer reassignments) { this.reassignments = reassignments; }
        
        public BigDecimal getAverageConfidenceScore() { return averageConfidenceScore; }
        public void setAverageConfidenceScore(BigDecimal averageConfidenceScore) { this.averageConfidenceScore = averageConfidenceScore; }
        
        public BigDecimal getAverageAssignmentTime() { return averageAssignmentTime; }
        public void setAverageAssignmentTime(BigDecimal averageAssignmentTime) { this.averageAssignmentTime = averageAssignmentTime; }
        
        public static class AssignmentStatisticsBuilder {
            private Integer totalAssignments;
            private Integer successfulAssignments;
            private Integer failedAssignments;
            private Integer reassignments;
            private BigDecimal averageConfidenceScore;
            private BigDecimal averageAssignmentTime;
            
            public AssignmentStatisticsBuilder totalAssignments(Integer totalAssignments) {
                this.totalAssignments = totalAssignments;
                return this;
            }
            
            public AssignmentStatisticsBuilder successfulAssignments(Integer successfulAssignments) {
                this.successfulAssignments = successfulAssignments;
                return this;
            }
            
            public AssignmentStatisticsBuilder failedAssignments(Integer failedAssignments) {
                this.failedAssignments = failedAssignments;
                return this;
            }
            
            public AssignmentStatisticsBuilder reassignments(Integer reassignments) {
                this.reassignments = reassignments;
                return this;
            }
            
            public AssignmentStatisticsBuilder averageConfidenceScore(BigDecimal averageConfidenceScore) {
                this.averageConfidenceScore = averageConfidenceScore;
                return this;
            }
            
            public AssignmentStatisticsBuilder averageAssignmentTime(BigDecimal averageAssignmentTime) {
                this.averageAssignmentTime = averageAssignmentTime;
                return this;
            }
            
            public AssignmentStatistics build() {
                AssignmentStatistics stats = new AssignmentStatistics();
                stats.totalAssignments = this.totalAssignments;
                stats.successfulAssignments = this.successfulAssignments;
                stats.failedAssignments = this.failedAssignments;
                stats.reassignments = this.reassignments;
                stats.averageConfidenceScore = this.averageConfidenceScore;
                stats.averageAssignmentTime = this.averageAssignmentTime;
                return stats;
            }
        }
    }
    
    public static class StrategyPerformance {
        private String strategy;
        private Integer usageCount;
        private BigDecimal averageConfidenceScore;
        private BigDecimal successRate;
        
        // Getters and setters
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
        
        public Integer getUsageCount() { return usageCount; }
        public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
        
        public BigDecimal getAverageConfidenceScore() { return averageConfidenceScore; }
        public void setAverageConfidenceScore(BigDecimal averageConfidenceScore) { this.averageConfidenceScore = averageConfidenceScore; }
        
        public BigDecimal getSuccessRate() { return successRate; }
        public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
    }
    
    public static class AssignmentFailure {
        private UUID ticketId;
        private String category;
        private String priority;
        private String failureReason;
        private LocalDateTime failedAt;
        
        // Getters and setters
        public UUID getTicketId() { return ticketId; }
        public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        public LocalDateTime getFailedAt() { return failedAt; }
        public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
    }
    
    public static class WorkloadDistribution {
        private UUID teamId;
        private String teamName;
        private Integer totalTechnicians;
        private Integer activeTechnicians;
        private BigDecimal averageWorkload;
        private Integer maxWorkload;
        private Integer minWorkload;
        
        // Getters and setters
        public UUID getTeamId() { return teamId; }
        public void setTeamId(UUID teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public Integer getTotalTechnicians() { return totalTechnicians; }
        public void setTotalTechnicians(Integer totalTechnicians) { this.totalTechnicians = totalTechnicians; }
        
        public Integer getActiveTechnicians() { return activeTechnicians; }
        public void setActiveTechnicians(Integer activeTechnicians) { this.activeTechnicians = activeTechnicians; }
        
        public BigDecimal getAverageWorkload() { return averageWorkload; }
        public void setAverageWorkload(BigDecimal averageWorkload) { this.averageWorkload = averageWorkload; }
        
        public Integer getMaxWorkload() { return maxWorkload; }
        public void setMaxWorkload(Integer maxWorkload) { this.maxWorkload = maxWorkload; }
        
        public Integer getMinWorkload() { return minWorkload; }
        public void setMinWorkload(Integer minWorkload) { this.minWorkload = minWorkload; }
    }
    
    public static class ConfidenceScoreTrend {
        private LocalDate date;
        private String strategy;
        private BigDecimal averageConfidenceScore;
        private Integer assignmentCount;
        
        // Getters and setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
        
        public BigDecimal getAverageConfidenceScore() { return averageConfidenceScore; }
        public void setAverageConfidenceScore(BigDecimal averageConfidenceScore) { this.averageConfidenceScore = averageConfidenceScore; }
        
        public Integer getAssignmentCount() { return assignmentCount; }
        public void setAssignmentCount(Integer assignmentCount) { this.assignmentCount = assignmentCount; }
    }
}
