package com.itsm.ticket.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Test controller for Gemini AI API
 * Used to diagnose and test Gemini API connectivity
 */
@RestController
@RequestMapping("/api/test/gemini")
@RequiredArgsConstructor
@Slf4j
public class GeminiTestController {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.nlp.gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${app.nlp.gemini.url}")
    private String geminiUrl;
    
    @Value("${app.nlp.enabled:true}")
    private boolean nlpEnabled;
    
    /**
     * Test Gemini API with a simple prompt
     */
    @PostMapping("/test-prompt")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GeminiTestResponse> testGeminiPrompt(@Valid @RequestBody GeminiTestRequest request) {
        log.info("Testing Gemini API with prompt: {}", request.getPrompt());
        
        GeminiTestResponse.GeminiTestResponseBuilder responseBuilder = GeminiTestResponse.builder()
                .nlpEnabled(nlpEnabled)
                .apiKeyConfigured(geminiApiKey != null && !geminiApiKey.trim().isEmpty())
                .apiUrl(geminiUrl);
        
        if (!nlpEnabled) {
            return ResponseEntity.ok(responseBuilder
                    .success(false)
                    .message("NLP is disabled in configuration")
                    .build());
        }
        
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return ResponseEntity.ok(responseBuilder
                    .success(false)
                    .message("Gemini API key is not configured")
                    .build());
        }
        
        try {
            String response = callGeminiAPI(request.getPrompt());
            
            return ResponseEntity.ok(responseBuilder
                    .success(true)
                    .message("Gemini API call successful")
                    .prompt(request.getPrompt())
                    .geminiResponse(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Gemini API test failed: {}", e.getMessage(), e);
            
            return ResponseEntity.ok(responseBuilder
                    .success(false)
                    .message("Gemini API call failed: " + e.getMessage())
                    .prompt(request.getPrompt())
                    .error(e.getMessage())
                    .build());
        }
    }
    
    /**
     * List available Gemini models
     */
    @GetMapping("/models")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listGeminiModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1/models?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("Error listing Gemini models: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "message", "Failed to list models"
            ));
        }
    }

    /**
     * Get Gemini API configuration info
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeminiConfigResponse> getGeminiConfig() {
        
        GeminiConfigResponse response = GeminiConfigResponse.builder()
                .nlpEnabled(nlpEnabled)
                .apiUrl(geminiUrl)
                .apiKeyConfigured(geminiApiKey != null && !geminiApiKey.trim().isEmpty())
                .apiKeyLength(geminiApiKey != null ? geminiApiKey.length() : 0)
                .apiKeyPrefix(geminiApiKey != null && geminiApiKey.length() > 10 ? 
                    geminiApiKey.substring(0, 10) + "..." : "Not configured")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test a simple categorization prompt
     */
    @PostMapping("/test-categorization")
    @PreAuthorize("hasRole('UTILISATEUR') or hasRole('TECHNICIEN') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<GeminiTestResponse> testCategorization(@Valid @RequestBody CategorizationTestRequest request) {

        String prompt = buildCategorizationPrompt(request.getTitre(), request.getDescription());

        GeminiTestRequest testRequest = GeminiTestRequest.builder()
                .prompt(prompt)
                .build();

        return testGeminiPrompt(testRequest);
    }

    /**
     * Test with different model
     */
    @PostMapping("/test-model/{modelName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeminiTestResponse> testWithModel(
            @PathVariable String modelName,
            @Valid @RequestBody GeminiTestRequest request) {

        log.info("Testing Gemini API with model: {} and prompt: {}", modelName, request.getPrompt());

        GeminiTestResponse.GeminiTestResponseBuilder responseBuilder = GeminiTestResponse.builder()
                .nlpEnabled(nlpEnabled)
                .apiKeyConfigured(geminiApiKey != null && !geminiApiKey.trim().isEmpty())
                .apiUrl("https://generativelanguage.googleapis.com/v1/models/" + modelName + ":generateContent");

        try {
            String response = callGeminiAPIWithModel(request.getPrompt(), modelName);

            return ResponseEntity.ok(responseBuilder
                    .success(true)
                    .message("Gemini API call successful with model: " + modelName)
                    .prompt(request.getPrompt())
                    .geminiResponse(response)
                    .build());

        } catch (Exception e) {
            log.error("Gemini API test failed with model {}: {}", modelName, e.getMessage(), e);

            return ResponseEntity.ok(responseBuilder
                    .success(false)
                    .message("Gemini API call failed with model " + modelName + ": " + e.getMessage())
                    .prompt(request.getPrompt())
                    .error(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Call Gemini API with specific model
     */
    private String callGeminiAPIWithModel(String prompt, String modelName) {
        String url = "https://generativelanguage.googleapis.com/v1/models/" + modelName + ":generateContent?key=" + geminiApiKey;
        return callGeminiAPIInternal(prompt, url);
    }

    /**
     * Call Gemini API with the given prompt
     */
    private String callGeminiAPI(String prompt) {
        String url = geminiUrl + "?key=" + geminiApiKey;
        return callGeminiAPIInternal(prompt, url);
    }

    /**
     * Internal method to call Gemini API
     */
    private String callGeminiAPIInternal(String prompt, String url) {
        log.info("Calling Gemini API at: {}", url);
        log.debug("API Key length: {}", geminiApiKey.length());
        log.debug("Prompt: {}", prompt);

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

            log.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));

            ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

            log.info("Gemini API response status: {}", response.getStatusCode());
            log.debug("Gemini API response body: {}", response.getBody());

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }

            throw new RuntimeException("Invalid response format from Gemini API: " + responseBody);

        } catch (RestClientException e) {
            log.error("Gemini API REST error: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Gemini API general error: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build a simple categorization prompt for testing
     */
    private String buildCategorizationPrompt(String titre, String description) {
        return String.format("""
            Analyse ce ticket et réponds avec une catégorie parmi: SECURITE, AUDIT, CONFORMITE, DEVELOPPEMENT, DEVOPS, CLOUD
            
            Titre: %s
            Description: %s
            
            Réponds uniquement avec le nom de la catégorie.
            """, titre, description);
    }
    
    /**
     * Request DTO for Gemini test
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GeminiTestRequest {
        @jakarta.validation.constraints.NotBlank(message = "Le prompt est obligatoire")
        private String prompt;
    }
    
    /**
     * Request DTO for categorization test
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategorizationTestRequest {
        @jakarta.validation.constraints.NotBlank(message = "Le titre est obligatoire")
        private String titre;
        
        @jakarta.validation.constraints.NotBlank(message = "La description est obligatoire")
        private String description;
    }
    
    /**
     * Response DTO for Gemini test
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GeminiTestResponse {
        private boolean success;
        private String message;
        private boolean nlpEnabled;
        private boolean apiKeyConfigured;
        private String apiUrl;
        private String prompt;
        private String geminiResponse;
        private String error;
    }
    
    /**
     * Response DTO for Gemini configuration
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GeminiConfigResponse {
        private boolean nlpEnabled;
        private String apiUrl;
        private boolean apiKeyConfigured;
        private int apiKeyLength;
        private String apiKeyPrefix;
    }
}
