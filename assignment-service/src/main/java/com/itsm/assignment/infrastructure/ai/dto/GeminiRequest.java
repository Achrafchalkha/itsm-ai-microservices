package com.itsm.assignment.infrastructure.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Gemini AI API
 * Based on Gemini Pro API structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    
    private List<Content> contents;
    private GenerationConfig generationConfig;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
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
    public static class GenerationConfig {
        private Double temperature;
        private Integer maxOutputTokens;
        private Double topP;
        private Integer topK;
    }
    
    /**
     * Create a request for ticket assignment analysis
     */
    public static GeminiRequest createAssignmentAnalysisRequest(String prompt) {
        return GeminiRequest.builder()
                .contents(List.of(
                    Content.builder()
                        .parts(List.of(
                            Part.builder()
                                .text(prompt)
                                .build()
                        ))
                        .build()
                ))
                .generationConfig(GenerationConfig.builder()
                    .temperature(0.3) // Lower temperature for more consistent results
                    .maxOutputTokens(1000)
                    .topP(0.8)
                    .topK(40)
                    .build())
                .build();
    }
}
