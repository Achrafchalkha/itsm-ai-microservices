package com.itsm.user.infrastructure.external;

import com.itsm.user.domain.model.Utilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for async communication with auth-service
 * Handles authentication credential creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    /**
     * Async call to create manager authentication in auth-service
     */
    @Async
    public void createManagerAuth(Utilisateur manager, String password, String teamName, String teamDescription) {
        try {
            log.info("Creating manager authentication in auth-service for: {}", manager.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("id", manager.getId());
            request.put("nom", manager.getNom());
            request.put("prenom", manager.getPrenom());
            request.put("email", manager.getEmail());
            request.put("password", password);
            request.put("role", manager.getRole().name());
            request.put("teamName", teamName);
            request.put("teamDescription", teamDescription);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/manager";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Manager authentication created successfully in auth-service: {}", manager.getEmail());
        } catch (Exception e) {
            log.error("Failed to create manager authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to create technician authentication in auth-service
     */
    @Async
    public void createTechnicianAuth(Utilisateur technician, String password) {
        try {
            log.info("Creating technician authentication in auth-service for: {}", technician.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("id", technician.getId());
            request.put("nom", technician.getNom());
            request.put("prenom", technician.getPrenom());
            request.put("email", technician.getEmail());
            request.put("password", password);
            request.put("role", technician.getRole().name());
            request.put("teamId", technician.getTeamId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/technician";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Technician authentication created successfully in auth-service: {}", technician.getEmail());
        } catch (Exception e) {
            log.error("Failed to create technician authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to disable user authentication in auth-service
     */
    @Async
    public void disableUserAuth(String email) {
        try {
            log.info("Disabling user authentication in auth-service for: {}", email);

            String url = authServiceUrl + "/api/auth/sync/disable/" + email;
            restTemplate.postForEntity(url, null, String.class);

            log.info("User authentication disabled successfully in auth-service: {}", email);
        } catch (Exception e) {
            log.error("Failed to disable user authentication in auth-service: {}", e.getMessage(), e);
        }
    }
}
