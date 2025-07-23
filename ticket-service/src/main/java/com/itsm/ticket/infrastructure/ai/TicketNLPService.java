package com.itsm.ticket.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for NLP analysis of ticket descriptions to recommend category and priority
 * Uses Gemini AI to analyze ticket content and suggest appropriate categorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketNLPService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.nlp.gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${app.nlp.gemini.url}")
    private String geminiUrl;
    
    @Value("${app.nlp.enabled:true}")
    private boolean nlpEnabled;
    
    // Available categories
    private static final List<String> AVAILABLE_CATEGORIES = List.of(
        "SECURITE", "AUDIT", "CONFORMITE", "DEVELOPPEMENT", "DEVOPS", "CLOUD"
    );
    
    // Available priorities
    private static final List<String> AVAILABLE_PRIORITIES = List.of(
        "BASSE", "NORMALE", "HAUTE", "CRITIQUE"
    );
    
    /**
     * Analyze ticket description and recommend category and priority
     */
    public TicketRecommendation analyzeTicket(String titre, String description) {
        log.info("Starting NLP analysis for ticket: {}", titre);
        log.debug("NLP enabled: {}, API key configured: {}", nlpEnabled, geminiApiKey != null && !geminiApiKey.trim().isEmpty());

        if (!nlpEnabled) {
            log.info("NLP is disabled, returning default recommendations");
            return createDefaultRecommendation();
        }

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.error("Gemini API key is not configured");
            return createDefaultRecommendation();
        }

        log.info("NLP service configuration OK - API key length: {}, URL: {}", geminiApiKey.length(), geminiUrl);

        try {
            log.info("Building analysis prompt for ticket: {}", titre);
            String prompt = buildAnalysisPrompt(titre, description);
            log.debug("Generated prompt length: {} characters", prompt.length());

            log.info("Calling Gemini API with URL: {}", geminiUrl);
            String geminiResponse = callGeminiAPI(prompt);
            log.info("Received Gemini response, length: {} characters", geminiResponse != null ? geminiResponse.length() : 0);

            log.info("Parsing Gemini response...");
            TicketRecommendation recommendation = parseRecommendationResponse(geminiResponse);
            log.info("Successfully parsed recommendation: category={}, priority={}, confidence={}",
                    recommendation.getRecommendedCategory(),
                    recommendation.getRecommendedPriority(),
                    recommendation.getConfidence());

            return recommendation;

        } catch (Exception e) {
            log.error("Error analyzing ticket with Gemini AI - Title: {}, Error: {}", titre, e.getMessage(), e);
            log.error("Falling back to default recommendation");
            return createDefaultRecommendation();
        }
    }
    
    /**
     * Build prompt for Gemini AI to analyze ticket and recommend category/priority
     */
    private String buildAnalysisPrompt(String titre, String description) {
        return String.format("""
            Analyse ce ticket et choisis la catégorie la plus appropriée:

            CATEGORIES DISPONIBLES:
            - SECURITE: Incidents de sécurité ACTIFS, intrusions détectées, malware, accès non autorisé, pare-feu bloqué, antivirus alertes
            - AUDIT: Évaluations proactives, audits de pénétration, tests de sécurité, rapports d'audit, vérifications de conformité, assessments
            - CONFORMITE: Respect des normes (ISO 27001, GDPR), réglementations, politiques, mise en conformité
            - DEVELOPPEMENT: Développement d'applications, bugs, nouvelles fonctionnalités, code, programmation
            - DEVOPS: CI/CD, déploiement, automatisation, infrastructure as code, pipelines, Jenkins, Docker
            - CLOUD: Services cloud (AWS, Azure, GCP), migration cloud, architecture cloud, conteneurs, Kubernetes

            PRIORITES DISPONIBLES:
            - BASSE: Demandes non urgentes, améliorations
            - NORMALE: Problèmes standards, maintenance
            - HAUTE: Problèmes impactant les utilisateurs
            - CRITIQUE: Systèmes en panne, sécurité compromise

            RÈGLES DE CLASSIFICATION:

            AUDIT vs SECURITE:
            - AUDIT: "audit de pénétration", "évaluation de sécurité", "test proactif", "générer un rapport", "assessment"
            - SECURITE: "intrusion détectée", "alerte de sécurité", "accès non autorisé", "incident en cours", "malware trouvé"
            - Les outils (Kali Linux, Nmap, Metasploit) peuvent être utilisés pour les DEUX catégories

            EXEMPLES:
            - "Réaliser un audit de pénétration avec Nmap" → AUDIT (évaluation proactive)
            - "Intrusion détectée, analyser avec Nmap" → SECURITE (incident réactif)

            DEVOPS vs CLOUD:
            - Si focus sur pipelines, CI/CD, automatisation → DEVOPS
            - Si focus sur services cloud, migration, architecture cloud → CLOUD

            TICKET À ANALYSER:
            Titre: %s
            Description: %s

            Analyse le CONTEXTE et l'OBJECTIF du ticket, pas seulement les outils mentionnés.

            Réponds UNIQUEMENT avec:
            Catégorie: [CATEGORIE]
            Priorité: [PRIORITE]
            """, titre, description);
    }
    
    /**
     * Call Gemini API with the given prompt
     */
    private String callGeminiAPI(String prompt) {
        String url = geminiUrl + "?key=" + geminiApiKey;

        log.info("Calling Gemini API at: {}", geminiUrl);
        log.info("API Key configured: {}", geminiApiKey != null && !geminiApiKey.trim().isEmpty());
        log.debug("API Key length: {}", geminiApiKey != null ? geminiApiKey.length() : 0);
        log.debug("Prompt length: {} characters", prompt.length());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

            log.info("Sending request to Gemini API...");
            ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

            log.info("Gemini API response status: {}", response.getStatusCode());
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                log.info("Response contains keys: {}", responseBody.keySet());
                if (responseBody.containsKey("error")) {
                    log.error("Gemini API returned error: {}", responseBody.get("error"));
                    throw new RuntimeException("Gemini API error: " + responseBody.get("error"));
                }
            }

            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                log.info("Found {} candidates in response", candidates.size());

                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (!parts.isEmpty()) {
                        String responseText = (String) parts.get(0).get("text");
                        log.info("Successfully extracted response text from Gemini API, length: {}", responseText.length());
                        return responseText;
                    }
                }
            }

            log.error("Invalid response format from Gemini API: {}", responseBody);
            throw new RuntimeException("Invalid response format from Gemini API: " + responseBody);

        } catch (RestClientException e) {
            log.error("Gemini API REST error: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Gemini API general error: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse Gemini response and extract recommendation
     */
    private TicketRecommendation parseRecommendationResponse(String response) {
        log.info("Parsing Gemini response: {}", response);

        try {
            // Try simple approach first - look for category directly in response
            String category = extractCategoryFromResponse(response);
            String priority = extractPriorityFromResponse(response);

            log.info("Extracted category: {}, priority: {}", category, priority);

            return TicketRecommendation.builder()
                    .recommendedCategory(category)
                    .recommendedPriority(priority)
                    .confidence(0.8)
                    .reasoning("Analyse par IA Gemini")
                    .detectedKeywords(List.of())
                    .alternativeCategories(List.of())
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            return createDefaultRecommendation();
        }
    }

    /**
     * Extract category from response text
     */
    private String extractCategoryFromResponse(String response) {
        String upperResponse = response.toUpperCase();

        // Check each category
        for (String category : AVAILABLE_CATEGORIES) {
            if (upperResponse.contains(category)) {
                log.debug("Found category {} in response", category);
                return category;
            }
        }

        log.warn("No category found in response, using default");
        return "DEVELOPPEMENT";
    }

    /**
     * Extract priority from response text
     */
    private String extractPriorityFromResponse(String response) {
        String upperResponse = response.toUpperCase();

        // Check each priority
        for (String priority : AVAILABLE_PRIORITIES) {
            if (upperResponse.contains(priority)) {
                log.debug("Found priority {} in response", priority);
                return priority;
            }
        }

        log.warn("No priority found in response, using default");
        return "NORMALE";
    }
    
    /**
     * Extract JSON from Gemini response (removes markdown formatting)
     */
    private String extractJsonFromResponse(String response) {
        // Remove markdown code blocks if present
        Pattern jsonPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = jsonPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // If no markdown, try to find JSON object
        Pattern objectPattern = Pattern.compile("\\{[\\s\\S]*\\}");
        Matcher objectMatcher = objectPattern.matcher(response);
        
        if (objectMatcher.find()) {
            return objectMatcher.group().trim();
        }
        
        return response.trim();
    }
    
    /**
     * Create default recommendation when AI is unavailable
     */
    private TicketRecommendation createDefaultRecommendation() {
        return TicketRecommendation.builder()
                .recommendedCategory("DEVELOPPEMENT")
                .recommendedPriority("NORMALE")
                .confidence(0.5)
                .reasoning("Recommandation par défaut (IA indisponible)")
                .detectedKeywords(List.of())
                .alternativeCategories(List.of("DEVOPS"))
                .build();
    }
    
    /**
     * DTO for ticket recommendation result
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketRecommendation {
        private String recommendedCategory;
        private String recommendedPriority;
        private double confidence;
        private String reasoning;
        private List<String> detectedKeywords;
        private List<String> alternativeCategories;
    }
}
