package com.itsm.ticket.presentation.controller;

import com.itsm.ticket.infrastructure.ai.TicketNLPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for NLP-based ticket analysis and recommendations
 * Provides endpoints for category and priority recommendations using AI
 */
@RestController
@RequestMapping("/api/tickets/nlp")
@RequiredArgsConstructor
@Slf4j
public class TicketNLPController {
    
    private final TicketNLPService ticketNLPService;
    
    /**
     * Analyze ticket content and get AI recommendations for category and priority
     */
    @PostMapping("/analyze")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<TicketRecommendationResponse> analyzeTicket(
            @Valid @RequestBody TicketAnalysisRequest request) {
        
        log.info("Analyzing ticket for NLP recommendations: {}", request.getTitre());
        
        try {
            TicketNLPService.TicketRecommendation recommendation = ticketNLPService.analyzeTicket(
                    request.getTitre(), 
                    request.getDescription()
            );
            
            TicketRecommendationResponse response = TicketRecommendationResponse.builder()
                    .recommendedCategory(recommendation.getRecommendedCategory())
                    .recommendedPriority(recommendation.getRecommendedPriority())
                    .confidence(recommendation.getConfidence())
                    .reasoning(recommendation.getReasoning())
                    .detectedKeywords(recommendation.getDetectedKeywords())
                    .alternativeCategories(recommendation.getAlternativeCategories())
                    .availableCategories(java.util.List.of(
                        "SECURITE", "AUDIT", "CONFORMITE", "DEVELOPPEMENT", "DEVOPS", "CLOUD"
                    ))
                    .availablePriorities(java.util.List.of(
                        "BASSE", "NORMALE", "HAUTE", "CRITIQUE"
                    ))
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing ticket: {}", e.getMessage(), e);

            // Return default recommendation instead of error
            TicketRecommendationResponse defaultResponse = TicketRecommendationResponse.builder()
                    .recommendedCategory("DEVELOPPEMENT")
                    .recommendedPriority("NORMALE")
                    .confidence(0.5)
                    .reasoning("Recommandation par défaut (erreur IA): " + e.getMessage())
                    .detectedKeywords(List.of())
                    .alternativeCategories(List.of("DEVOPS"))
                    .availableCategories(List.of("SECURITE", "AUDIT", "CONFORMITE", "DEVELOPPEMENT", "DEVOPS", "CLOUD"))
                    .availablePriorities(List.of("BASSE", "NORMALE", "HAUTE", "CRITIQUE"))
                    .build();

            return ResponseEntity.ok(defaultResponse);
        }
    }
    
    /**
     * Test the main NLP service directly (for debugging)
     */
    @PostMapping("/test-main-service")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<Object> testMainNLPService(@Valid @RequestBody TicketAnalysisRequest request) {

        log.info("Testing main NLP service with: {}", request.getTitre());

        try {
            TicketNLPService.TicketRecommendation recommendation = ticketNLPService.analyzeTicket(
                    request.getTitre(),
                    request.getDescription()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Main NLP service test successful",
                "recommendation", recommendation
            ));

        } catch (Exception e) {
            log.error("Main NLP service test failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Main NLP service test failed: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Simple debug endpoint
     */
    @PostMapping("/debug")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<Object> debugEndpoint(@Valid @RequestBody TicketAnalysisRequest request) {

        log.info("Debug endpoint called with: {}", request.getTitre());

        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Debug endpoint working",
                "titre", request.getTitre(),
                "description", request.getDescription(),
                "nlpEnabled", true,
                "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("Debug endpoint failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Debug endpoint failed: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get available categories and priorities
     */
    @GetMapping("/options")
    @PreAuthorize("hasRole('UTILISATEUR')")
    public ResponseEntity<TicketOptionsResponse> getTicketOptions() {
        
        TicketOptionsResponse response = TicketOptionsResponse.builder()
                .availableCategories(java.util.List.of(
                    "SECURITE", "AUDIT", "CONFORMITE", "DEVELOPPEMENT", "DEVOPS", "CLOUD"
                ))
                .availablePriorities(java.util.List.of(
                    "BASSE", "NORMALE", "HAUTE", "CRITIQUE"
                ))
                .categoryDescriptions(java.util.Map.of(
                    "SECURITE", "Problèmes de sécurité, vulnérabilités, accès non autorisé",
                    "AUDIT", "Audits de sécurité, conformité, vérifications",
                    "CONFORMITE", "Respect des normes, réglementations, politiques",
                    "DEVELOPPEMENT", "Développement d'applications, bugs, nouvelles fonctionnalités",
                    "DEVOPS", "Déploiement, CI/CD, automatisation, infrastructure as code",
                    "CLOUD", "Services cloud, migration, architecture cloud"
                ))
                .priorityDescriptions(java.util.Map.of(
                    "BASSE", "Impact minimal, peut attendre",
                    "NORMALE", "Impact modéré, traitement standard",
                    "HAUTE", "Impact important, traitement prioritaire",
                    "CRITIQUE", "Impact critique, traitement immédiat"
                ))
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Request DTO for ticket analysis
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketAnalysisRequest {
        @jakarta.validation.constraints.NotBlank(message = "Le titre est obligatoire")
        @jakarta.validation.constraints.Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
        private String titre;
        
        @jakarta.validation.constraints.NotBlank(message = "La description est obligatoire")
        @jakarta.validation.constraints.Size(max = 5000, message = "La description ne peut pas dépasser 5000 caractères")
        private String description;
    }
    
    /**
     * Response DTO for ticket recommendations
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketRecommendationResponse {
        private String recommendedCategory;
        private String recommendedPriority;
        private double confidence;
        private String reasoning;
        private java.util.List<String> detectedKeywords;
        private java.util.List<String> alternativeCategories;
        private java.util.List<String> availableCategories;
        private java.util.List<String> availablePriorities;
    }
    
    /**
     * Response DTO for ticket options
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketOptionsResponse {
        private java.util.List<String> availableCategories;
        private java.util.List<String> availablePriorities;
        private java.util.Map<String, String> categoryDescriptions;
        private java.util.Map<String, String> priorityDescriptions;
    }
}
