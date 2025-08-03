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
     * Start working on a ticket (change status from EN_COURS to OUVERT)
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
                .fichiersAttaches(ticket.getFichiersAttaches())
                .tempsPremiereReponseMinutes(ticket.getTempsPremiereReponseMinutes())
                .nombreReassignations(ticket.getNombreReassignations())
                .enableNlp(ticket.getEnableNlp())
                .actif(ticket.getActif())
                .commentaireResolution(ticket.getCommentaireResolution())
                .statutSla(ticket.getStatutSla() != null ? ticket.getStatutSla().name() : null)
                .build();
    }

    /**
     * Download file attachment for technician from a specific ticket
     */
    @GetMapping("/tickets/{ticketId}/files/{fileName}")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFileForTechnician(
            @PathVariable UUID ticketId,
            @PathVariable String fileName) {
        try {
            log.debug("Technician downloading file: {} from ticket: {}", fileName, ticketId);

            // Get current technician ID
            UUID technicianId = securityService.getCurrentUserId();
            if (technicianId == null) {
                log.error("No current technician found for file download");
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
            }

            // Get the specific ticket and verify technician is assigned
            Ticket ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                log.error("Ticket not found: {}", ticketId);
                return ResponseEntity.notFound().build();
            }

            // Verify that the current technician is assigned to this ticket
            if (ticket.getTechnicienId() == null || !ticket.getTechnicienId().equals(technicianId)) {
                log.error("Technician {} not assigned to ticket {}", technicianId, ticketId);
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }

            // Check if ticket has attached files
            if (ticket.getFichiersAttaches() == null || ticket.getFichiersAttaches().trim().isEmpty()) {
                log.error("No files attached to ticket {}", ticketId);
                return ResponseEntity.notFound().build();
            }

            try {
                // Parse the attached files JSON
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<java.util.Map<String, Object>> files = mapper.readValue(
                    ticket.getFichiersAttaches(),
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
                );

                // Look for the requested file
                for (java.util.Map<String, Object> fileInfo : files) {
                    String filePath = (String) fileInfo.get("filePath");
                    String storedFileName = (String) fileInfo.get("fileName");

                    if (filePath != null && storedFileName != null) {
                        // Compare with the fileName stored in database, not the physical filename
                        log.debug("Comparing requested file '{}' with stored fileName '{}'", fileName, storedFileName);

                        if (storedFileName.equals(fileName)) {
                            // File found, prepare for download
                            java.nio.file.Path fullFilePath = java.nio.file.Paths.get("uploads").resolve(filePath);

                            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(fullFilePath.toUri());

                            if (resource.exists() && resource.isReadable()) {
                                // Determine content type
                                String contentType = java.nio.file.Files.probeContentType(fullFilePath);
                                if (contentType == null) {
                                    contentType = "application/octet-stream";
                                }

                                // Use original name for download
                                String originalName = (String) fileInfo.get("originalName");
                                String downloadName = originalName != null ? originalName : fileName;

                                log.info("Technician {} downloading file: {} (original: {}) from ticket: {}",
                                        technicianId, fileName, downloadName, ticketId);

                                return ResponseEntity.ok()
                                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                               "attachment; filename=\"" + downloadName + "\"")
                                        .body(resource);
                            } else {
                                log.error("File exists in database but not on disk: {}", fullFilePath);
                                return ResponseEntity.notFound().build();
                            }
                        }
                    }
                }

                log.error("File {} not found in ticket {}", fileName, ticketId);
                return ResponseEntity.notFound().build();

            } catch (Exception e) {
                log.error("Error parsing attached files for ticket {}: {}", ticketId, e.getMessage());
                return ResponseEntity.internalServerError().build();
            }

        } catch (Exception e) {
            log.error("Error downloading file {} from ticket {} for technician: {}", fileName, ticketId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
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
