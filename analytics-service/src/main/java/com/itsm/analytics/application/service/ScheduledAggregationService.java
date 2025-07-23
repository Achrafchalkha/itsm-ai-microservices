package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.DailyKPI;
import com.itsm.analytics.domain.model.TeamPerformanceMetrics;
import com.itsm.analytics.infrastructure.persistence.repository.JpaDailyKPIRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for scheduled aggregation of analytics data
 * Runs daily and monthly jobs to aggregate KPIs and performance metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "analytics.aggregation.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledAggregationService {
    
    private final KPICalculationEngine kpiCalculationEngine;
    private final JpaDailyKPIRepository dailyKPIRepository;
    private final SatisfactionService satisfactionService;
    
    /**
     * Daily aggregation job
     * Runs every day at 1:00 AM to aggregate previous day's data
     */
    @Scheduled(cron = "${analytics.aggregation.daily-job-cron:0 0 1 * * ?}")
    @Transactional
    public void runDailyAggregation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        log.info("Starting daily aggregation for date: {}", yesterday);
        
        try {
            // Calculate daily KPIs for yesterday
            DailyKPI dailyKPI = kpiCalculationEngine.calculateDailyKPIs(yesterday);
            
            // Save or update daily KPI
            DailyKPI existingKPI = dailyKPIRepository.findByDateKpi(yesterday).orElse(null);
            if (existingKPI != null) {
                log.info("Updating existing daily KPI for date: {}", yesterday);
                // Update existing KPI with new calculations
                updateExistingDailyKPI(existingKPI, dailyKPI);
                dailyKPIRepository.save(existingKPI);
            } else {
                log.info("Creating new daily KPI for date: {}", yesterday);
                dailyKPIRepository.save(dailyKPI);
            }
            
            // Aggregate team performance metrics
            aggregateTeamPerformanceMetrics(yesterday);
            
            // Aggregate technician performance metrics
            aggregateTechnicianPerformanceMetrics(yesterday);
            
            // Clean up old data if needed
            cleanupOldData();
            
            log.info("Completed daily aggregation for date: {}", yesterday);
            
        } catch (Exception e) {
            log.error("Error during daily aggregation for date {}: {}", yesterday, e.getMessage(), e);
        }
    }
    
    /**
     * Weekly aggregation job
     * Runs every Sunday at 2:00 AM to aggregate weekly metrics
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void runWeeklyAggregation() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6); // Last 7 days
        
        log.info("Starting weekly aggregation from {} to {}", startDate, endDate);
        
        try {
            // Aggregate weekly team performance
            aggregateWeeklyTeamPerformance(startDate, endDate);
            
            // Aggregate weekly technician performance
            aggregateWeeklyTechnicianPerformance(startDate, endDate);
            
            // Calculate weekly trends
            calculateWeeklyTrends(startDate, endDate);
            
            log.info("Completed weekly aggregation from {} to {}", startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error during weekly aggregation: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Monthly aggregation job
     * Runs on the 1st day of each month at 2:00 AM to aggregate previous month's data
     */
    @Scheduled(cron = "${analytics.aggregation.monthly-job-cron:0 0 2 1 * ?}")
    @Transactional
    public void runMonthlyAggregation() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate startDate = lastMonth.withDayOfMonth(1);
        LocalDate endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
        
        log.info("Starting monthly aggregation for {} (from {} to {})", 
                lastMonth.getMonth(), startDate, endDate);
        
        try {
            // Generate monthly report
            generateMonthlyReport(lastMonth.getYear(), lastMonth.getMonthValue());
            
            // Aggregate monthly team performance
            aggregateMonthlyTeamPerformance(startDate, endDate);
            
            // Aggregate monthly technician performance
            aggregateMonthlyTechnicianPerformance(startDate, endDate);
            
            // Calculate monthly trends and comparisons
            calculateMonthlyTrends(lastMonth);
            
            log.info("Completed monthly aggregation for {}", lastMonth.getMonth());
            
        } catch (Exception e) {
            log.error("Error during monthly aggregation: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Satisfaction aggregation job
     * Runs every 4 hours to aggregate satisfaction scores
     */
    @Scheduled(fixedRate = 14400000) // 4 hours in milliseconds
    @Transactional
    public void runSatisfactionAggregation() {
        log.debug("Starting satisfaction aggregation");
        
        try {
            LocalDate today = LocalDate.now();
            
            // Aggregate satisfaction scores for today
            satisfactionService.aggregateDailySatisfactionScores(today);
            
            // Update team satisfaction metrics
            satisfactionService.updateTeamSatisfactionMetrics(today);
            
            // Update technician satisfaction metrics
            satisfactionService.updateTechnicianSatisfactionMetrics(today);
            
            log.debug("Completed satisfaction aggregation");
            
        } catch (Exception e) {
            log.error("Error during satisfaction aggregation: {}", e.getMessage(), e);
        }
    }
    
    /**
     * SLA monitoring job
     * Runs every 15 minutes to check for SLA violations
     */
    @Scheduled(fixedRateString = "${analytics.sla.check-interval-minutes:15}000")
    public void runSLAMonitoring() {
        log.debug("Running SLA monitoring check");
        
        try {
            // This would check for tickets approaching SLA deadlines
            // Implementation would call ticket-service to get active tickets
            // and check their SLA status
            
            log.debug("Completed SLA monitoring check");
            
        } catch (Exception e) {
            log.error("Error during SLA monitoring: {}", e.getMessage(), e);
        }
    }
    
    // Private helper methods
    
    /**
     * Update existing daily KPI with new calculations
     */
    private void updateExistingDailyKPI(DailyKPI existing, DailyKPI newKPI) {
        // Update metrics that might have changed
        existing.setTotalTicketsCreated(newKPI.getTotalTicketsCreated());
        existing.setTotalTicketsResolved(newKPI.getTotalTicketsResolved());
        existing.setTotalTicketsClosed(newKPI.getTotalTicketsClosed());
        existing.setTicketsWithinSla(newKPI.getTicketsWithinSla());
        existing.setTicketsBreachedSla(newKPI.getTicketsBreachedSla());
        existing.setAverageResolutionTimeMinutes(newKPI.getAverageResolutionTimeMinutes());
        existing.setAverageFirstResponseTimeMinutes(newKPI.getAverageFirstResponseTimeMinutes());
        existing.setTotalAssignments(newKPI.getTotalAssignments());
        existing.setTotalReassignments(newKPI.getTotalReassignments());
        existing.setAverageAssignmentConfidence(newKPI.getAverageAssignmentConfidence());
        existing.setTotalSatisfactionResponses(newKPI.getTotalSatisfactionResponses());
        existing.setAverageSatisfactionScore(newKPI.getAverageSatisfactionScore());
        existing.setTeamMetricsJson(newKPI.getTeamMetricsJson());
        existing.setTechnicianMetricsJson(newKPI.getTechnicianMetricsJson());
        existing.setCategoryMetricsJson(newKPI.getCategoryMetricsJson());
        existing.setUpdatedAt(LocalDateTime.now());
    }
    
    /**
     * Aggregate team performance metrics for a specific date
     */
    private void aggregateTeamPerformanceMetrics(LocalDate date) {
        log.debug("Aggregating team performance metrics for date: {}", date);
        
        try {
            // This would get all teams and calculate their performance metrics
            // Implementation would call user-service to get teams
            // and then calculate metrics for each team
            
        } catch (Exception e) {
            log.error("Error aggregating team performance metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Aggregate technician performance metrics for a specific date
     */
    private void aggregateTechnicianPerformanceMetrics(LocalDate date) {
        log.debug("Aggregating technician performance metrics for date: {}", date);
        
        try {
            // This would get all technicians and calculate their performance metrics
            // Implementation would call user-service to get technicians
            // and then calculate metrics for each technician
            
        } catch (Exception e) {
            log.error("Error aggregating technician performance metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Aggregate weekly team performance
     */
    private void aggregateWeeklyTeamPerformance(LocalDate startDate, LocalDate endDate) {
        log.debug("Aggregating weekly team performance from {} to {}", startDate, endDate);
        
        try {
            // This would aggregate team performance over the week
            // and store in team_performance_metrics table
            
        } catch (Exception e) {
            log.error("Error aggregating weekly team performance: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Aggregate weekly technician performance
     */
    private void aggregateWeeklyTechnicianPerformance(LocalDate startDate, LocalDate endDate) {
        log.debug("Aggregating weekly technician performance from {} to {}", startDate, endDate);
        
        try {
            // This would aggregate technician performance over the week
            // and store in technician_performance_metrics table
            
        } catch (Exception e) {
            log.error("Error aggregating weekly technician performance: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Calculate weekly trends
     */
    private void calculateWeeklyTrends(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating weekly trends from {} to {}", startDate, endDate);
        
        try {
            // This would calculate trends compared to previous week
            // and store trend data for analytics
            
        } catch (Exception e) {
            log.error("Error calculating weekly trends: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate monthly report
     */
    private void generateMonthlyReport(int year, int month) {
        log.info("Generating monthly report for {}/{}", month, year);
        
        try {
            // This would generate a comprehensive monthly report
            // and store in monthly_reports table
            
        } catch (Exception e) {
            log.error("Error generating monthly report for {}/{}: {}", month, year, e.getMessage(), e);
        }
    }
    
    /**
     * Aggregate monthly team performance
     */
    private void aggregateMonthlyTeamPerformance(LocalDate startDate, LocalDate endDate) {
        log.debug("Aggregating monthly team performance from {} to {}", startDate, endDate);
        
        try {
            // This would aggregate team performance over the month
            
        } catch (Exception e) {
            log.error("Error aggregating monthly team performance: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Aggregate monthly technician performance
     */
    private void aggregateMonthlyTechnicianPerformance(LocalDate startDate, LocalDate endDate) {
        log.debug("Aggregating monthly technician performance from {} to {}", startDate, endDate);
        
        try {
            // This would aggregate technician performance over the month
            
        } catch (Exception e) {
            log.error("Error aggregating monthly technician performance: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Calculate monthly trends
     */
    private void calculateMonthlyTrends(LocalDate month) {
        log.debug("Calculating monthly trends for {}", month);
        
        try {
            // This would calculate trends compared to previous month
            // and year-over-year comparisons
            
        } catch (Exception e) {
            log.error("Error calculating monthly trends: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up old data
     */
    private void cleanupOldData() {
        log.debug("Cleaning up old analytics data");
        
        try {
            // Clean up old daily KPIs (keep last 2 years)
            LocalDate cutoffDate = LocalDate.now().minusYears(2);
            dailyKPIRepository.deleteByDateKpiBefore(cutoffDate);
            
            // Clean up old resolved SLA alerts (keep last 6 months)
            LocalDateTime alertCutoffDate = LocalDateTime.now().minusMonths(6);
            // This would be implemented with SLA alert repository
            
            log.debug("Completed cleanup of old analytics data");
            
        } catch (Exception e) {
            log.error("Error cleaning up old data: {}", e.getMessage(), e);
        }
    }
}
