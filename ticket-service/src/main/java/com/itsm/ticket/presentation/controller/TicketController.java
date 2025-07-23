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
                request.isEnableNlp()
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
                            .build())
                    .toList();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting user tickets: {}", e.getMessage(), e);
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
