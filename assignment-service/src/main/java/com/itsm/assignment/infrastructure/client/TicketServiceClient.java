package com.itsm.assignment.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Client for communicating with ticket-service
 * Updates ticket assignment information
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${ticket-service.url}")
    private String ticketServiceUrl;

    private static final String PUBLIC_API_BASE = "/api/public/assignment";
    
    /**
     * Get ticket details by ID
     */
    public TicketDTO getTicketById(UUID ticketId) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(ticketServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            String response = webClient.get()
                    .uri(PUBLIC_API_BASE + "/tickets/{id}", ticketId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            return objectMapper.readValue(response, TicketDTO.class);
            
        } catch (Exception e) {
            log.error("Error fetching ticket {}: {}", ticketId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Update ticket assignment information
     */
    public void updateTicketAssignment(UUID ticketId, UUID technicianId, UUID teamId) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(ticketServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            webClient.put()
                    .uri(PUBLIC_API_BASE + "/tickets/{id}/assignment?technicianId={technicianId}&teamId={teamId}",
                         ticketId, technicianId, teamId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            log.info("Updated assignment for ticket {} to technician {} in team {}", 
                    ticketId, technicianId, teamId);
            
        } catch (Exception e) {
            log.error("Error updating ticket assignment for {}: {}", ticketId, e.getMessage());
        }
    }
    
    /**
     * Update ticket status
     */
    public void updateTicketStatus(UUID ticketId, String status) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(ticketServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            StatusUpdateRequest request = StatusUpdateRequest.builder()
                    .statut(status)
                    .build();
            
            webClient.put()
                    .uri("/api/tickets/{id}/status", ticketId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            log.info("Updated status for ticket {} to {}", ticketId, status);
            
        } catch (Exception e) {
            log.error("Error updating ticket status for {}: {}", ticketId, e.getMessage());
        }
    }
    
    /**
     * DTO for Ticket information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TicketDTO {
        private UUID id;
        private String titre;
        private String description;
        private String statut;
        private String priorite;
        private String categorie;
        private UUID utilisateurId;
        private UUID technicienId;
        private UUID teamId;
        private Boolean enableNlp;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private boolean actif;
    }
    
    /**
     * Request DTO for updating ticket assignment
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssignmentUpdateRequest {
        private UUID technicienId;
        private UUID teamId;
    }
    
    /**
     * Request DTO for updating ticket status
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatusUpdateRequest {
        private String statut;
    }
}
