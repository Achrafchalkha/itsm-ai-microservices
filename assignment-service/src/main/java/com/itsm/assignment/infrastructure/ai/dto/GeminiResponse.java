package com.itsm.assignment.infrastructure.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Gemini AI API
 * Based on Gemini Pro API response structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Integer index;
        private List<SafetyRating> safetyRatings;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
        private String role;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRating {
        private String category;
        private String probability;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;
    }
    
    /**
     * Extract the generated text from the response
     */
    public String getGeneratedText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null && 
                firstCandidate.getContent().getParts() != null && 
                !firstCandidate.getContent().getParts().isEmpty()) {
                return firstCandidate.getContent().getParts().get(0).getText();
            }
        }
        return null;
    }
}
