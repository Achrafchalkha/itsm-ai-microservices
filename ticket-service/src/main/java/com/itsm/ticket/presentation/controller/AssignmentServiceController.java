package com.itsm.ticket.presentation.controller;

import com.itsm.ticket.application.service.TicketService;
import com.itsm.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public endpoints for assignment-service integration
 * These endpoints don't require JWT authentication for service-to-service communication
 */
@RestController
@RequestMapping("/api/public/assignment")
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceController {
    
    private final TicketService ticketService;
    
    /**
     * Update ticket assignment (technician and team)
     * Used by assignment-service after successful assignment
     */
    @PutMapping("/tickets/{ticketId}/assignment")
    public ResponseEntity<Void> updateTicketAssignment(
            @PathVariable UUID ticketId,
            @RequestParam UUID technicianId,
            @RequestParam UUID teamId) {
        
        log.info("Assignment-service updating ticket {} assignment: technician={}, team={}", 
                ticketId, technicianId, teamId);
        
        try {
            ticketService.updateAssignment(ticketId, technicianId, teamId);
            log.info("Successfully updated assignment for ticket: {}", ticketId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error updating assignment for ticket {} by assignment-service: {}", 
                    ticketId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get ticket details for assignment-service
     */
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDetailsDTO> getTicketDetails(@PathVariable UUID ticketId) {
        log.info("Assignment-service requesting ticket details: {}", ticketId);
        
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            
            TicketDetailsDTO response = TicketDetailsDTO.builder()
                    .id(ticket.getId())
                    .titre(ticket.getTitre())
                    .description(ticket.getDescription())
                    .statut(ticket.getStatut().name())
                    .priorite(ticket.getPriorite().name())
                    .categorie(ticket.getCategorie())
                    .utilisateurId(ticket.getUtilisateurId())
                    .technicienId(ticket.getTechnicienId())
                    .teamId(ticket.getTeamId())
                    .enableNlp(ticket.getEnableNlp())
                    .dateCreation(ticket.getDateCreation())
                    .dateModification(ticket.getDateModification())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving ticket {} for assignment-service: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Health check endpoint for assignment-service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Ticket-Service assignment endpoints are healthy");
    }
    
    /**
     * DTO for ticket details response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketDetailsDTO {
        private UUID id;
        private String titre;
        private String description;
        private String statut;
        private String priorite;
        private String categorie;
        private UUID utilisateurId;
        private UUID technicienId;
        private UUID teamId;
        private Boolean enableNlp;
        private java.time.LocalDateTime dateCreation;
        private java.time.LocalDateTime dateModification;
    }
}
