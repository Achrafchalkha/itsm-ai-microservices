package com.itsm.analytics.infrastructure.kafka;

import com.itsm.analytics.application.service.AnalyticsAggregationService;
import com.itsm.analytics.application.service.SLAMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka listener for ticket events
 * Updates analytics in real-time when tickets are created, updated, or resolved
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {
    
    private final AnalyticsAggregationService aggregationService;
    private final SLAMonitoringService slaMonitoringService;
    
    /**
     * Handle ticket created events
     */
    @KafkaListener(topics = "ticket.created", groupId = "analytics-service-group")
    public void handleTicketCreated(@Payload Map<String, Object> ticketData,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        
        log.info("Received ticket.created event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        
        try {
            UUID ticketId = UUID.fromString((String) ticketData.get("id"));
            String categorie = (String) ticketData.get("categorie");
            String priorite = (String) ticketData.get("priorite");
            LocalDateTime dateCreation = LocalDateTime.parse((String) ticketData.get("dateCreation"));
            
            log.debug("Processing ticket created: {} - Category: {}, Priority: {}", ticketId, categorie, priorite);
            
            // Update daily KPI aggregations
            aggregationService.incrementTicketsCreated(dateCreation.toLocalDate());
            
            // Start SLA monitoring for this ticket
            slaMonitoringService.startSLAMonitoring(ticketId, categorie, priorite, dateCreation);
            
            // Update category-specific metrics
            aggregationService.updateCategoryMetrics(categorie, "CREATED", 1);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed ticket.created event for ticket: {}", ticketId);
            
        } catch (Exception e) {
            log.error("Error processing ticket.created event: {}", e.getMessage(), e);
            // Don't acknowledge on error - message will be retried
        }
    }
    
    /**
     * Handle ticket resolved events
     */
    @KafkaListener(topics = "ticket.resolved", groupId = "analytics-service-group")
    public void handleTicketResolved(@Payload Map<String, Object> ticketData,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment acknowledgment) {
        
        log.info("Received ticket.resolved event from topic: {}", topic);
        
        try {
            UUID ticketId = UUID.fromString((String) ticketData.get("id"));
            String categorie = (String) ticketData.get("categorie");
            UUID technicienId = ticketData.get("technicienId") != null ? 
                UUID.fromString((String) ticketData.get("technicienId")) : null;
            UUID teamId = ticketData.get("teamId") != null ? 
                UUID.fromString((String) ticketData.get("teamId")) : null;
            
            LocalDateTime dateResolution = ticketData.get("dateResolution") != null ?
                LocalDateTime.parse((String) ticketData.get("dateResolution")) : LocalDateTime.now();
            
            Integer tempsResolutionMinutes = (Integer) ticketData.get("tempsResolutionMinutes");
            Boolean slaRespecte = (Boolean) ticketData.get("slaRespecte");
            
            log.debug("Processing ticket resolved: {} - SLA respected: {}, Resolution time: {} minutes", 
                     ticketId, slaRespecte, tempsResolutionMinutes);
            
            // Update daily KPI aggregations
            aggregationService.incrementTicketsResolved(dateResolution.toLocalDate());
            
            // Update SLA metrics
            if (slaRespecte != null) {
                if (slaRespecte) {
                    aggregationService.incrementSLACompliant(dateResolution.toLocalDate());
                } else {
                    aggregationService.incrementSLABreached(dateResolution.toLocalDate());
                }
            }
            
            // Update team performance metrics
            if (teamId != null) {
                aggregationService.updateTeamResolutionMetrics(teamId, dateResolution.toLocalDate(), 
                                                              tempsResolutionMinutes, slaRespecte);
            }
            
            // Update technician performance metrics
            if (technicienId != null) {
                aggregationService.updateTechnicianResolutionMetrics(technicienId, dateResolution.toLocalDate(),
                                                                    tempsResolutionMinutes, slaRespecte);
            }
            
            // Stop SLA monitoring for this ticket
            slaMonitoringService.stopSLAMonitoring(ticketId);
            
            // Update category-specific metrics
            aggregationService.updateCategoryMetrics(categorie, "RESOLVED", 1);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed ticket.resolved event for ticket: {}", ticketId);
            
        } catch (Exception e) {
            log.error("Error processing ticket.resolved event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle ticket status updated events
     */
    @KafkaListener(topics = "ticket.status.updated", groupId = "analytics-service-group")
    public void handleTicketStatusUpdated(@Payload Map<String, Object> ticketData,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         Acknowledgment acknowledgment) {
        
        log.info("Received ticket.status.updated event from topic: {}", topic);
        
        try {
            UUID ticketId = UUID.fromString((String) ticketData.get("id"));
            String oldStatus = (String) ticketData.get("oldStatus");
            String newStatus = (String) ticketData.get("newStatus");
            LocalDateTime dateModification = LocalDateTime.parse((String) ticketData.get("dateModification"));
            
            log.debug("Processing ticket status update: {} - {} -> {}", ticketId, oldStatus, newStatus);
            
            // Update status transition metrics
            aggregationService.updateStatusTransitionMetrics(dateModification.toLocalDate(), oldStatus, newStatus);
            
            // Check if ticket moved to closed status
            if ("FERME".equals(newStatus)) {
                aggregationService.incrementTicketsClosed(dateModification.toLocalDate());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed ticket.status.updated event for ticket: {}", ticketId);
            
        } catch (Exception e) {
            log.error("Error processing ticket.status.updated event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle ticket SLA breach events
     */
    @KafkaListener(topics = "ticket.sla.breached", groupId = "analytics-service-group")
    public void handleTicketSLABreached(@Payload Map<String, Object> ticketData,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       Acknowledgment acknowledgment) {
        
        log.info("Received ticket.sla.breached event from topic: {}", topic);
        
        try {
            UUID ticketId = UUID.fromString((String) ticketData.get("id"));
            String categorie = (String) ticketData.get("categorie");
            String priorite = (String) ticketData.get("priorite");
            UUID teamId = ticketData.get("teamId") != null ? 
                UUID.fromString((String) ticketData.get("teamId")) : null;
            UUID technicienId = ticketData.get("technicienId") != null ? 
                UUID.fromString((String) ticketData.get("technicienId")) : null;
            
            LocalDateTime slaDeadline = LocalDateTime.parse((String) ticketData.get("slaDeadline"));
            
            log.warn("SLA breached for ticket: {} - Category: {}, Priority: {}", ticketId, categorie, priorite);
            
            // Create SLA alert
            slaMonitoringService.createSLABreachAlert(ticketId, categorie, priorite, slaDeadline, teamId, technicienId);
            
            // Update SLA breach metrics
            aggregationService.incrementSLABreached(LocalDateTime.now().toLocalDate());
            
            // Update team SLA metrics
            if (teamId != null) {
                aggregationService.updateTeamSLABreach(teamId, LocalDateTime.now().toLocalDate());
            }
            
            // Update technician SLA metrics
            if (technicienId != null) {
                aggregationService.updateTechnicianSLABreach(technicienId, LocalDateTime.now().toLocalDate());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed ticket.sla.breached event for ticket: {}", ticketId);
            
        } catch (Exception e) {
            log.error("Error processing ticket.sla.breached event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle ticket priority updated events
     */
    @KafkaListener(topics = "ticket.priority.updated", groupId = "analytics-service-group")
    public void handleTicketPriorityUpdated(@Payload Map<String, Object> ticketData,
                                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                           Acknowledgment acknowledgment) {
        
        log.info("Received ticket.priority.updated event from topic: {}", topic);
        
        try {
            UUID ticketId = UUID.fromString((String) ticketData.get("id"));
            String oldPriority = (String) ticketData.get("oldPriority");
            String newPriority = (String) ticketData.get("newPriority");
            String categorie = (String) ticketData.get("categorie");
            LocalDateTime dateCreation = LocalDateTime.parse((String) ticketData.get("dateCreation"));
            
            log.debug("Processing ticket priority update: {} - {} -> {}", ticketId, oldPriority, newPriority);
            
            // Update SLA monitoring with new priority
            slaMonitoringService.updateSLAMonitoring(ticketId, categorie, newPriority, dateCreation);
            
            // Update priority change metrics
            aggregationService.updatePriorityChangeMetrics(LocalDateTime.now().toLocalDate(), oldPriority, newPriority);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed ticket.priority.updated event for ticket: {}", ticketId);
            
        } catch (Exception e) {
            log.error("Error processing ticket.priority.updated event: {}", e.getMessage(), e);
        }
    }
}
