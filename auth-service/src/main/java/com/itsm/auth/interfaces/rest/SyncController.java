package com.itsm.auth.interfaces.rest;

import com.itsm.auth.application.service.UtilisateurService;
import com.itsm.auth.domain.model.Role;
import com.itsm.auth.domain.model.Utilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Sync Controller for auth-service
 * Handles synchronization requests from user-service
 * Creates authentication records with the same ID as user-service
 */
@RestController
@RequestMapping("/api/auth/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

    private final UtilisateurService utilisateurService;

    /**
     * Create user authentication record with specific ID (from user-service)
     * This ensures both services have the same ID for the same user
     */
    @PostMapping("/create-user")
    public ResponseEntity<String> createUserAuth(@RequestBody Map<String, Object> request) {
        try {
            // Extract data from request
            UUID id = UUID.fromString((String) request.get("id"));
            String nom = (String) request.get("nom");
            String prenom = (String) request.get("prenom");
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            String roleStr = (String) request.get("role");
            
            Role role = Role.valueOf(roleStr);
            
            log.info("Sync request: Creating auth record with ID: {} for user: {} ({})", 
                    id, email, role);

            // Check if user already exists in auth-service
            if (utilisateurService.existsByEmail(email)) {
                log.warn("User already exists in auth-service: {}", email);
                return ResponseEntity.ok("User already exists");
            }

            if (utilisateurService.existsById(id)) {
                log.warn("User ID already exists in auth-service: {}", id);
                return ResponseEntity.ok("User ID already exists");
            }

            // Create user with the SAME ID as in user-service
            Utilisateur utilisateur = Utilisateur.builder()
                    .id(id)  // Use the same ID from user-service
                    .nom(nom)
                    .prenom(prenom)
                    .email(email)
                    .role(role)
                    .actif(true)
                    .build();

            // Save with hashed password
            utilisateurService.creerUtilisateurAvecId(utilisateur, password);
            
            log.info("Successfully created auth record with same ID: {} for user: {}", id, email);
            return ResponseEntity.ok("User authentication created successfully");
            
        } catch (Exception e) {
            log.error("Failed to create user authentication: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create user authentication: " + e.getMessage());
        }
    }

    /**
     * Update user authentication record (from user-service)
     * Updates auth_db.utilisateurs with same ID
     */
    @PostMapping("/update-user")
    public ResponseEntity<String> updateUserAuth(@RequestBody Map<String, Object> request) {
        try {
            UUID id = UUID.fromString((String) request.get("id"));
            String nom = (String) request.get("nom");
            String prenom = (String) request.get("prenom");
            String email = (String) request.get("email");
            String roleStr = (String) request.get("role");
            boolean emailChanged = (Boolean) request.getOrDefault("emailChanged", false);
            boolean passwordChanged = (Boolean) request.getOrDefault("passwordChanged", false);
            String newPassword = (String) request.get("newPassword");

            Role role = Role.valueOf(roleStr);

            log.info("Sync request: Updating auth record with ID: {} for user: {} ({})",
                    id, email, role);

            // Update user in auth-service
            utilisateurService.mettreAJourUtilisateur(id, nom, prenom, email, role,
                    emailChanged, passwordChanged, newPassword);

            log.info("Successfully updated auth record with ID: {} for user: {}", id, email);
            return ResponseEntity.ok("User authentication updated successfully");

        } catch (Exception e) {
            log.error("Failed to update user authentication: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user authentication: " + e.getMessage());
        }
    }

    /**
     * Disable user authentication (soft delete)
     */
    @PostMapping("/disable/{email}")
    public ResponseEntity<String> disableUserAuth(@PathVariable String email) {
        try {
            log.info("Sync request: Disabling auth for user: {}", email);

            utilisateurService.desactiverUtilisateur(email);

            log.info("Successfully disabled auth for user: {}", email);
            return ResponseEntity.ok("User authentication disabled successfully");

        } catch (Exception e) {
            log.error("Failed to disable user authentication: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to disable user authentication: " + e.getMessage());
        }
    }

    /**
     * Health check for sync endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Sync endpoint is healthy");
    }
}
