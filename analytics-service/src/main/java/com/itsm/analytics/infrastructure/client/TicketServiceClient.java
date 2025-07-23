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
 * Client for communicating with ticket-service
 * Retrieves ticket data for analytics calculations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${ticket-service.url}")
    private String ticketServiceUrl;
    
    /**
     * Get tickets created between two dates
     */
    public List<TicketDTO> getTicketsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/created-between")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets created between {} and {}: {}", start, end, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get tickets resolved between two dates
     */
    public List<TicketDTO> getTicketsResolvedBetween(LocalDateTime start, LocalDateTime end) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/resolved-between")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets resolved between {} and {}: {}", start, end, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get tickets closed between two dates
     */
    public List<TicketDTO> getTicketsClosedBetween(LocalDateTime start, LocalDateTime end) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/closed-between")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets closed between {} and {}: {}", start, end, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get all tickets in a period
     */
    public List<TicketDTO> getTicketsByPeriod(LocalDateTime start, LocalDateTime end) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/period")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets by period {} to {}: {}", start, end, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get tickets by team and period
     */
    public List<TicketDTO> getTicketsByTeamAndPeriod(UUID teamId, LocalDateTime start, LocalDateTime end) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/team/{teamId}")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .buildAndExpand(teamId)
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets by team {} and period {} to {}: {}", teamId, start, end, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get resolved tickets by team and period
     */
    public List<TicketDTO> getResolvedTicketsByTeamAndPeriod(UUID teamId, LocalDateTime start, LocalDateTime end) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/team/{teamId}/resolved")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .buildAndExpand(teamId)
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting resolved tickets by team {} and period {} to {}: {}", teamId, start, end, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get tickets approaching SLA deadline
     */
    public List<TicketDTO> getTicketsApproachingSLA(int hoursBeforeDeadline) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/approaching-sla")
                    .queryParam("hours", hoursBeforeDeadline)
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets approaching SLA: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get tickets that breached SLA
     */
    public List<TicketDTO> getTicketsBreachedSLA() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(ticketServiceUrl)
                    .path("/api/tickets/analytics/breached-sla")
                    .toUriString();
            
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TicketDTO>>() {});
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting tickets that breached SLA: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * DTO for ticket data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
        
        // SLA tracking
        private LocalDateTime dateLimiteSla;
        private LocalDateTime datePremiereReponse;
        private Boolean slaRespecte;
        private Integer tempsResolutionMinutes;
        private String statutSla;
        
        // Analytics
        private Integer nombreReassignations;
        private Integer tempsPremiereReponseMinutes;
        
        // Timestamps
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private LocalDateTime dateFermeture;
        
        // Additional fields
        private String commentaireResolution;
        private Boolean enableNlp;
        private Boolean actif;
    }
}
