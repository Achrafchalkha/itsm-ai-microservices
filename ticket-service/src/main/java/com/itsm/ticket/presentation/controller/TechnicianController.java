package com.itsm.ticket.presentation.controller;

import com.itsm.ticket.application.service.TicketService;
import com.itsm.ticket.domain.model.StatutTicket;
import com.itsm.ticket.domain.model.Ticket;
import com.itsm.ticket.infrastructure.security.SecurityService;
import com.itsm.ticket.presentation.dto.TicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for technician ticket operations
 * Handles technician dashboard, ticket resolution, and status updates
 */
@RestController
@RequestMapping("/api/technician")
@RequiredArgsConstructor
@Slf4j
public class TechnicianController {
    
    private final TicketService ticketService;
    private final SecurityService securityService;
    
    /**
     * Get all tickets assigned to the current technician
     */
    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<List<TicketResponse>> getMyAssignedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "assignedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} requesting assigned tickets", technicianId);

        // Debug: Also log the current user details
        log.debug("Current user email: {}", securityService.getCurrentUserEmail());
        log.debug("Current user role: {}", securityService.getCurrentUserRole());
        
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            List<Ticket> tickets = ticketService.getTicketsByTechnician(technicianId);
            
            List<TicketResponse> response = tickets.stream()
                    .map(this::convertToResponse)
                    .toList();
            
            log.debug("Found {} assigned tickets for technician {}", response.size(), technicianId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting assigned tickets for technician {}: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get tickets by status for current technician
     */
    @GetMapping("/my-tickets/status/{status}")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<List<TicketResponse>> getMyTicketsByStatus(@PathVariable String status) {
        
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} requesting tickets with status: {}", technicianId, status);
        
        try {
            StatutTicket statutTicket = StatutTicket.valueOf(status.toUpperCase());
            List<Ticket> tickets = ticketService.getTicketsByTechnicianAndStatus(technicianId, statutTicket);
            
            List<TicketResponse> response = tickets.stream()
                    .map(this::convertToResponse)
                    .toList();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status requested: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting tickets by status for technician {}: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Start working on a ticket (change status to OUVERT)
     */
    @PutMapping("/tickets/{ticketId}/start")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<TicketResponse> startWorkingOnTicket(@PathVariable UUID ticketId) {

        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} starting work on ticket {}", technicianId, ticketId);
        
        try {
            Ticket ticket = ticketService.startWorkingOnTicket(ticketId, technicianId);
            TicketResponse response = convertToResponse(ticket);
            
            log.info("Ticket {} status changed to EN_COURS by technician {}", ticketId, technicianId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error starting work on ticket {} by technician {}: {}", ticketId, technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add a work note/comment to a ticket
     */
    @PostMapping("/tickets/{ticketId}/notes")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<Void> addWorkNote(
            @PathVariable UUID ticketId,
            @Valid @RequestBody AddWorkNoteRequest request) {
        
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} adding work note to ticket {}", technicianId, ticketId);
        
        try {
            ticketService.addWorkNote(ticketId, technicianId, request.getNote());
            
            log.info("Work note added to ticket {} by technician {}", ticketId, technicianId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error adding work note to ticket {} by technician {}: {}", ticketId, technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Resolve a ticket with solution
     */
    @PutMapping("/tickets/{ticketId}/resolve")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<TicketResponse> resolveTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody ResolveTicketRequest request) {
        
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} resolving ticket {} with solution: {}", 
                technicianId, ticketId, request.getSolution().substring(0, Math.min(50, request.getSolution().length())));
        
        try {
            Ticket ticket = ticketService.resolveTicket(ticketId, technicianId, request.getSolution());
            TicketResponse response = convertToResponse(ticket);
            
            log.info("Ticket {} resolved by technician {}", ticketId, technicianId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error resolving ticket {} by technician {}: {}", ticketId, technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Request reassignment of a ticket
     */
    @PutMapping("/tickets/{ticketId}/request-reassignment")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<Void> requestReassignment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody RequestReassignmentRequest request) {
        
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} requesting reassignment of ticket {}: {}", 
                technicianId, ticketId, request.getReason());
        
        try {
            ticketService.requestReassignment(ticketId, technicianId, request.getReason());
            
            log.info("Reassignment requested for ticket {} by technician {}", ticketId, technicianId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error requesting reassignment for ticket {} by technician {}: {}", 
                    ticketId, technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Debug endpoint to check ticket assignments
     */
    @GetMapping("/debug/all-tickets")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<String> debugAllTickets() {

        UUID technicianId = securityService.getCurrentUserId();
        log.info("DEBUG: Current technician ID: {}", technicianId);

        try {
            // Get ALL tickets to see assignment status
            List<Ticket> allTickets = ticketService.getAllTickets(); // We need to create this method

            StringBuilder debug = new StringBuilder();
            debug.append("Current Technician ID: ").append(technicianId).append("\n\n");
            debug.append("All Tickets in Database:\n");

            for (Ticket ticket : allTickets) {
                debug.append("Ticket ID: ").append(ticket.getId())
                     .append(", Title: ").append(ticket.getTitre())
                     .append(", Assigned to: ").append(ticket.getTechnicienId())
                     .append(", Created by: ").append(ticket.getUtilisateurId())
                     .append(", Status: ").append(ticket.getStatut())
                     .append("\n");
            }

            return ResponseEntity.ok(debug.toString());

        } catch (Exception e) {
            log.error("Error in debug endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get technician dashboard summary
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<TechnicianDashboardResponse> getDashboard() {
        
        UUID technicianId = securityService.getCurrentUserId();
        log.debug("Getting dashboard for technician {}", technicianId);
        
        try {
            TechnicianDashboardResponse dashboard = ticketService.getTechnicianDashboard(technicianId);
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error getting dashboard for technician {}: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Convert Ticket to TicketResponse
     */
    private TicketResponse convertToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .titre(ticket.getTitre())
                .description(ticket.getDescription())
                .statut(ticket.getStatut().name())
                .priorite(ticket.getPriorite().name())
                .categorie(ticket.getCategorie())
                .utilisateurId(ticket.getUtilisateurId())
                .technicienId(ticket.getTechnicienId())
                .teamId(ticket.getTeamId())
                .dateCreation(ticket.getDateCreation())
                .dateModification(ticket.getDateModification())
                .dateFermeture(ticket.getDateFermeture())
                .dateLimiteSla(ticket.getDateLimiteSla())
                .datePremiereReponse(ticket.getDatePremiereReponse())
                .slaRespecte(ticket.getSlaRespecte())
                .tempsResolutionMinutes(ticket.getTempsResolutionMinutes())
                .tempsPremiereReponseMinutes(ticket.getTempsPremiereReponseMinutes())
                .nombreReassignations(ticket.getNombreReassignations())
                .enableNlp(ticket.getEnableNlp())
                .actif(ticket.getActif())
                .commentaireResolution(ticket.getCommentaireResolution())
                .statutSla(ticket.getStatutSla() != null ? ticket.getStatutSla().name() : null)
                .build();
    }
    
    // DTO Classes
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AddWorkNoteRequest {
        private String note;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResolveTicketRequest {
        private String solution;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RequestReassignmentRequest {
        private String reason;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnicianDashboardResponse {
        private int totalAssignedTickets;
        private int openTickets;
        private int inProgressTickets;
        private int resolvedToday;
        private int overdueTickets;
        private double averageResolutionTime;
        private int urgentTicketsCount;
        private int newTicketsCount;
    }
}
