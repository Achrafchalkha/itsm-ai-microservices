package com.itsm.user.infrastructure.client;

import com.itsm.user.domain.model.Role;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Client to communicate with Auth-Service
 * Handles user authentication and user data retrieval
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    /**
     * Get user information from auth-service
     */
    public AuthUserDto getUserById(UUID userId, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AuthUserDto> response = restTemplate.exchange(
                authServiceUrl + "/api/auth/users/" + userId,
                HttpMethod.GET,
                entity,
                AuthUserDto.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching user from auth-service: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch user data", e);
        }
    }
    
    /**
     * Get all users from auth-service
     */
    public List<AuthUserDto> getAllUsers(String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<AuthUserDto[]> response = restTemplate.exchange(
                authServiceUrl + "/api/auth/users",
                HttpMethod.GET,
                entity,
                AuthUserDto[].class
            );
            
            return List.of(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching users from auth-service: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch users data", e);
        }
    }
    
    /**
     * Create user in auth-service
     */
    public AuthUserDto createUser(CreateUserRequest request, String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<AuthUserDto> response = restTemplate.exchange(
                authServiceUrl + "/api/auth/register",
                HttpMethod.POST,
                entity,
                AuthUserDto.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating user in auth-service: {}", e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Validate JWT token with auth-service
     */
    public boolean validateToken(String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                authServiceUrl + "/api/auth/validate",
                HttpMethod.GET,
                entity,
                Void.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Data
    public static class AuthUserDto {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private Role role;
        private boolean actif;
    }
    
    @Data
    public static class CreateUserRequest {
        private String nom;
        private String prenom;
        private String email;
        private String motDePasse;
        private Role role;
    }
}
