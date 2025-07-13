package com.itsm.user.infrastructure.kafka;

import com.itsm.user.application.service.UserService;
import com.itsm.user.application.service.TeamService;
import com.itsm.user.domain.model.NiveauCompetence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka event listener for TechnicianCreatedEvent from auth-service
 * Handles the team assignment when technicians are created by managers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TechnicianCreatedEventListener {
    
    private final UserService userService;
    private final TeamService teamService;
    
    /**
     * Listen for technician creation events from auth-service
     * Updates the technician's team assignment and manager
     */
    @KafkaListener(
            topics = "technician-created",
            groupId = "user-service-group",
            containerFactory = "technicianKafkaListenerContainerFactory"
    )
    public void handleTechnicianCreated(
            @Payload TechnicianCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received TechnicianCreatedEvent from topic: {}, partition: {}, offset: {}, technicianId: {}, teamId: {}", 
                topic, partition, offset, event.getTechnicianId(), event.getTeamId());
        
        try {
            // Find the manager's team and assign technician to it
            if (event.getManagerId() != null) {
                // Find the team managed by this manager
                UUID managerTeamId = teamService.obtenirEquipeParManager(event.getManagerId());

                if (managerTeamId != null) {
                    // Assign technician to the manager's team
                    userService.assignerEquipe(event.getTechnicianId(), managerTeamId);
                    log.info("Assigned technician {} to manager's team {}", event.getTechnicianId(), managerTeamId);

                    // Add technician to team members
                    teamService.ajouterMembreEquipe(managerTeamId, event.getTechnicianId(), event.getManagerId());
                    log.info("Added technician {} as member of team {}", event.getTechnicianId(), managerTeamId);
                } else {
                    log.warn("No team found for manager {}. Technician {} not assigned to team.",
                            event.getManagerId(), event.getTechnicianId());
                }
            }
            
            // Update technician profile with additional details
            if (event.getLocalisation() != null) {
                userService.mettreAJourProfil(event.getTechnicianId(),
                    event.getNom(), event.getPrenom(), event.getLocalisation());
                log.info("Updated technician {} with location: {}",
                        event.getTechnicianId(), event.getLocalisation());
            }

            // Add competences if provided
            if (event.getCompetences() != null && !event.getCompetences().isEmpty()) {
                for (TechnicianCreatedEvent.CompetenceInfo comp : event.getCompetences()) {
                    try {
                        NiveauCompetence niveau = convertStringToNiveauCompetence(comp.getNiveau());
                        userService.ajouterCompetence(event.getTechnicianId(),
                            comp.getNom(), comp.getDescription(),
                            comp.getCategorie(), niveau);
                        log.info("Added competence {} to technician {}",
                                comp.getNom(), event.getTechnicianId());
                    } catch (Exception e) {
                        log.warn("Failed to add competence {} to technician {}: {}",
                                comp.getNom(), event.getTechnicianId(), e.getMessage());
                    }
                }
            }
            
            log.info("Successfully processed TechnicianCreatedEvent for technicianId: {}", event.getTechnicianId());
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing TechnicianCreatedEvent for technicianId: {}", event.getTechnicianId(), e);
            
            // Acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }

    /**
     * Convert string level to NiveauCompetence enum
     */
    private NiveauCompetence convertStringToNiveauCompetence(String niveau) {
        if (niveau == null) {
            return NiveauCompetence.JUNIOR;
        }

        switch (niveau.toUpperCase()) {
            case "JUNIOR":
                return NiveauCompetence.JUNIOR;
            case "SENIOR":
                return NiveauCompetence.SENIOR;
            case "EXPERT":
                return NiveauCompetence.EXPERT;
            default:
                log.warn("Unknown competence level: {}, defaulting to JUNIOR", niveau);
                return NiveauCompetence.JUNIOR;
        }
    }
}
