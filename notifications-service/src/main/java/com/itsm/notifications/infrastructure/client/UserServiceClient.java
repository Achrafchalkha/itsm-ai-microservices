package com.itsm.notifications.infrastructure.client;

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
 * Retrieves team and manager information for notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${user-service.url}")
    private String userServiceUrl;

    private static final String PUBLIC_API_BASE = "/api/public/assignment";

    /**
     * Get team manager ID by team ID
     */
    public UUID getTeamManagerId(UUID teamId) {
        try {
            log.info("🌐 CALLING USER SERVICE: GET {}/teams/{}/manager", userServiceUrl + PUBLIC_API_BASE, teamId);

            WebClient webClient = webClientBuilder
                    .baseUrl(userServiceUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            TeamManagerInfo managerInfo = webClient.get()
                    .uri(PUBLIC_API_BASE + "/teams/{teamId}/manager", teamId)
                    .retrieve()
                    .bodyToMono(TeamManagerInfo.class)
                    .block();

            if (managerInfo != null) {
                log.info("✅ USER SERVICE RESPONSE: Found manager {} ({}) for team {}",
                        managerInfo.getManagerId(), managerInfo.getManagerName(), teamId);
                return managerInfo.getManagerId();
            } else {
                log.warn("❌ USER SERVICE RESPONSE: No manager found for team {}", teamId);
                return null;
            }

        } catch (Exception e) {
            log.error("❌ USER SERVICE ERROR: Error fetching manager for team {}: {}", teamId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Team manager info DTO
     */
    public static class TeamManagerInfo {
        private UUID managerId;
        private String managerName;
        private String managerEmail;

        // Default constructor for JSON deserialization
        public TeamManagerInfo() {}

        public TeamManagerInfo(UUID managerId, String managerName, String managerEmail) {
            this.managerId = managerId;
            this.managerName = managerName;
            this.managerEmail = managerEmail;
        }

        public UUID getManagerId() {
            return managerId;
        }

        public void setManagerId(UUID managerId) {
            this.managerId = managerId;
        }

        public String getManagerName() {
            return managerName;
        }

        public void setManagerName(String managerName) {
            this.managerName = managerName;
        }

        public String getManagerEmail() {
            return managerEmail;
        }

        public void setManagerEmail(String managerEmail) {
            this.managerEmail = managerEmail;
        }
    }
}
