package com.itsm.analytics.application.service;

import com.itsm.analytics.domain.model.SLAAlert;
import com.itsm.analytics.domain.model.SLAConfiguration;
import com.itsm.analytics.infrastructure.persistence.repository.JpaSLAAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for monitoring SLA compliance and creating alerts
 * Tracks ticket SLA deadlines and escalates when necessary
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SLAMonitoringService {
    
    private final SLAConfigurationService slaConfigurationService;
    private final JpaSLAAlertRepository slaAlertRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Start SLA monitoring for a new ticket
     */
    public void startSLAMonitoring(UUID ticketId, String categorie, String priorite, LocalDateTime dateCreation) {
        log.info("Starting SLA monitoring for ticket: {} - Category: {}, Priority: {}", ticketId, categorie, priorite);
        
        try {
            // Calculate SLA deadlines
            LocalDateTime resolutionDeadline = slaConfigurationService.calculerDateLimiteSLA(categorie, priorite, dateCreation);
            LocalDateTime firstResponseDeadline = slaConfigurationService.calculerDateLimitePremiereReponse(categorie, priorite, dateCreation);
            
            log.debug("SLA deadlines for ticket {}: Resolution: {}, First Response: {}", 
                     ticketId, resolutionDeadline, firstResponseDeadline);
            
            // Store SLA monitoring information (this could be in a separate table)
            // For now, we'll just log the monitoring start
            
        } catch (Exception e) {
            log.error("Error starting SLA monitoring for ticket {}: {}", ticketId, e.getMessage(), e);
        }
    }
    
    /**
     * Update SLA monitoring when ticket priority changes
     */
    public void updateSLAMonitoring(UUID ticketId, String categorie, String newPriorite, LocalDateTime dateCreation) {
        log.info("Updating SLA monitoring for ticket: {} - New Priority: {}", ticketId, newPriorite);
        
        try {
            // Recalculate SLA deadlines with new priority
            LocalDateTime newResolutionDeadline = slaConfigurationService.calculerDateLimiteSLA(categorie, newPriorite, dateCreation);
            LocalDateTime newFirstResponseDeadline = slaConfigurationService.calculerDateLimitePremiereReponse(categorie, newPriorite, dateCreation);
            
            log.debug("Updated SLA deadlines for ticket {}: Resolution: {}, First Response: {}", 
                     ticketId, newResolutionDeadline, newFirstResponseDeadline);
            
            // Update existing alerts if any
            List<SLAAlert> existingAlerts = slaAlertRepository.findByTicketIdAndResolved(ticketId, false);
            for (SLAAlert alert : existingAlerts) {
                alert.resoudre("Priority changed - SLA deadlines updated");
                slaAlertRepository.save(alert);
            }
            
        } catch (Exception e) {
            log.error("Error updating SLA monitoring for ticket {}: {}", ticketId, e.getMessage(), e);
        }
    }
    
    /**
     * Stop SLA monitoring when ticket is resolved
     */
    public void stopSLAMonitoring(UUID ticketId) {
        log.info("Stopping SLA monitoring for ticket: {}", ticketId);
        
        try {
            // Resolve any existing alerts for this ticket
            List<SLAAlert> existingAlerts = slaAlertRepository.findByTicketIdAndResolved(ticketId, false);
            for (SLAAlert alert : existingAlerts) {
                alert.resoudre("Ticket resolved");
                slaAlertRepository.save(alert);
            }
            
            log.debug("Resolved {} SLA alerts for ticket: {}", existingAlerts.size(), ticketId);
            
        } catch (Exception e) {
            log.error("Error stopping SLA monitoring for ticket {}: {}", ticketId, e.getMessage(), e);
        }
    }
    
    /**
     * Create SLA breach alert
     */
    public void createSLABreachAlert(UUID ticketId, String categorie, String priorite, 
                                    LocalDateTime slaDeadline, UUID teamId, UUID technicienId) {
        log.warn("Creating SLA breach alert for ticket: {}", ticketId);
        
        try {
            // Calculate time remaining (will be negative for breached)
            long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), slaDeadline);
            
            // Determine alert level based on configuration
            SLAAlert.AlertLevel alertLevel = determineAlertLevel(categorie, priorite, LocalDateTime.now());
            
            // Create alert
            SLAAlert alert = SLAAlert.creerAlerte(
                    ticketId, 
                    SLAAlert.AlertType.BREACHED, 
                    alertLevel, 
                    slaDeadline, 
                    (int) minutesRemaining
            );
            
            SLAAlert savedAlert = slaAlertRepository.save(alert);
            
            // Send notification via Kafka
            sendSLAAlertNotification(savedAlert, teamId, technicienId);
            
            log.info("Created SLA breach alert: {} for ticket: {}", savedAlert.getId(), ticketId);
            
        } catch (Exception e) {
            log.error("Error creating SLA breach alert for ticket {}: {}", ticketId, e.getMessage(), e);
        }
    }
    
    /**
     * Scheduled job to check for approaching SLA deadlines
     * Runs every 15 minutes by default
     */
    @Scheduled(fixedRateString = "${analytics.sla.check-interval-minutes:15}000")
    public void checkApproachingSLADeadlines() {
        log.debug("Checking for approaching SLA deadlines");
        
        try {
            // This would query ticket-service for active tickets and check their SLA status
            // For now, we'll implement a placeholder
            
            // Get active tickets approaching SLA deadline
            // This would be implemented by calling ticket-service
            
            log.debug("Completed SLA deadline check");
            
        } catch (Exception e) {
            log.error("Error checking approaching SLA deadlines: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create approaching SLA alert
     */
    public void createApproachingSLAAlert(UUID ticketId, String categorie, String priorite,
                                         LocalDateTime slaDeadline, UUID teamId, UUID technicienId,
                                         int hoursBeforeDeadline) {
        log.info("Creating approaching SLA alert for ticket: {} - {} hours before deadline", ticketId, hoursBeforeDeadline);
        
        try {
            // Check if alert already exists for this ticket
            List<SLAAlert> existingAlerts = slaAlertRepository.findByTicketIdAndAlertTypeAndResolved(
                    ticketId, SLAAlert.AlertType.APPROACHING, false);
            
            if (!existingAlerts.isEmpty()) {
                log.debug("Approaching SLA alert already exists for ticket: {}", ticketId);
                return;
            }
            
            // Calculate time remaining
            long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), slaDeadline);
            
            // Determine alert level
            SLAAlert.AlertLevel alertLevel = determineAlertLevel(categorie, priorite, LocalDateTime.now());
            
            // Create alert
            SLAAlert alert = SLAAlert.creerAlerte(
                    ticketId,
                    SLAAlert.AlertType.APPROACHING,
                    alertLevel,
                    slaDeadline,
                    (int) minutesRemaining
            );
            
            SLAAlert savedAlert = slaAlertRepository.save(alert);
            
            // Send notification via Kafka
            sendSLAAlertNotification(savedAlert, teamId, technicienId);
            
            log.info("Created approaching SLA alert: {} for ticket: {}", savedAlert.getId(), ticketId);
            
        } catch (Exception e) {
            log.error("Error creating approaching SLA alert for ticket {}: {}", ticketId, e.getMessage(), e);
        }
    }
    
    /**
     * Escalate SLA alert to higher level
     */
    public void escalateSLAAlert(UUID alertId, UUID escalatedTo) {
        log.info("Escalating SLA alert: {} to: {}", alertId, escalatedTo);
        
        try {
            SLAAlert alert = slaAlertRepository.findById(alertId)
                    .orElseThrow(() -> new IllegalArgumentException("SLA alert not found: " + alertId));
            
            alert.escalader(escalatedTo);
            slaAlertRepository.save(alert);
            
            // Send escalation notification
            sendEscalationNotification(alert, escalatedTo);
            
            log.info("Escalated SLA alert: {} to: {}", alertId, escalatedTo);
            
        } catch (Exception e) {
            log.error("Error escalating SLA alert {}: {}", alertId, e.getMessage(), e);
        }
    }
    
    /**
     * Resolve SLA alert
     */
    public void resolveSLAAlert(UUID alertId, String resolutionAction) {
        log.info("Resolving SLA alert: {} with action: {}", alertId, resolutionAction);
        
        try {
            SLAAlert alert = slaAlertRepository.findById(alertId)
                    .orElseThrow(() -> new IllegalArgumentException("SLA alert not found: " + alertId));
            
            alert.resoudre(resolutionAction);
            slaAlertRepository.save(alert);
            
            log.info("Resolved SLA alert: {}", alertId);
            
        } catch (Exception e) {
            log.error("Error resolving SLA alert {}: {}", alertId, e.getMessage(), e);
        }
    }
    
    /**
     * Get active SLA alerts for a team
     */
    @Transactional(readOnly = true)
    public List<SLAAlert> getActiveSLAAlerts(UUID teamId) {
        // This would need to join with ticket data to filter by team
        // For now, return all unresolved alerts
        return slaAlertRepository.findByResolvedOrderByCreatedAtDesc(false);
    }
    
    /**
     * Get SLA alerts requiring escalation
     */
    @Transactional(readOnly = true)
    public List<SLAAlert> getAlertsRequiringEscalation() {
        // This would find alerts that need escalation based on time and configuration
        return slaAlertRepository.findByResolvedAndEscalatedAtIsNull(false);
    }
    
    // Private helper methods
    
    /**
     * Determine alert level based on SLA configuration
     */
    private SLAAlert.AlertLevel determineAlertLevel(String categorie, String priorite, LocalDateTime currentTime) {
        // Check if should escalate to admin
        if (slaConfigurationService.doitEscaladerAdmin(categorie, priorite, currentTime)) {
            return SLAAlert.AlertLevel.ADMIN;
        }
        
        // Check if should escalate to manager
        if (slaConfigurationService.doitEscaladerManager(categorie, priorite, currentTime)) {
            return SLAAlert.AlertLevel.MANAGER;
        }
        
        return SLAAlert.AlertLevel.MANAGER; // Default to manager level
    }
    
    /**
     * Send SLA alert notification via Kafka
     */
    private void sendSLAAlertNotification(SLAAlert alert, UUID teamId, UUID technicienId) {
        try {
            Map<String, Object> notificationData = Map.of(
                    "alertId", alert.getId().toString(),
                    "ticketId", alert.getTicketId().toString(),
                    "alertType", alert.getAlertType().name(),
                    "alertLevel", alert.getAlertLevel().name(),
                    "slaDeadline", alert.getSlaDeadline().toString(),
                    "timeRemainingMinutes", alert.getTimeRemainingMinutes(),
                    "teamId", teamId != null ? teamId.toString() : null,
                    "technicienId", technicienId != null ? technicienId.toString() : null,
                    "createdAt", alert.getCreatedAt().toString()
            );
            
            kafkaTemplate.send("sla.alert.created", notificationData);
            log.debug("Sent SLA alert notification for alert: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Error sending SLA alert notification for alert {}: {}", alert.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send escalation notification via Kafka
     */
    private void sendEscalationNotification(SLAAlert alert, UUID escalatedTo) {
        try {
            Map<String, Object> escalationData = Map.of(
                    "alertId", alert.getId().toString(),
                    "ticketId", alert.getTicketId().toString(),
                    "escalatedTo", escalatedTo.toString(),
                    "escalatedAt", alert.getEscalatedAt().toString(),
                    "alertLevel", alert.getAlertLevel().name()
            );
            
            kafkaTemplate.send("sla.alert.escalated", escalationData);
            log.debug("Sent escalation notification for alert: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Error sending escalation notification for alert {}: {}", alert.getId(), e.getMessage(), e);
        }
    }
}
