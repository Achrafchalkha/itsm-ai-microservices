package com.itsm.analytics.infrastructure.kafka;

import com.itsm.analytics.application.service.AnalyticsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka listener for assignment events
 * Updates analytics when tickets are assigned, reassigned, or assignment fails
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentEventListener {
    
    private final AnalyticsAggregationService aggregationService;
    
    /**
     * Handle assignment created events
     */
    @KafkaListener(topics = "assignment.created", groupId = "analytics-service-group")
    public void handleAssignmentCreated(@Payload Map<String, Object> assignmentData,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                       @Header(KafkaHeaders.OFFSET) long offset,
                                       Acknowledgment acknowledgment) {
        
        log.info("Received assignment.created event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        
        try {
            UUID assignmentId = UUID.fromString((String) assignmentData.get("id"));
            UUID ticketId = UUID.fromString((String) assignmentData.get("ticketId"));
            UUID technicienId = UUID.fromString((String) assignmentData.get("technicienId"));
            UUID teamId = UUID.fromString((String) assignmentData.get("teamId"));
            String strategy = (String) assignmentData.get("strategy");
            
            BigDecimal confidenceScore = assignmentData.get("confidenceScore") != null ?
                new BigDecimal(assignmentData.get("confidenceScore").toString()) : null;
            
            LocalDateTime assignedAt = LocalDateTime.parse((String) assignmentData.get("assignedAt"));
            
            log.debug("Processing assignment created: {} - Ticket: {}, Technician: {}, Strategy: {}, Confidence: {}", 
                     assignmentId, ticketId, technicienId, strategy, confidenceScore);
            
            // Update daily assignment metrics
            aggregationService.incrementAssignments(assignedAt.toLocalDate());
            
            // Update assignment strategy metrics
            aggregationService.updateAssignmentStrategyMetrics(assignedAt.toLocalDate(), strategy, confidenceScore);
            
            // Update team assignment metrics
            aggregationService.updateTeamAssignmentMetrics(teamId, assignedAt.toLocalDate());
            
            // Update technician assignment metrics
            aggregationService.updateTechnicianAssignmentMetrics(technicienId, assignedAt.toLocalDate());
            
            // Update confidence score statistics
            if (confidenceScore != null) {
                aggregationService.updateConfidenceScoreMetrics(assignedAt.toLocalDate(), strategy, confidenceScore);
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed assignment.created event for assignment: {}", assignmentId);
            
        } catch (Exception e) {
            log.error("Error processing assignment.created event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle assignment reassigned events
     */
    @KafkaListener(topics = "assignment.reassigned", groupId = "analytics-service-group")
    public void handleAssignmentReassigned(@Payload Map<String, Object> reassignmentData,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          Acknowledgment acknowledgment) {
        
        log.info("Received assignment.reassigned event from topic: {}", topic);
        
        try {
            UUID assignmentId = UUID.fromString((String) reassignmentData.get("id"));
            UUID ticketId = UUID.fromString((String) reassignmentData.get("ticketId"));
            UUID oldTechnicienId = UUID.fromString((String) reassignmentData.get("oldTechnicienId"));
            UUID newTechnicienId = UUID.fromString((String) reassignmentData.get("newTechnicienId"));
            UUID teamId = UUID.fromString((String) reassignmentData.get("teamId"));
            String reason = (String) reassignmentData.get("reason");
            
            LocalDateTime reassignedAt = LocalDateTime.parse((String) reassignmentData.get("reassignedAt"));
            
            log.debug("Processing assignment reassigned: {} - Ticket: {}, From: {} To: {}, Reason: {}", 
                     assignmentId, ticketId, oldTechnicienId, newTechnicienId, reason);
            
            // Update daily reassignment metrics
            aggregationService.incrementReassignments(reassignedAt.toLocalDate());
            
            // Update team reassignment metrics
            aggregationService.updateTeamReassignmentMetrics(teamId, reassignedAt.toLocalDate());
            
            // Update technician reassignment metrics (both old and new)
            aggregationService.updateTechnicianReassignmentFrom(oldTechnicienId, reassignedAt.toLocalDate());
            aggregationService.updateTechnicianReassignmentTo(newTechnicienId, reassignedAt.toLocalDate());
            
            // Update reassignment reason metrics
            aggregationService.updateReassignmentReasonMetrics(reassignedAt.toLocalDate(), reason);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed assignment.reassigned event for assignment: {}", assignmentId);
            
        } catch (Exception e) {
            log.error("Error processing assignment.reassigned event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle assignment failed events
     */
    @KafkaListener(topics = "assignment.failed", groupId = "analytics-service-group")
    public void handleAssignmentFailed(@Payload Map<String, Object> failureData,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        
        log.info("Received assignment.failed event from topic: {}", topic);
        
        try {
            UUID ticketId = UUID.fromString((String) failureData.get("ticketId"));
            String categorie = (String) failureData.get("categorie");
            String priorite = (String) failureData.get("priorite");
            String failureReason = (String) failureData.get("failureReason");
            UUID teamId = failureData.get("teamId") != null ? 
                UUID.fromString((String) failureData.get("teamId")) : null;
            
            LocalDateTime failedAt = LocalDateTime.parse((String) failureData.get("failedAt"));
            
            log.warn("Processing assignment failure: Ticket: {}, Category: {}, Priority: {}, Reason: {}", 
                    ticketId, categorie, priorite, failureReason);
            
            // Update daily assignment failure metrics
            aggregationService.incrementAssignmentFailures(failedAt.toLocalDate());
            
            // Update failure reason metrics
            aggregationService.updateAssignmentFailureReasonMetrics(failedAt.toLocalDate(), failureReason);
            
            // Update category-specific failure metrics
            aggregationService.updateCategoryFailureMetrics(categorie, failedAt.toLocalDate());
            
            // Update team failure metrics if team was identified
            if (teamId != null) {
                aggregationService.updateTeamAssignmentFailureMetrics(teamId, failedAt.toLocalDate());
            }
            
            // Update priority-specific failure metrics
            aggregationService.updatePriorityFailureMetrics(priorite, failedAt.toLocalDate());
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed assignment.failed event for ticket: {}", ticketId);
            
        } catch (Exception e) {
            log.error("Error processing assignment.failed event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle technician status updated events
     */
    @KafkaListener(topics = "technician.status.updated", groupId = "analytics-service-group")
    public void handleTechnicianStatusUpdated(@Payload Map<String, Object> statusData,
                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                             Acknowledgment acknowledgment) {
        
        log.info("Received technician.status.updated event from topic: {}", topic);
        
        try {
            UUID technicienId = UUID.fromString((String) statusData.get("technicienId"));
            UUID teamId = UUID.fromString((String) statusData.get("teamId"));
            Boolean oldActif = (Boolean) statusData.get("oldActif");
            Boolean newActif = (Boolean) statusData.get("newActif");
            Integer oldChargeActuelle = (Integer) statusData.get("oldChargeActuelle");
            Integer newChargeActuelle = (Integer) statusData.get("newChargeActuelle");
            
            LocalDateTime updatedAt = LocalDateTime.parse((String) statusData.get("updatedAt"));
            
            log.debug("Processing technician status update: {} - Active: {} -> {}, Workload: {} -> {}", 
                     technicienId, oldActif, newActif, oldChargeActuelle, newChargeActuelle);
            
            // Update technician availability metrics
            if (!oldActif.equals(newActif)) {
                aggregationService.updateTechnicianAvailabilityMetrics(teamId, updatedAt.toLocalDate(), 
                                                                      newActif ? 1 : -1);
            }
            
            // Update workload change metrics
            if (!oldChargeActuelle.equals(newChargeActuelle)) {
                int workloadChange = newChargeActuelle - oldChargeActuelle;
                aggregationService.updateWorkloadChangeMetrics(teamId, technicienId, updatedAt.toLocalDate(), 
                                                              workloadChange);
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed technician.status.updated event for technician: {}", technicienId);
            
        } catch (Exception e) {
            log.error("Error processing technician.status.updated event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle team member added events
     */
    @KafkaListener(topics = "team.member.added", groupId = "analytics-service-group")
    public void handleTeamMemberAdded(@Payload Map<String, Object> memberData,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment acknowledgment) {
        
        log.info("Received team.member.added event from topic: {}", topic);
        
        try {
            UUID teamId = UUID.fromString((String) memberData.get("teamId"));
            UUID technicienId = UUID.fromString((String) memberData.get("technicienId"));
            LocalDateTime addedAt = LocalDateTime.parse((String) memberData.get("addedAt"));
            
            log.debug("Processing team member added: Team: {}, Technician: {}", teamId, technicienId);
            
            // Update team size metrics
            aggregationService.updateTeamSizeMetrics(teamId, addedAt.toLocalDate(), 1);
            
            // Initialize technician metrics for this team
            aggregationService.initializeTechnicianMetrics(technicienId, teamId, addedAt.toLocalDate());
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed team.member.added event for team: {}, technician: {}", teamId, technicienId);
            
        } catch (Exception e) {
            log.error("Error processing team.member.added event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle team member removed events
     */
    @KafkaListener(topics = "team.member.removed", groupId = "analytics-service-group")
    public void handleTeamMemberRemoved(@Payload Map<String, Object> memberData,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       Acknowledgment acknowledgment) {
        
        log.info("Received team.member.removed event from topic: {}", topic);
        
        try {
            UUID teamId = UUID.fromString((String) memberData.get("teamId"));
            UUID technicienId = UUID.fromString((String) memberData.get("technicienId"));
            LocalDateTime removedAt = LocalDateTime.parse((String) memberData.get("removedAt"));
            
            log.debug("Processing team member removed: Team: {}, Technician: {}", teamId, technicienId);
            
            // Update team size metrics
            aggregationService.updateTeamSizeMetrics(teamId, removedAt.toLocalDate(), -1);
            
            // Archive technician metrics for this team
            aggregationService.archiveTechnicianMetrics(technicienId, teamId, removedAt.toLocalDate());
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed team.member.removed event for team: {}, technician: {}", teamId, technicienId);
            
        } catch (Exception e) {
            log.error("Error processing team.member.removed event: {}", e.getMessage(), e);
        }
    }
}
