package com.itsm.assignment.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.assignment.domain.model.Assignment;
import com.itsm.assignment.domain.model.AssignmentStrategy;
import com.itsm.assignment.domain.model.NLPAnalysisResult;
import com.itsm.assignment.infrastructure.ai.GeminiAIService;
import com.itsm.assignment.infrastructure.client.TicketServiceClient;
import com.itsm.assignment.infrastructure.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core assignment engine that implements intelligent ticket assignment logic
 * Combines team filtering, NLP analysis, and multiple assignment strategies
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentEngine {
    
    private final UserServiceClient userServiceClient;
    private final TicketServiceClient ticketServiceClient;
    private final GeminiAIService geminiAIService;
    private final ObjectMapper objectMapper;
    
    @Value("${assignment.default.strategy:HYBRID}")
    private String defaultStrategy;
    
    @Value("${assignment.workload.max-per-technician:5}")
    private int maxWorkloadPerTechnician;
    
    @Value("${assignment.confidence.threshold:0.6}")
    private double confidenceThreshold;
    
    /**
     * Main assignment method - assigns ticket to best available technician
     */
    public AssignmentResult assignTicket(UUID ticketId, String ticketDescription, 
                                       String ticketCategory, String ticketPriority, 
                                       Boolean enableNlp) {
        
        log.info("Starting DIRECT technician assignment for ticket {} with category {}", ticketId, ticketCategory);

        try {
            // Step 1: Get ALL active technicians (bypass team selection)
            List<UserServiceClient.TechnicianDTO> allTechnicians =
                    userServiceClient.getAllActiveTechnicians();

            if (allTechnicians.isEmpty()) {
                log.warn("No active technicians found in the system");
                return AssignmentResult.failed("No active technicians available");
            }

            // Step 2: Filter by competence category match
            List<UserServiceClient.TechnicianDTO> competentTechnicians = allTechnicians.stream()
                    .filter(tech -> hasCompetenceInCategory(tech, ticketCategory))
                    .toList();

            if (competentTechnicians.isEmpty()) {
                log.warn("No technicians found with competences in category: {}", ticketCategory);
                return AssignmentResult.waitingForTechnician(ticketCategory,
                    "No technicians with competences in category: " + ticketCategory);
            }

            log.info("Found {} technicians with competences in category {}: {}",
                    competentTechnicians.size(), ticketCategory,
                    competentTechnicians.stream().map(t -> t.getNom() + " " + t.getPrenom()).toList());

            // Step 3: Filter by workload availability
            List<UserServiceClient.TechnicianDTO> availableTechnicians = competentTechnicians.stream()
                    .filter(tech -> tech.getChargeActuelle() < maxWorkloadPerTechnician)
                    .toList();

            if (availableTechnicians.isEmpty()) {
                log.warn("All competent technicians are at maximum workload for category: {}", ticketCategory);
                return AssignmentResult.waitingForTechnician(ticketCategory,
                    "All competent technicians are at maximum workload");
            }
            
            // Step 3: NLP Analysis (if enabled)
            NLPAnalysisResult nlpResult = null;
            if (Boolean.TRUE.equals(enableNlp)) {
                List<GeminiAIService.TechnicianCompetence> techCompetences = 
                        convertToTechnicianCompetences(availableTechnicians);
                nlpResult = geminiAIService.analyzeTicketForAssignment(
                        ticketDescription, ticketCategory, techCompetences);
            }
            
            // Step 4: Apply assignment strategy
            AssignmentStrategy strategy = AssignmentStrategy.valueOf(defaultStrategy);
            TechnicianScore bestMatch = selectBestTechnician(availableTechnicians, nlpResult, strategy, ticketCategory);
            
            if (bestMatch == null) {
                return AssignmentResult.failed("No suitable technician found");
            }
            
            // Step 5: Create assignment
            UserServiceClient.TechnicianDTO selectedTechnician = bestMatch.getTechnician();
            String nlpAnalysisJson = nlpResult != null ? serializeNlpResult(nlpResult) : null;
            
            Assignment assignment = Assignment.createAssignment(
                    ticketId,
                    selectedTechnician.getId(),
                    selectedTechnician.getTeamId(),
                    strategy,
                    bestMatch.getScore(),
                    bestMatch.getReason(),
                    nlpAnalysisJson
            );
            
            log.info("Assigned ticket {} to technician {} with confidence {}", 
                    ticketId, selectedTechnician.getId(), bestMatch.getScore());
            
            return AssignmentResult.success(assignment, selectedTechnician, nlpResult);
            
        } catch (Exception e) {
            log.error("Error during assignment process for ticket {}: {}", ticketId, e.getMessage(), e);
            return AssignmentResult.failed("Assignment process failed: " + e.getMessage());
        }
    }
    
    /**
     * Select best technician based on strategy and NLP analysis
     */
    private TechnicianScore selectBestTechnician(List<UserServiceClient.TechnicianDTO> technicians,
                                               NLPAnalysisResult nlpResult,
                                               AssignmentStrategy strategy,
                                               String ticketCategory) {

        return switch (strategy) {
            case LEAST_WORKLOAD -> selectByLeastWorkload(technicians);
            case BEST_SKILL -> selectByBestSkill(technicians, nlpResult);
            case HYBRID -> selectByHybridScore(technicians, nlpResult, ticketCategory);
        };
    }
    
    /**
     * Select technician with lowest workload
     */
    private TechnicianScore selectByLeastWorkload(List<UserServiceClient.TechnicianDTO> technicians) {
        UserServiceClient.TechnicianDTO selected = technicians.stream()
                .min(Comparator.comparing(UserServiceClient.TechnicianDTO::getChargeActuelle))
                .orElse(null);
        
        if (selected == null) return null;
        
        BigDecimal score = BigDecimal.valueOf(1.0 - (selected.getChargeActuelle() / (double) maxWorkloadPerTechnician));
        String reason = String.format("Selected based on lowest workload (%d tickets)",
                selected.getChargeActuelle());

        return new TechnicianScore(selected, score, reason);
    }
    
    /**
     * Select technician based on best skill match from NLP analysis
     */
    private TechnicianScore selectByBestSkill(List<UserServiceClient.TechnicianDTO> technicians,
                                            NLPAnalysisResult nlpResult) {
        
        if (nlpResult == null || nlpResult.getTechnicianScores() == null) {
            // Fallback to workload if no NLP analysis
            return selectByLeastWorkload(technicians);
        }
        
        UserServiceClient.TechnicianDTO bestTechnician = null;
        BigDecimal bestScore = BigDecimal.ZERO;

        for (UserServiceClient.TechnicianDTO tech : technicians) {
            BigDecimal score = nlpResult.getTechnicianScores().get(tech.getId());
            if (score != null && score.compareTo(bestScore) > 0) {
                bestScore = score;
                bestTechnician = tech;
            }
        }
        
        if (bestTechnician == null) {
            return selectByLeastWorkload(technicians);
        }
        
        String reason = String.format("Selected based on skill match (confidence: %.2f)", bestScore.doubleValue());
        return new TechnicianScore(bestTechnician, bestScore, reason);
    }
    
    /**
     * Select technician using hybrid scoring (workload + skill)
     * Implements tie-breaking logic as specified
     */
    private TechnicianScore selectByHybridScore(List<UserServiceClient.TechnicianDTO> technicians,
                                              NLPAnalysisResult nlpResult,
                                              String ticketCategory) {

        List<TechnicianScore> scoredTechnicians = new ArrayList<>();

        for (UserServiceClient.TechnicianDTO tech : technicians) {
            // Workload score (40% weight)
            double workloadScore = 1.0 - (tech.getChargeActuelle() / (double) maxWorkloadPerTechnician);

            // Skill score (60% weight)
            BigDecimal skillScore = BigDecimal.valueOf(0.5); // Default if no NLP
            if (nlpResult != null && nlpResult.getTechnicianScores() != null) {
                skillScore = nlpResult.getTechnicianScores().getOrDefault(tech.getId(), BigDecimal.valueOf(0.5));
            }

            // Hybrid score calculation
            BigDecimal hybridScore = BigDecimal.valueOf(workloadScore * 0.4).add(skillScore.multiply(BigDecimal.valueOf(0.6)));

            String reason = String.format(
                "Hybrid score: %.2f (workload: %.2f, skill: %.2f, charge: %d)",
                hybridScore.doubleValue(), workloadScore, skillScore.doubleValue(), tech.getChargeActuelle());

            scoredTechnicians.add(new TechnicianScore(tech, hybridScore, reason));
        }

        // Sort by hybrid score (descending), then by workload (ascending), then by competence level (descending)
        return scoredTechnicians.stream()
                .sorted((t1, t2) -> {
                    // Primary: hybrid score (higher is better)
                    int scoreComparison = t2.getScore().compareTo(t1.getScore());
                    if (scoreComparison != 0) return scoreComparison;

                    // Secondary: workload (lower is better) - as specified in requirements
                    int workloadComparison = Integer.compare(
                        t1.getTechnician().getChargeActuelle(),
                        t2.getTechnician().getChargeActuelle());
                    if (workloadComparison != 0) return workloadComparison;

                    // Tertiary: highest competence level for the problem category
                    int maxLevel1 = getMaxCompetenceLevelForCategory(t1.getTechnician(), ticketCategory);
                    int maxLevel2 = getMaxCompetenceLevelForCategory(t2.getTechnician(), ticketCategory);
                    return Integer.compare(maxLevel2, maxLevel1); // Higher level is better
                })
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Convert technician DTOs to competence objects for Gemini analysis
     */
    private List<GeminiAIService.TechnicianCompetence> convertToTechnicianCompetences(
            List<UserServiceClient.TechnicianDTO> technicians) {
        
        return technicians.stream()
                .map(tech -> GeminiAIService.TechnicianCompetence.builder()
                        .technicianId(tech.getId())
                        .name(tech.getFullName())
                        .specialite(tech.getSpecialite())
                        .currentWorkload(tech.getChargeActuelle())
                        .competencesDescription(parseCompetencesDescription(tech.getCompetencesJson()))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Parse competences JSON to readable description for Gemini AI analysis
     */
    private String parseCompetencesDescription(String competencesJson) {
        if (competencesJson == null || competencesJson.trim().isEmpty()) {
            return "Aucune compétence spécifiée";
        }

        try {
            List<Map<String, Object>> competences = objectMapper.readValue(
                    competencesJson, new TypeReference<List<Map<String, Object>>>() {});

            if (competences.isEmpty()) {
                return "Aucune compétence spécifiée";
            }

            StringBuilder description = new StringBuilder();
            description.append("\n");
            for (Map<String, Object> comp : competences) {
                String nom = (String) comp.get("nom");
                String categorie = (String) comp.get("categorie");
                String niveau = (String) comp.get("niveau");
                String desc = (String) comp.get("description");

                // Enhanced format for better AI analysis
                description.append(String.format("  🔧 COMPÉTENCE: %s\n", nom));
                description.append(String.format("     Catégorie: %s | Niveau: %s", categorie, niveau));

                // Add numerical level for easier AI processing
                int niveauNum = convertNiveauToNumber(niveau);
                description.append(String.format(" [Score: %d/5]\n", niveauNum));
                description.append(String.format("     Description: %s\n", desc));
                description.append("     ---\n");
            }

            return description.toString().trim();

        } catch (Exception e) {
            log.warn("Error parsing competences JSON: {}", e.getMessage());
            return "Erreur de parsing: " + competencesJson;
        }
    }

    /**
     * Convert competence level to numerical score for AI analysis
     */
    private int convertNiveauToNumber(String niveau) {
        if (niveau == null) return 1;
        return switch (niveau.toUpperCase()) {
            case "JUNIOR" -> 1;
            case "INTERMEDIAIRE" -> 2;
            case "AVANCE" -> 3;
            case "SENIOR" -> 4;
            case "EXPERT" -> 5;
            default -> 2; // Default to intermediate
        };
    }

    /**
     * Check if technician has competences in the specified category
     */
    private boolean hasCompetenceInCategory(UserServiceClient.TechnicianDTO technician, String category) {
        if (technician.getCompetencesJson() == null || technician.getCompetencesJson().trim().isEmpty()) {
            return false;
        }

        try {
            List<Map<String, Object>> competences = objectMapper.readValue(
                    technician.getCompetencesJson(),
                    new TypeReference<List<Map<String, Object>>>() {});

            boolean hasCategory = competences.stream()
                    .anyMatch(comp -> category.equals(comp.get("categorie")));

            if (hasCategory) {
                log.debug("Technician {} {} has competences in category {}",
                        technician.getNom(), technician.getPrenom(), category);
            }

            return hasCategory;

        } catch (Exception e) {
            log.warn("Error parsing competences JSON for technician {}: {}", technician.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Get the maximum competence level for a specific category for tie-breaking
     */
    private int getMaxCompetenceLevelForCategory(UserServiceClient.TechnicianDTO technician, String category) {
        if (technician.getCompetencesJson() == null || technician.getCompetencesJson().trim().isEmpty()) {
            return 0;
        }

        try {
            List<Map<String, Object>> competences = objectMapper.readValue(
                    technician.getCompetencesJson(),
                    new TypeReference<List<Map<String, Object>>>() {});

            return competences.stream()
                    .filter(comp -> category.equals(comp.get("categorie")))
                    .mapToInt(comp -> convertNiveauToNumber((String) comp.get("niveau")))
                    .max()
                    .orElse(0);

        } catch (Exception e) {
            log.warn("Error parsing competences JSON for technician {}: {}", technician.getId(), e.getMessage());
            return 0;
        }
    }
    
    /**
     * Serialize NLP result to JSON
     */
    private String serializeNlpResult(NLPAnalysisResult nlpResult) {
        try {
            return objectMapper.writeValueAsString(nlpResult);
        } catch (Exception e) {
            log.error("Error serializing NLP result: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Inner class to hold technician scoring results
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TechnicianScore {
        private UserServiceClient.TechnicianDTO technician;
        private BigDecimal score;
        private String reason;
    }
    
    /**
     * Result of assignment operation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class AssignmentResult {
        private boolean success;
        private boolean requiresWaiting;  // New field for EN_ATTENTE status
        private String errorMessage;
        private String ticketCategory;   // For manager notification
        private Assignment assignment;
        private UserServiceClient.TechnicianDTO assignedTechnician;
        private NLPAnalysisResult nlpAnalysis;

        public static AssignmentResult success(Assignment assignment,
                                             UserServiceClient.TechnicianDTO technician,
                                             NLPAnalysisResult nlpAnalysis) {
            return AssignmentResult.builder()
                    .success(true)
                    .requiresWaiting(false)
                    .assignment(assignment)
                    .assignedTechnician(technician)
                    .nlpAnalysis(nlpAnalysis)
                    .build();
        }

        public static AssignmentResult failed(String errorMessage) {
            return AssignmentResult.builder()
                    .success(false)
                    .requiresWaiting(false)
                    .errorMessage(errorMessage)
                    .build();
        }

        public static AssignmentResult waitingForTechnician(String ticketCategory, String reason) {
            return AssignmentResult.builder()
                    .success(false)
                    .requiresWaiting(true)
                    .ticketCategory(ticketCategory)
                    .errorMessage(reason)
                    .build();
        }
    }
}
