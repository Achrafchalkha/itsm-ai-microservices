package com.itsm.ticket.presentation.controller;

import com.itsm.ticket.application.service.TicketService;
import com.itsm.ticket.domain.model.PrioriteTicket;
import com.itsm.ticket.domain.model.Ticket;
import com.itsm.ticket.infrastructure.ai.TicketNLPService;
import com.itsm.ticket.infrastructure.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for ticket operations
 * Handles ticket creation, updates, and queries with NLP integration
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {
    
    private final TicketService ticketService;
    private final TicketNLPService ticketNLPService;
    private final SecurityService securityService;
    
    /**
     * Create a new ticket with optional NLP analysis
     * Primarily for UTILISATEUR role, but other roles can also create tickets
     */
    @PostMapping
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<CreateTicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        log.info("Creating ticket: {}", request.getTitre());
        
        try {
            // Get current user ID from security context
            UUID utilisateurId = securityService.getCurrentUserId();
            if (utilisateurId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String finalCategory = request.getCategorie();
            String finalPriority = request.getPriorite();
            TicketNLPService.TicketRecommendation nlpRecommendation = null;
            
            // Apply NLP analysis if enabled and no category/priority provided
            if (request.isEnableNlp() && (request.getCategorie() == null || request.getPriorite() == null)) {
                log.info("Running NLP analysis for ticket: {}", request.getTitre());
                
                nlpRecommendation = ticketNLPService.analyzeTicket(
                    request.getTitre(), 
                    request.getDescription()
                );
                
                // Use NLP recommendations if not provided by user
                if (request.getCategorie() == null) {
                    finalCategory = nlpRecommendation.getRecommendedCategory();
                    log.info("NLP recommended category: {}", finalCategory);
                }
                
                if (request.getPriorite() == null) {
                    finalPriority = nlpRecommendation.getRecommendedPriority();
                    log.info("NLP recommended priority: {}", finalPriority);
                }
            }
            
            // Set defaults if still null
            if (finalCategory == null) {
                finalCategory = "DEVELOPPEMENT";
            }
            if (finalPriority == null) {
                finalPriority = "NORMALE";
            }
            
            // Get user email for assignment-service
            String utilisateurEmail = securityService.getCurrentUserEmail();

            // Create ticket
            Ticket ticket = ticketService.createTicket(
                request.getTitre(),
                request.getDescription(),
                PrioriteTicket.valueOf(finalPriority),
                finalCategory,
                utilisateurId,
                utilisateurEmail,
                request.isEnableNlp(),
                request.getFichiersAttaches()
            );
            
            // Build response
            CreateTicketResponse response = CreateTicketResponse.builder()
                    .ticketId(ticket.getId())
                    .titre(ticket.getTitre())
                    .description(ticket.getDescription())
                    .statut(ticket.getStatut().name())
                    .priorite(ticket.getPriorite().name())
                    .categorie(ticket.getCategorie())
                    .utilisateurId(ticket.getUtilisateurId())
                    .enableNlp(ticket.getEnableNlp())
                    .dateCreation(ticket.getDateCreation())
                    .nlpRecommendation(nlpRecommendation)
                    .build();
            
            log.info("Successfully created ticket: {}", ticket.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating ticket: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get ticket by ID
     */
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable UUID ticketId) {
        log.debug("Getting ticket: {}", ticketId);
        
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            
            TicketResponse response = TicketResponse.builder()
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
            log.error("Error getting ticket {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Test endpoint to verify authentication
     */
    @GetMapping("/auth-test")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<AuthTestResponse> testAuth() {
        try {
            UUID userId = securityService.getCurrentUserId();
            String email = securityService.getCurrentUserEmail();
            String role = securityService.getCurrentUserRole();

            AuthTestResponse response = AuthTestResponse.builder()
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .authenticated(userId != null)
                    .message("Authentication successful")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Auth test failed: {}", e.getMessage(), e);
            AuthTestResponse response = AuthTestResponse.builder()
                    .authenticated(false)
                    .message("Authentication failed: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Get tickets for current user
     */
    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        try {
            UUID utilisateurId = securityService.getCurrentUserId();
            if (utilisateurId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Ticket> tickets = ticketService.getTicketsByUser(utilisateurId);
            
            List<TicketResponse> response = tickets.stream()
                    .map(ticket -> TicketResponse.builder()
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
                            .fichiersAttaches(ticket.getFichiersAttaches())
                            .build())
                    .toList();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting user tickets: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get tickets by user ID (for admin/manager access)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<List<TicketResponse>> getTicketsByUser(@PathVariable UUID userId) {
        try {
            log.debug("Getting tickets for user: {}", userId);

            List<Ticket> tickets = ticketService.getTicketsByUser(userId);

            List<TicketResponse> response = tickets.stream()
                    .map(ticket -> TicketResponse.builder()
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
                            .fichiersAttaches(ticket.getFichiersAttaches())
                            .build())
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting tickets for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download file attachment
     */
    @GetMapping("/files/{fileName}")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String fileName) {
        try {
            log.debug("Downloading file: {}", fileName);

            // Get current user ID and determine user role
            UUID currentUserId = securityService.getCurrentUserId();
            if (currentUserId == null) {
                log.error("No current user found for file download");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Get tickets based on user role
            List<Ticket> accessibleTickets;
            if (securityService.hasRole("TECHNICIEN")) {
                // For technicians, get tickets assigned to them
                accessibleTickets = ticketService.getTicketsByTechnician(currentUserId);
                log.debug("Technician {} accessing files from {} assigned tickets", currentUserId, accessibleTickets.size());
            } else {
                // For regular users, get their own tickets
                accessibleTickets = ticketService.getTicketsByUser(currentUserId);
                log.debug("User {} accessing files from {} owned tickets", currentUserId, accessibleTickets.size());
            }

            for (Ticket ticket : accessibleTickets) {
                if (ticket.getFichiersAttaches() != null && !ticket.getFichiersAttaches().trim().isEmpty()) {
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

                                if (fileName.equals(storedFileName)) {
                                    // Found the file, create the full path
                                    java.nio.file.Path fullFilePath = java.nio.file.Paths.get(filePath);
                                    log.debug("Found file at path: {}", fullFilePath.toAbsolutePath());

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

                                        return ResponseEntity.ok()
                                                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                                                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                                       "attachment; filename=\"" + downloadName + "\"")
                                                .body(resource);
                                    } else {
                                        log.error("File exists in database but not on disk: {}", fullFilePath);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing attached files for ticket {}: {}", ticket.getId(), e.getMessage());
                    }
                }
            }

            log.error("File not found or access denied: {}", fileName);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error downloading file {}: {}", fileName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete ticket (soft delete - only ticket owner can delete)
     */
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID ticketId) {
        try {
            log.debug("Deleting ticket: {}", ticketId);

            // Get current user
            UUID currentUserId = securityService.getCurrentUserId();
            if (currentUserId == null) {
                log.error("No current user found for ticket deletion");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Get the ticket to verify ownership
            Ticket ticket = ticketService.getTicketById(ticketId);

            // Check if current user is the owner of the ticket
            if (!ticket.getUtilisateurId().equals(currentUserId)) {
                log.error("User {} attempted to delete ticket {} owned by {}",
                         currentUserId, ticketId, ticket.getUtilisateurId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Allow deletion regardless of status
            log.info("Deleting ticket {} with status {} for user {}", ticketId, ticket.getStatut(), currentUserId);

            // Perform soft delete
            ticketService.deleteTicket(ticketId);

            log.info("Ticket {} deleted successfully by user {}", ticketId, currentUserId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting ticket {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request DTO for ticket creation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateTicketRequest {
        @jakarta.validation.constraints.NotBlank(message = "Le titre est obligatoire")
        @jakarta.validation.constraints.Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
        private String titre;
        
        @jakarta.validation.constraints.NotBlank(message = "La description est obligatoire")
        @jakarta.validation.constraints.Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
        private String description;
        
        private String categorie; // Optional - will use NLP if not provided
        private String priorite;  // Optional - will use NLP if not provided
        
        @lombok.Builder.Default
        private boolean enableNlp = true; // Enable NLP by default

        private String fichiersAttaches; // JSON string of attached files
    }
    
    /**
     * Response DTO for ticket creation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateTicketResponse {
        private UUID ticketId;
        private String titre;
        private String description;
        private String statut;
        private String priorite;
        private String categorie;
        private UUID utilisateurId;
        private boolean enableNlp;
        private LocalDateTime dateCreation;
        private TicketNLPService.TicketRecommendation nlpRecommendation;
    }
    
    /**
     * Response DTO for ticket information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketResponse {
        private UUID id;
        private String titre;
        private String description;
        private String statut;
        private String priorite;
        private String categorie;
        private UUID utilisateurId;
        private UUID technicienId;
        private UUID teamId;
        private boolean enableNlp;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private String fichiersAttaches;
    }

    /**
     * Response DTO for authentication test
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthTestResponse {
        private UUID userId;
        private String email;
        private String role;
        private boolean authenticated;
        private String message;
    }
}
