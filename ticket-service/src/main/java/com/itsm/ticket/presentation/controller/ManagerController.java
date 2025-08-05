package com.itsm.ticket.presentation.controller;

import com.itsm.ticket.application.service.TicketService;
import com.itsm.ticket.application.dto.TeamDashboardDto;
import com.itsm.ticket.application.dto.TechnicianStatsDto;
import com.itsm.ticket.domain.model.StatutTicket;
import com.itsm.ticket.domain.model.Ticket;
import com.itsm.ticket.infrastructure.security.SecurityService;
import com.itsm.ticket.infrastructure.client.UserServiceClient;
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

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for manager ticket operations
 * Handles team ticket management and oversight
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {
    
    private final TicketService ticketService;
    private final SecurityService securityService;
    private final UserServiceClient userServiceClient;
    
    /**
     * Get all tickets for manager's team
     * Fetches tickets where the assigned technician belongs to the same team as the manager
     */
    @GetMapping("/team/tickets")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TicketResponse>> getTeamTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        
        UUID managerId = securityService.getCurrentUserId();
        log.info("Manager {} requesting team tickets", managerId);
        
        try {
            // Create pageable
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get tickets for manager's team
            Page<Ticket> ticketPage = ticketService.getTicketsByManagerTeam(managerId, pageable, status, priority);
            
            List<TicketResponse> response = ticketPage.getContent().stream()
                    .map(this::convertToResponse)
                    .toList();
            
            log.info("Returning {} tickets for manager {}'s team", response.size(), managerId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting team tickets for manager {}: {}", managerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get tickets by status for manager's team
     */
    @GetMapping("/team/tickets/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TicketResponse>> getTeamTicketsByStatus(@PathVariable String status) {
        
        UUID managerId = securityService.getCurrentUserId();
        log.info("Manager {} requesting team tickets with status: {}", managerId, status);
        
        try {
            StatutTicket statutTicket = StatutTicket.valueOf(status.toUpperCase());
            List<Ticket> tickets = ticketService.getTicketsByManagerTeamAndStatus(managerId, statutTicket);
            
            List<TicketResponse> response = tickets.stream()
                    .map(this::convertToResponse)
                    .toList();
            
            log.info("Returning {} tickets with status {} for manager {}'s team", response.size(), status, managerId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting team tickets by status for manager {}: {}", managerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get ticket details (manager can see any ticket from their team)
     */
    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TicketResponse> getTicketDetails(@PathVariable UUID ticketId) {
        
        UUID managerId = securityService.getCurrentUserId();
        log.info("Manager {} requesting ticket details: {}", managerId, ticketId);
        
        try {
            Ticket ticket = ticketService.getTicketByIdForManager(ticketId, managerId);
            TicketResponse response = convertToResponse(ticket);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting ticket {} for manager {}: {}", ticketId, managerId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get team dashboard statistics
     */
    @GetMapping("/team/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TeamDashboardDto> getTeamDashboard() {

        UUID managerId = securityService.getCurrentUserId();
        log.info("Manager {} requesting team dashboard", managerId);

        try {
            TeamDashboardDto dashboard = ticketService.getTeamDashboard(managerId);

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            log.error("Error getting team dashboard for manager {}: {}", managerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Convert Ticket entity to TicketResponse DTO with technician information
     */
    private TicketResponse convertToResponse(Ticket ticket) {
        TicketResponse.TicketResponseBuilder builder = TicketResponse.builder()
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
                .commentaireResolution(ticket.getCommentaireResolution());

        // Fetch technician information if ticket is assigned
        if (ticket.getTechnicienId() != null) {
            try {
                UserServiceClient.TechnicianBasicInfo technicianInfo =
                    userServiceClient.getTechnicianBasicInfo(ticket.getTechnicienId());

                if (technicianInfo != null) {
                    builder.technicienNom(technicianInfo.getNom())
                           .technicienPrenom(technicianInfo.getPrenom())
                           .technicienEmail(technicianInfo.getEmail())
                           .technicienSpecialite(technicianInfo.getSpecialite());
                }
            } catch (Exception e) {
                log.warn("Could not fetch technician info for ticket {}: {}", ticket.getId(), e.getMessage());
                // Continue without technician info - don't fail the whole response
            }
        }

        return builder.build();
    }
    
}
