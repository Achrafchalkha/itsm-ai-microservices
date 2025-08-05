package com.itsm.ticket.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * Client for communicating with user-service
 * Retrieves manager and team information for ticket filtering
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
     * Get manager's team ID (simple approach)
     * Queries utilisateurs table with manager ID to get team_id
     * Used to filter tickets by team for manager dashboard
     */
    public UUID getManagerTeamId(UUID managerId) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            UUID teamId = webClient.get()
                    .uri(PUBLIC_API_BASE + "/managers/{managerId}/team", managerId)
                    .retrieve()
                    .bodyToMono(UUID.class)
                    .block();

            log.debug("Retrieved team ID {} for manager {}", teamId, managerId);
            return teamId;

        } catch (Exception e) {
            log.error("Error fetching team ID for manager {}: {}", managerId, e.getMessage());
            return null;
        }
    }

    /**
     * Get technician basic info (simple approach)
     * Queries utilisateurs table with technician ID to get basic info
     * Used to display technician details in ticket lists
     */
    public TechnicianBasicInfo getTechnicianBasicInfo(UUID technicianId) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            TechnicianBasicInfo technicianInfo = webClient.get()
                    .uri(PUBLIC_API_BASE + "/technicians/{technicianId}/info", technicianId)
                    .retrieve()
                    .bodyToMono(TechnicianBasicInfo.class)
                    .block();

            log.debug("Retrieved technician info for {}: {} {}", technicianId,
                     technicianInfo != null ? technicianInfo.getPrenom() : "null",
                     technicianInfo != null ? technicianInfo.getNom() : "null");
            return technicianInfo;

        } catch (Exception e) {
            log.error("Error fetching technician info for {}: {}", technicianId, e.getMessage());
            return null;
        }
    }

    /**
     * Basic technician info DTO
     */
    public static class TechnicianBasicInfo {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private String specialite;
        private UUID teamId;

        // Default constructor for JSON deserialization
        public TechnicianBasicInfo() {}

        public TechnicianBasicInfo(UUID id, String nom, String prenom, String email, String specialite, UUID teamId) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.specialite = specialite;
            this.teamId = teamId;
        }

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getSpecialite() { return specialite; }
        public void setSpecialite(String specialite) { this.specialite = specialite; }

        public UUID getTeamId() { return teamId; }
        public void setTeamId(UUID teamId) { this.teamId = teamId; }
    }
}
