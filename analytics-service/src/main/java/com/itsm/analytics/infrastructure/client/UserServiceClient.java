package com.itsm.analytics.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Client for communicating with user-service
 * Retrieves user and team data for analytics calculations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${user-service.url}")
    private String userServiceUrl;
    
    /**
     * Get all teams
     */
    public List<TeamDTO> getAllTeams() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/teams")
                    .toUriString();
            
            ResponseEntity<List<TeamDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TeamDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting all teams: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get team by ID
     */
    public TeamDTO getTeamById(UUID teamId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/teams/{teamId}")
                    .buildAndExpand(teamId)
                    .toUriString();
            
            ResponseEntity<TeamDTO> response = restTemplate.getForEntity(url, TeamDTO.class);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting team {}: {}", teamId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get technicians by team
     */
    public List<TechnicianDTO> getTechniciansByTeam(UUID teamId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/technicians/team/{teamId}")
                    .buildAndExpand(teamId)
                    .toUriString();
            
            ResponseEntity<List<TechnicianDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TechnicianDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting technicians for team {}: {}", teamId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get all technicians
     */
    public List<TechnicianDTO> getAllTechnicians() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/technicians")
                    .toUriString();
            
            ResponseEntity<List<TechnicianDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TechnicianDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting all technicians: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get technician by ID
     */
    public TechnicianDTO getTechnicianById(UUID technicianId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/technicians/{technicianId}")
                    .buildAndExpand(technicianId)
                    .toUriString();
            
            ResponseEntity<TechnicianDTO> response = restTemplate.getForEntity(url, TechnicianDTO.class);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting technician {}: {}", technicianId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get manager by ID
     */
    public ManagerDTO getManagerById(UUID managerId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/managers/{managerId}")
                    .buildAndExpand(managerId)
                    .toUriString();
            
            ResponseEntity<ManagerDTO> response = restTemplate.getForEntity(url, ManagerDTO.class);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting manager {}: {}", managerId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get teams by category
     */
    public List<TeamDTO> getTeamsByCategory(String category) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/teams/category/{category}")
                    .buildAndExpand(category)
                    .toUriString();
            
            ResponseEntity<List<TeamDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TeamDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting teams for category {}: {}", category, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get workload statistics for all technicians
     */
    public WorkloadStatsDTO getWorkloadStatistics() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
                    .path("/api/technicians/stats/workload")
                    .toUriString();
            
            ResponseEntity<WorkloadStatsDTO> response = restTemplate.getForEntity(url, WorkloadStatsDTO.class);
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting workload statistics: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * DTO for team data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamDTO {
        private UUID id;
        private String nom;
        private String description;
        private UUID managerId;
        private List<String> categories;
        private List<UUID> memberIds;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private boolean actif;
    }
    
    /**
     * DTO for technician data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private boolean actif;
    }
    
    /**
     * DTO for manager data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManagerDTO {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private UUID teamId;
        private String localisation;
        private String telephone;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private boolean actif;
    }
    
    /**
     * DTO for workload statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkloadStatsDTO {
        private int totalTechnicians;
        private int activeTechnicians;
        private double averageWorkload;
        private int maxWorkload;
        private int minWorkload;
        private int totalWorkload;
        private List<TechnicianWorkloadDTO> technicianWorkloads;
    }
    
    /**
     * DTO for individual technician workload
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicianWorkloadDTO {
        private UUID technicianId;
        private String nom;
        private String prenom;
        private UUID teamId;
        private int chargeActuelle;
        private boolean actif;
    }
}
