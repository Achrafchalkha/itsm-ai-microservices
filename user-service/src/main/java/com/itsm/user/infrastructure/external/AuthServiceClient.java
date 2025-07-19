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
import java.util.UUID;

/**
 * Client for async communication with auth-service
 * Handles dual database synchronization - same ID in both auth_db and user_db
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
     * CRITICAL: Uses the SAME ID as created in user-service for dual database sync
     */
    @Async
    public void createManagerAuth(Utilisateur manager, String password, String teamName, String teamDescription) {
        try {
            log.info("Creating manager authentication in auth-service with SAME ID: {} for: {}", 
                    manager.getId(), manager.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("id", manager.getId().toString());  // SAME ID for dual database sync
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

            String url = authServiceUrl + "/api/auth/sync/create-user";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Manager authentication created successfully in auth_db with SAME ID: {} for: {}", 
                    manager.getId(), manager.getEmail());
        } catch (Exception e) {
            log.error("Failed to create manager authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to create technician authentication in auth-service
     * CRITICAL: Uses the SAME ID as created in user-service for dual database sync
     */
    @Async
    public void createTechnicianAuth(Utilisateur technician, String password) {
        try {
            log.info("Creating technician authentication in auth-service with SAME ID: {} for: {}", 
                    technician.getId(), technician.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("id", technician.getId().toString());  // SAME ID for dual database sync
            request.put("nom", technician.getNom());
            request.put("prenom", technician.getPrenom());
            request.put("email", technician.getEmail());
            request.put("password", password);
            request.put("role", technician.getRole().name());
            request.put("teamId", technician.getTeamId() != null ? technician.getTeamId().toString() : null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/create-user";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Technician authentication created successfully in auth_db with SAME ID: {} for: {}", 
                    technician.getId(), technician.getEmail());
        } catch (Exception e) {
            log.error("Failed to create technician authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to update manager authentication in auth-service
     * Updates auth_db.utilisateurs with same ID
     */
    @Async
    public void updateManagerAuth(Utilisateur manager, String newPassword, boolean emailChanged, boolean passwordChanged) {
        try {
            log.info("Updating manager authentication in auth-service with ID: {} for: {}", 
                    manager.getId(), manager.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("id", manager.getId().toString());
            request.put("nom", manager.getNom());
            request.put("prenom", manager.getPrenom());
            request.put("email", manager.getEmail());
            request.put("role", manager.getRole().name());
            request.put("emailChanged", emailChanged);
            request.put("passwordChanged", passwordChanged);
            
            if (passwordChanged && newPassword != null) {
                request.put("newPassword", newPassword);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/update-user";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Manager authentication updated successfully in auth-service: {}", manager.getEmail());
        } catch (Exception e) {
            log.error("Failed to update manager authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to update technician authentication in auth-service
     * Updates auth_db.utilisateurs with same ID
     */
    @Async
    public void updateTechnicianAuth(Utilisateur technician, String newPassword, boolean emailChanged, boolean passwordChanged) {
        try {
            log.info("Updating technician authentication in auth-service with ID: {} for: {}", 
                    technician.getId(), technician.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("id", technician.getId().toString());
            request.put("nom", technician.getNom());
            request.put("prenom", technician.getPrenom());
            request.put("email", technician.getEmail());
            request.put("role", technician.getRole().name());
            request.put("emailChanged", emailChanged);
            request.put("passwordChanged", passwordChanged);
            
            if (passwordChanged && newPassword != null) {
                request.put("newPassword", newPassword);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/update-user";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Technician authentication updated successfully in auth-service: {}", technician.getEmail());
        } catch (Exception e) {
            log.error("Failed to update technician authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to disable user authentication in auth-service by ID
     * Uses same ID for dual database synchronization
     */
    @Async
    public void disableUserAuth(UUID userId, String email) {
        try {
            log.info("Disabling user authentication in auth-service with ID: {} for: {}", userId, email);

            Map<String, Object> request = new HashMap<>();
            request.put("id", userId.toString());  // Use same ID for consistency
            request.put("email", email);
            request.put("actif", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/disable-user";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("User authentication disabled successfully in auth-service with ID: {} for: {}", userId, email);
        } catch (Exception e) {
            log.error("Failed to disable user authentication in auth-service: {}", e.getMessage(), e);
        }
    }

    /**
     * Async call to reactivate user authentication in auth-service by ID
     * Uses same ID for dual database synchronization
     */
    @Async
    public void reactivateUserAuth(UUID userId, String email) {
        try {
            log.info("Reactivating user authentication in auth-service with ID: {} for: {}", userId, email);

            Map<String, Object> request = new HashMap<>();
            request.put("id", userId.toString());  // Use same ID for consistency
            request.put("email", email);
            request.put("actif", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String url = authServiceUrl + "/api/auth/sync/reactivate-user";
            restTemplate.postForEntity(url, entity, String.class);

            log.info("User authentication reactivated successfully in auth-service with ID: {} for: {}", userId, email);
        } catch (Exception e) {
            log.error("Failed to reactivate user authentication in auth-service: {}", e.getMessage(), e);
        }
    }
}
