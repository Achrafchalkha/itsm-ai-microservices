package com.itsm.assignment.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.assignment.domain.model.Assignment;
import com.itsm.assignment.domain.model.AssignmentStatus;
import com.itsm.assignment.domain.service.AssignmentEngine;
import com.itsm.assignment.infrastructure.client.TicketServiceClient;
import com.itsm.assignment.infrastructure.client.UserServiceClient;
import com.itsm.assignment.infrastructure.kafka.AssignmentEventPublisher;
import com.itsm.assignment.infrastructure.kafka.event.AssignmentCreatedEvent;
import com.itsm.assignment.infrastructure.kafka.event.TicketCreatedEvent;
import com.itsm.assignment.infrastructure.persistence.entity.AssignmentEntity;
import com.itsm.assignment.infrastructure.persistence.repository.JpaAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Main application service for assignment operations
 * Orchestrates the assignment process and manages assignment lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {
    
    private final AssignmentEngine assignmentEngine;
    private final JpaAssignmentRepository assignmentRepository;
    private final AssignmentEventPublisher eventPublisher;
    private final TicketServiceClient ticketServiceClient;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;
    
    @Value("${assignment.fallback.notify-manager:true}")
    private boolean notifyManagerOnFailure;
    
    /**
     * Process automatic assignment for a newly created ticket
     */
    @Transactional
    public void processAutomaticAssignment(TicketCreatedEvent ticketEvent) {
        log.info("Processing automatic assignment for ticket: {}", ticketEvent.getTicketId());
        
        try {
            // Check if ticket is already assigned
            if (assignmentRepository.findByTicketIdAndStatus(
                    ticketEvent.getTicketId(), AssignmentStatus.ACTIVE).isPresent()) {
                log.info("Ticket {} is already assigned, skipping", ticketEvent.getTicketId());
                return;
            }
            
            // Run assignment engine
            AssignmentEngine.AssignmentResult result = assignmentEngine.assignTicket(
                    ticketEvent.getTicketId(),
                    ticketEvent.getDescription(),
                    ticketEvent.getCategorie(),
                    ticketEvent.getPriorite(),
                    ticketEvent.getEnableNlp()
            );
            
            if (result.isSuccess()) {
                // Save assignment
                AssignmentEntity assignmentEntity = convertToEntity(result.getAssignment());
                assignmentEntity = assignmentRepository.save(assignmentEntity);
                
                // Update ticket service
                ticketServiceClient.updateTicketAssignment(
                        ticketEvent.getTicketId(),
                        result.getAssignedTechnician().getId(),
                        result.getAssignedTechnician().getTeamId()
                );
                
                // Update technician workload
                userServiceClient.updateTechnicianWorkload(
                        result.getAssignedTechnician().getId(), 1);
                
                // Publish assignment created event
                AssignmentCreatedEvent assignmentEvent = createAssignmentCreatedEvent(
                        result.getAssignment(), result.getAssignedTechnician(), ticketEvent);
                eventPublisher.publishAssignmentCreated(assignmentEvent);
                
                log.info("Successfully assigned ticket {} to technician {}", 
                        ticketEvent.getTicketId(), result.getAssignedTechnician().getId());
                
            } else if (result.isRequiresWaiting()) {
                // Handle ticket waiting for available technician
                handleTicketWaiting(result, ticketEvent);
            } else {
                // Handle assignment failure
                handleAssignmentFailure(ticketEvent, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error processing automatic assignment for ticket {}: {}", 
                    ticketEvent.getTicketId(), e.getMessage(), e);
            handleAssignmentFailure(ticketEvent, "System error: " + e.getMessage());
        }
    }
    
    /**
     * Manually assign ticket to specific technician
     */
    @Transactional
    public Assignment manualAssignment(UUID ticketId, UUID technicianId, UUID assignedBy, String reason) {
        log.info("Processing manual assignment of ticket {} to technician {} by {}", 
                ticketId, technicianId, assignedBy);
        
        try {
            // Get ticket and technician details
            TicketServiceClient.TicketDTO ticket = ticketServiceClient.getTicketById(ticketId);
            UserServiceClient.TechnicianDTO technician = userServiceClient.getTechnicianById(technicianId);
            
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket not found: " + ticketId);
            }
            if (technician == null) {
                throw new IllegalArgumentException("Technician not found: " + technicianId);
            }
            
            // Check if ticket is already assigned
            assignmentRepository.findByTicketIdAndStatus(ticketId, AssignmentStatus.ACTIVE)
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Ticket is already assigned to technician: " + 
                                existing.getTechnicianId());
                    });
            
            // Create manual assignment
            Assignment assignment = Assignment.createAssignment(
                    ticketId,
                    technicianId,
                    technician.getTeamId(),
                    com.itsm.assignment.domain.model.AssignmentStrategy.BEST_SKILL, // Manual override
                    BigDecimal.valueOf(1.0), // Full confidence for manual assignment
                    "Manual assignment: " + reason,
                    null // No NLP analysis for manual assignment
            );
            
            // Save assignment
            AssignmentEntity assignmentEntity = convertToEntity(assignment);
            assignmentEntity = assignmentRepository.save(assignmentEntity);
            
            // Update external services
            ticketServiceClient.updateTicketAssignment(ticketId, technicianId, technician.getTeamId());
            userServiceClient.updateTechnicianWorkload(technicianId, 1);
            
            // Publish event
            TicketCreatedEvent ticketEvent = TicketCreatedEvent.builder()
                    .ticketId(ticket.getId())
                    .titre(ticket.getTitre())
                    .description(ticket.getDescription())
                    .categorie(ticket.getCategorie())
                    .priorite(ticket.getPriorite())
                    .utilisateurId(ticket.getUtilisateurId())
                    .enableNlp(false)
                    .dateCreation(ticket.getDateCreation())
                    .build();
            
            AssignmentCreatedEvent assignmentEvent = createAssignmentCreatedEvent(
                    assignment, technician, ticketEvent);
            eventPublisher.publishAssignmentCreated(assignmentEvent);
            
            log.info("Successfully manually assigned ticket {} to technician {}", ticketId, technicianId);
            return assignment;
            
        } catch (Exception e) {
            log.error("Error processing manual assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Manual assignment failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reassign ticket to different technician
     */
    @Transactional
    public Assignment reassignTicket(UUID ticketId, UUID newTechnicianId, UUID reassignedBy, String reason) {
        log.info("Reassigning ticket {} to technician {} by {}", ticketId, newTechnicianId, reassignedBy);
        
        try {
            // Find current assignment
            AssignmentEntity currentAssignment = assignmentRepository
                    .findByTicketIdAndStatus(ticketId, AssignmentStatus.ACTIVE)
                    .orElseThrow(() -> new IllegalArgumentException("No active assignment found for ticket: " + ticketId));
            
            // Get new technician details
            UserServiceClient.TechnicianDTO newTechnician = userServiceClient.getTechnicianById(newTechnicianId);
            if (newTechnician == null) {
                throw new IllegalArgumentException("Technician not found: " + newTechnicianId);
            }
            
            // Update current assignment status
            currentAssignment.setStatus(AssignmentStatus.REASSIGNED);
            currentAssignment.setReassignedBy(reassignedBy);
            currentAssignment.setReassignmentReason(reason);
            assignmentRepository.save(currentAssignment);
            
            // Decrease workload for previous technician
            userServiceClient.updateTechnicianWorkload(currentAssignment.getTechnicianId(), -1);
            
            // Create new assignment
            Assignment newAssignment = Assignment.createAssignment(
                    ticketId,
                    newTechnicianId,
                    newTechnician.getTeamId(),
                    com.itsm.assignment.domain.model.AssignmentStrategy.BEST_SKILL,
                    BigDecimal.valueOf(1.0),
                    "Reassignment: " + reason,
                    null
            );
            
            // Save new assignment
            AssignmentEntity newAssignmentEntity = convertToEntity(newAssignment);
            assignmentRepository.save(newAssignmentEntity);
            
            // Update external services
            ticketServiceClient.updateTicketAssignment(ticketId, newTechnicianId, newTechnician.getTeamId());
            userServiceClient.updateTechnicianWorkload(newTechnicianId, 1);
            
            // Publish reassignment event
            AssignmentEventPublisher.AssignmentReassignedEvent reassignmentEvent = 
                    AssignmentEventPublisher.AssignmentReassignedEvent.builder()
                            .assignmentId(newAssignment.getId())
                            .ticketId(ticketId)
                            .previousTechnicianId(currentAssignment.getTechnicianId())
                            .newTechnicianId(newTechnicianId)
                            .reassignedBy(reassignedBy)
                            .reassignmentReason(reason)
                            .reassignedAt(LocalDateTime.now())
                            .newTechnicianName(newTechnician.getFullName())
                            .build();
            
            eventPublisher.publishAssignmentReassigned(reassignmentEvent);
            
            log.info("Successfully reassigned ticket {} from {} to {}", 
                    ticketId, currentAssignment.getTechnicianId(), newTechnicianId);
            
            return newAssignment;
            
        } catch (Exception e) {
            log.error("Error reassigning ticket {}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Reassignment failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle technician becoming unavailable
     */
    @Transactional
    public void handleTechnicianUnavailable(UUID technicianId) {
        log.info("Handling technician unavailable: {}", technicianId);
        
        try {
            // Find all active assignments for this technician
            List<AssignmentEntity> activeAssignments = assignmentRepository
                    .findByTechnicianIdAndStatus(technicianId, AssignmentStatus.ACTIVE);
            
            for (AssignmentEntity assignment : activeAssignments) {
                log.info("Need to reassign ticket {} due to technician {} unavailability", 
                        assignment.getTicketId(), technicianId);
                
                // For now, just mark as needing reassignment
                // In a full implementation, you might trigger automatic reassignment
                assignment.setReassignmentReason("Technician became unavailable");
                assignmentRepository.save(assignment);
            }
            
        } catch (Exception e) {
            log.error("Error handling technician unavailable {}: {}", technicianId, e.getMessage(), e);
        }
    }
    
    /**
     * Handle ticket waiting for available technician
     */
    private void handleTicketWaiting(AssignmentEngine.AssignmentResult result, TicketCreatedEvent ticketEvent) {
        log.info("Ticket {} set to EN_ATTENTE - no available technicians in category {}",
                ticketEvent.getTicketId(), result.getTicketCategory());

        try {
            // Update ticket status to EN_ATTENTE
            ticketServiceClient.updateTicketStatus(ticketEvent.getTicketId(), "EN_ATTENTE");

            // Find team managers for this category and notify them
            notifyTeamManagersForCategory(result.getTicketCategory(), ticketEvent, result.getErrorMessage());

            log.info("Ticket {} successfully set to EN_ATTENTE with manager notifications sent",
                    ticketEvent.getTicketId());

        } catch (Exception e) {
            log.error("Error handling ticket waiting for ticket {}: {}", ticketEvent.getTicketId(), e.getMessage(), e);
        }
    }

    /**
     * Notify team managers for the category that a ticket is waiting
     */
    private void notifyTeamManagersForCategory(String category, TicketCreatedEvent ticketEvent, String reason) {
        try {
            // Get teams that handle this category
            List<UserServiceClient.TeamDTO> teams = userServiceClient.findTeamsByCategory(category);

            for (UserServiceClient.TeamDTO team : teams) {
                if (team.getManagerId() != null) {
                    // Publish manager notification event
                    AssignmentEventPublisher.ManagerNotificationEvent managerEvent =
                            AssignmentEventPublisher.ManagerNotificationEvent.builder()
                                    .managerId(team.getManagerId())
                                    .teamId(team.getId())
                                    .ticketId(ticketEvent.getTicketId())
                                    .ticketTitle(ticketEvent.getTitre())
                                    .ticketCategory(category)
                                    .ticketPriority(ticketEvent.getPriorite())
                                    .reason(reason)
                                    .notificationAt(LocalDateTime.now())
                                    .build();

                    eventPublisher.publishManagerNotification(managerEvent);

                    log.info("Notified manager {} of team {} about waiting ticket {}",
                            team.getManagerId(), team.getId(), ticketEvent.getTicketId());
                }
            }

        } catch (Exception e) {
            log.error("Error notifying team managers for category {}: {}", category, e.getMessage(), e);
        }
    }

    /**
     * Handle assignment failure
     */
    private void handleAssignmentFailure(TicketCreatedEvent ticketEvent, String errorMessage) {
        log.warn("Assignment failed for ticket {}: {}", ticketEvent.getTicketId(), errorMessage);

        if (notifyManagerOnFailure) {
            // Publish assignment failed event for manager notification
            AssignmentEventPublisher.AssignmentFailedEvent failedEvent =
                    AssignmentEventPublisher.AssignmentFailedEvent.builder()
                            .ticketId(ticketEvent.getTicketId())
                            .failureReason(errorMessage)
                            .failedAt(LocalDateTime.now())
                            .ticketCategory(ticketEvent.getCategorie())
                            .ticketPriority(ticketEvent.getPriorite())
                            .ticketCreatorId(ticketEvent.getUtilisateurId())
                            .build();

            eventPublisher.publishAssignmentFailed(failedEvent);
        }
    }
    
    /**
     * Convert domain Assignment to JPA entity
     */
    private AssignmentEntity convertToEntity(Assignment assignment) {
        return AssignmentEntity.builder()
                .id(assignment.getId())
                .ticketId(assignment.getTicketId())
                .technicianId(assignment.getTechnicianId())
                .teamId(assignment.getTeamId())
                .strategy(assignment.getStrategy())
                .confidenceScore(assignment.getConfidenceScore())
                .assignmentReason(assignment.getAssignmentReason())
                .nlpAnalysisJson(assignment.getNlpAnalysisJson())
                .assignedAt(assignment.getAssignedAt())
                .status(assignment.getStatus())
                .reassignedBy(assignment.getReassignedBy())
                .reassignmentReason(assignment.getReassignmentReason())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
    
    /**
     * Create assignment created event for publishing
     */
    private AssignmentCreatedEvent createAssignmentCreatedEvent(Assignment assignment,
                                                              UserServiceClient.TechnicianDTO technician,
                                                              TicketCreatedEvent ticketEvent) {
        return AssignmentCreatedEvent.builder()
                .assignmentId(assignment.getId())
                .ticketId(assignment.getTicketId())
                .technicianId(assignment.getTechnicianId())
                .teamId(assignment.getTeamId())
                .assignmentStrategy(assignment.getStrategy().name())
                .confidenceScore(assignment.getConfidenceScore())
                .assignmentReason(assignment.getAssignmentReason())
                .assignedAt(assignment.getAssignedAt())
                .ticketTitre(ticketEvent.getTitre())
                .ticketDescription(ticketEvent.getDescription())
                .ticketCategorie(ticketEvent.getCategorie())
                .ticketPriorite(ticketEvent.getPriorite())
                .ticketUtilisateurId(ticketEvent.getUtilisateurId())
                .technicianNom(technician.getNom())
                .technicianPrenom(technician.getPrenom())
                .technicianEmail(technician.getEmail())
                .teamName("") // Would need to fetch team details
                .build();
    }
}
