package com.itsm.assignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Result of NLP analysis performed by Gemini AI
 * Contains extracted information from ticket description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPAnalysisResult {
    
    /**
     * Technologies detected in the ticket description
     */
    private List<String> detectedTechnologies;
    
    /**
     * Required competences extracted from description
     */
    private List<String> requiredCompetences;
    
    /**
     * Complexity level assessed by AI (LOW, MEDIUM, HIGH)
     */
    private String complexityLevel;
    
    /**
     * Urgency level detected from description
     */
    private String urgencyLevel;
    
    /**
     * Keywords extracted from description
     */
    private List<String> keywords;
    
    /**
     * Technician scores based on competence matching
     * Map of technicianId -> confidence score (0.0 to 1.0)
     */
    private Map<UUID, BigDecimal> technicianScores;
    
    /**
     * Recommended technician ID (highest score)
     */
    private UUID recommendedTechnicianId;
    
    /**
     * Overall confidence in the analysis (0.0 to 1.0)
     */
    private BigDecimal overallConfidence;
    
    /**
     * Detailed reasoning for the recommendation
     */
    private String reasoning;
    
    /**
     * Raw response from Gemini API
     */
    private String rawGeminiResponse;
    
    /**
     * Analysis timestamp
     */
    private java.time.LocalDateTime analyzedAt;
    
    /**
     * Check if analysis has high confidence
     */
    public boolean hasHighConfidence() {
        return overallConfidence != null && overallConfidence.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
    
    /**
     * Get top N technician recommendations
     */
    public List<UUID> getTopTechnicians(int limit) {
        if (technicianScores == null || technicianScores.isEmpty()) {
            return List.of();
        }
        
        return technicianScores.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}
