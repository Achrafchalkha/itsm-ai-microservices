package com.itsm.assignment.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

/**
 * Client for communicating with user-service
 * Retrieves team and technician information for assignment
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${user-service.url}")
    private String userServiceUrl;

    private static final String PUBLIC_API_BASE = "/api/public/assignment";

    /**
     * Get all active technicians
     */
    public List<TechnicianDTO> getAllActiveTechnicians() {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            String response = webClient.get()
                    .uri(PUBLIC_API_BASE + "/technicians")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<List<TechnicianDTO>>() {});

        } catch (Exception e) {
            log.error("Error fetching all active technicians: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Find teams that handle a specific category
     */
    public List<TeamDTO> findTeamsByCategory(String category) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            String response = webClient.get()
                    .uri(PUBLIC_API_BASE + "/teams/category/{category}", category)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            return objectMapper.readValue(response, new TypeReference<List<TeamDTO>>() {});
            
        } catch (Exception e) {
            log.error("Error fetching teams for category {}: {}", category, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get active technicians for specific teams
     */
    public List<TechnicianDTO> getActiveTechniciansByTeams(List<UUID> teamIds) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            String response = webClient.post()
                    .uri(PUBLIC_API_BASE + "/technicians/by-teams")
                    .bodyValue(teamIds)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<List<TechnicianDTO>>() {});

        } catch (Exception e) {
            log.error("Error fetching technicians for teams {}: {}", teamIds, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get active technicians who have competences in a specific category
     */
    public List<TechnicianDTO> getTechniciansByCompetenceCategory(String category) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            String response = webClient.get()
                    .uri(PUBLIC_API_BASE + "/technicians/competence-category/{category}", category)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<List<TechnicianDTO>>() {});

        } catch (Exception e) {
            log.error("Error fetching technicians for competence category {}: {}", category, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get technician details by ID
     */
    public TechnicianDTO getTechnicianById(UUID technicianId) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            String response = webClient.get()
                    .uri("/api/technicians/{id}", technicianId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            return objectMapper.readValue(response, TechnicianDTO.class);
            
        } catch (Exception e) {
            log.error("Error fetching technician {}: {}", technicianId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Update technician workload (increment/decrement charge_actuelle)
     */
    public void updateTechnicianWorkload(UUID technicianId, int workloadChange) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            webClient.put()
                    .uri(PUBLIC_API_BASE + "/technicians/{technicianId}/workload?increment={increment}", technicianId, workloadChange)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            log.info("Updated workload for technician {} by {}", technicianId, workloadChange);
            
        } catch (Exception e) {
            log.error("Error updating workload for technician {}: {}", technicianId, e.getMessage());
        }
    }
    
    /**
     * DTO for Team information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TeamDTO {
        private UUID id;
        private String nom;
        private String description;
        private UUID managerId;
        private List<String> categories;
        private List<UUID> memberIds;
        private boolean actif;
    }
    
    /**
     * DTO for Technician information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnicianDTO {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private UUID teamId;
        private String localisation;
        private String telephone;
        private String specialite;
        private String competencesJson;
        private Integer chargeActuelle;
        private boolean actif;
        
        public String getFullName() {
            return prenom + " " + nom;
        }
    }
}
