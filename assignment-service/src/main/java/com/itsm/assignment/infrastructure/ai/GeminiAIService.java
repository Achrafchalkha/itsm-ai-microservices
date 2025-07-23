package com.itsm.assignment.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.assignment.domain.model.NLPAnalysisResult;
import com.itsm.assignment.infrastructure.ai.dto.GeminiRequest;
import com.itsm.assignment.infrastructure.ai.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for integrating with Gemini AI for intelligent ticket assignment
 * Analyzes ticket descriptions and matches with technician competences
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAIService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${app.nlp.gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${app.nlp.gemini.url}")
    private String geminiUrl;
    
    @Value("${app.nlp.enabled:true}")
    private boolean nlpEnabled;
    
    /**
     * Analyze ticket description and match with technician competences
     */
    public NLPAnalysisResult analyzeTicketForAssignment(String ticketDescription, 
                                                       String ticketCategory,
                                                       List<TechnicianCompetence> availableTechnicians) {
        
        if (!nlpEnabled) {
            log.info("NLP is disabled, returning default analysis");
            return createDefaultAnalysis(ticketCategory, availableTechnicians);
        }
        
        try {
            String prompt = buildAssignmentPrompt(ticketDescription, ticketCategory, availableTechnicians);
            String geminiResponse = callGeminiAPI(prompt);
            return parseAssignmentResponse(geminiResponse, availableTechnicians);
            
        } catch (Exception e) {
            log.error("Error analyzing ticket with Gemini AI: {}", e.getMessage(), e);
            return createDefaultAnalysis(ticketCategory, availableTechnicians);
        }
    }
    
    /**
     * Build prompt for Gemini AI to analyze ticket and recommend technician
     */
    private String buildAssignmentPrompt(String description, String category,
                                       List<TechnicianCompetence> technicians) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Tu es un expert en assignation de tickets ITSM. ");
        prompt.append("Analyse la description du ticket et trouve le meilleur technicien selon ces critères:\n\n");
        prompt.append("PRIORITÉ D'ANALYSE:\n");
        prompt.append("1. CORRESPONDANCE EXACTE: Privilégier les compétences dont le NOM correspond exactement aux technologies mentionnées\n");
        prompt.append("2. CORRESPONDANCE DESCRIPTION: Analyser les descriptions des compétences pour trouver les plus pertinentes\n");
        prompt.append("3. NIVEAU DE COMPÉTENCE: Considérer le niveau (JUNIOR=1, INTERMEDIAIRE=2, AVANCE=3, SENIOR=4, EXPERT=5)\n");
        prompt.append("4. CHARGE DE TRAVAIL: Préférer les techniciens moins chargés\n\n");
        prompt.append("RÈGLES DE SCORING:\n");
        prompt.append("- Correspondance exacte du nom de compétence = +0.4 points\n");
        prompt.append("- Correspondance de description = +0.3 points\n");
        prompt.append("- Niveau de compétence élevé = +0.2 points\n");
        prompt.append("- Charge faible = +0.1 points\n\n");

        prompt.append("TICKET À ANALYSER:\n");
        prompt.append("Catégorie: ").append(category).append("\n");
        prompt.append("Description: ").append(description).append("\n\n");

        prompt.append("TECHNICIENS DISPONIBLES:\n");
        for (TechnicianCompetence tech : technicians) {
            prompt.append("ID: ").append(tech.getTechnicianId()).append("\n");
            prompt.append("Nom: ").append(tech.getName()).append("\n");
            prompt.append("Spécialité: ").append(tech.getSpecialite()).append("\n");
            prompt.append("Charge actuelle: ").append(tech.getCurrentWorkload()).append(" tickets\n");
            prompt.append("Compétences détaillées: ").append(tech.getCompetencesDescription()).append("\n");
            prompt.append("---\n");
        }
        
        prompt.append("\nINSTRUCTIONS D'ANALYSE DÉTAILLÉES:\n");
        prompt.append("1. EXTRACTION DES TECHNOLOGIES:\n");
        prompt.append("   - Identifie TOUS les outils/technologies mentionnés (Jenkins, Docker, Maven, npm, SonarQube, etc.)\n");
        prompt.append("   - Note les actions spécifiques (pipeline, build, test, déploiement)\n\n");
        prompt.append("2. CORRESPONDANCE EXACTE (PRIORITÉ MAXIMALE):\n");
        prompt.append("   - Si un technicien a une compétence nommée exactement comme l'outil principal → Score élevé\n");
        prompt.append("   - Exemple: Ticket mentionne 'Jenkins' → Technicien avec compétence 'Jenkins' = priorité\n\n");
        prompt.append("3. CORRESPONDANCE DESCRIPTION:\n");
        prompt.append("   - Analyser les descriptions des compétences pour trouver les plus pertinentes\n");
        prompt.append("   - Exemple: 'Configuration de pipelines' correspond à un ticket de pipeline\n\n");
        prompt.append("4. CALCUL DU SCORE FINAL:\n");
        prompt.append("   - Correspondance exacte du nom: 40% du score\n");
        prompt.append("   - Pertinence de la description: 30% du score\n");
        prompt.append("   - Niveau de compétence: 20% du score\n");
        prompt.append("   - Disponibilité (charge faible): 10% du score\n\n");

        prompt.append("EXEMPLE DE RAISONNEMENT:\n");
        prompt.append("Ticket: 'Pipeline Jenkins avec Docker'\n");
        prompt.append("Technicien A: Compétence 'Jenkins' (AVANCE) → Score: 0.9 (correspondance exacte)\n");
        prompt.append("Technicien B: Compétence 'Ansible' (SENIOR) → Score: 0.3 (pas de correspondance directe)\n");
        prompt.append("Résultat: Choisir Technicien A malgré niveau inférieur car correspondance exacte\n\n");

        prompt.append("Réponds UNIQUEMENT au format JSON suivant:\n");
        prompt.append("{\n");
        prompt.append("  \"detectedTechnologies\": [\"Jenkins\", \"Docker\", \"Maven\", \"npm\"],\n");
        prompt.append("  \"requiredCompetences\": [\"Jenkins\", \"CI/CD\", \"Pipeline\"],\n");
        prompt.append("  \"complexityLevel\": \"LOW|MEDIUM|HIGH\",\n");
        prompt.append("  \"urgencyLevel\": \"LOW|MEDIUM|HIGH\",\n");
        prompt.append("  \"keywords\": [\"pipeline\", \"intégration continue\"],\n");
        prompt.append("  \"recommendedTechnicianId\": \"uuid du technicien avec la meilleure correspondance\",\n");
        prompt.append("  \"overallConfidence\": 0.85,\n");
        prompt.append("  \"reasoning\": \"Technicien choisi car compétence 'Jenkins' correspond exactement à l'outil principal du ticket. Correspondance exacte prioritaire sur niveau supérieur.\",\n");
        prompt.append("  \"technicianScores\": {\n");
        prompt.append("    \"uuid-jenkins-tech\": 0.9,\n");
        prompt.append("    \"uuid-ansible-tech\": 0.3\n");
        prompt.append("  }\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    /**
     * Call Gemini API with the constructed prompt
     */
    private String callGeminiAPI(String prompt) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(geminiUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            GeminiRequest request = GeminiRequest.createAssignmentAnalysisRequest(prompt);
            
            String response = webClient.post()
                    .uri("?key=" + geminiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("Gemini API response: {}", response);
            
            // Parse response to extract generated text
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            return geminiResponse.getGeneratedText();
            
        } catch (WebClientResponseException e) {
            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API call failed", e);
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API call failed", e);
        }
    }
    
    /**
     * Parse Gemini response and create NLPAnalysisResult
     */
    private NLPAnalysisResult parseAssignmentResponse(String response, 
                                                    List<TechnicianCompetence> technicians) {
        try {
            // Extract JSON from response (in case there's extra text)
            String jsonResponse = extractJsonFromResponse(response);
            
            // Parse JSON response
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            
            return NLPAnalysisResult.builder()
                    .detectedTechnologies((List<String>) responseMap.get("detectedTechnologies"))
                    .requiredCompetences((List<String>) responseMap.get("requiredCompetences"))
                    .complexityLevel((String) responseMap.get("complexityLevel"))
                    .urgencyLevel((String) responseMap.get("urgencyLevel"))
                    .keywords((List<String>) responseMap.get("keywords"))
                    .recommendedTechnicianId(parseUUID((String) responseMap.get("recommendedTechnicianId")))
                    .overallConfidence(BigDecimal.valueOf(((Number) responseMap.get("overallConfidence")).doubleValue()))
                    .reasoning((String) responseMap.get("reasoning"))
                    .technicianScores(parseTechnicianScores((Map<String, Object>) responseMap.get("technicianScores")))
                    .rawGeminiResponse(response)
                    .analyzedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            return createDefaultAnalysis("UNKNOWN", technicians);
        }
    }
    
    /**
     * Extract JSON from Gemini response (removes markdown formatting if present)
     */
    private String extractJsonFromResponse(String response) {
        // Look for JSON content between ```json and ``` or just find { ... }
        Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // If no JSON found, return original response
        return response;
    }
    
    /**
     * Parse technician scores from response
     */
    private Map<UUID, BigDecimal> parseTechnicianScores(Map<String, Object> scoresMap) {
        if (scoresMap == null) return new HashMap<>();

        Map<UUID, BigDecimal> scores = new HashMap<>();
        for (Map.Entry<String, Object> entry : scoresMap.entrySet()) {
            try {
                UUID techId = UUID.fromString(entry.getKey());
                BigDecimal score = BigDecimal.valueOf(((Number) entry.getValue()).doubleValue());
                scores.put(techId, score);
            } catch (Exception e) {
                log.warn("Error parsing technician score: {} = {}", entry.getKey(), entry.getValue());
            }
        }
        return scores;
    }
    
    /**
     * Parse UUID from string, return null if invalid
     */
    private UUID parseUUID(String uuidString) {
        try {
            return uuidString != null ? UUID.fromString(uuidString) : null;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", uuidString);
            return null;
        }
    }
    
    /**
     * Create default analysis when Gemini is unavailable or disabled
     */
    private NLPAnalysisResult createDefaultAnalysis(String category, 
                                                  List<TechnicianCompetence> technicians) {
        
        // Simple fallback: recommend technician with lowest workload
        TechnicianCompetence recommended = technicians.stream()
                .min(Comparator.comparing(TechnicianCompetence::getCurrentWorkload))
                .orElse(null);
        
        Map<UUID, BigDecimal> scores = new HashMap<>();
        if (recommended != null) {
            scores.put(recommended.getTechnicianId(), BigDecimal.valueOf(0.5)); // Default confidence
        }
        
        return NLPAnalysisResult.builder()
                .detectedTechnologies(List.of())
                .requiredCompetences(List.of(category))
                .complexityLevel("MEDIUM")
                .urgencyLevel("MEDIUM")
                .keywords(List.of())
                .recommendedTechnicianId(recommended != null ? recommended.getTechnicianId() : null)
                .overallConfidence(BigDecimal.valueOf(0.5))
                .reasoning("Fallback assignment based on workload (Gemini unavailable)")
                .technicianScores(scores)
                .rawGeminiResponse("DEFAULT_ANALYSIS")
                .analyzedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * DTO for technician competence information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnicianCompetence {
        private UUID technicianId;
        private String name;
        private String specialite;
        private Integer currentWorkload;
        private String competencesDescription;
    }
}
