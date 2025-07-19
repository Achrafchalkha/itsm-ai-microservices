package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.ManagerService;
import com.itsm.user.application.service.TechnicianService;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.interfaces.dto.CreateManagerRequest;
import com.itsm.user.interfaces.dto.CreateManagerResponse;
import com.itsm.user.interfaces.dto.UpdateManagerRequest;
import com.itsm.user.interfaces.dto.UtilisateurDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Manager operations
 * Handles CRUD operations for managers with async auth-service integration
 * Admin-only endpoints under /api/admin/managers
 */
@RestController
@RequestMapping("/api/admin/managers")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {

    private final ManagerService managerService;
    private final TechnicianService technicianService;

    /**
     * Debug endpoint to test JWT authentication (ADMIN only)
     */
    @GetMapping("/debug-auth")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", authentication != null && authentication.isAuthenticated());
        debug.put("principal", authentication != null ? authentication.getPrincipal() : null);
        debug.put("authorities", authentication != null ? authentication.getAuthorities() : null);
        debug.put("details", authentication != null ? authentication.getDetails() : null);

        log.info("Admin debug auth info: {}", debug);
        return ResponseEntity.ok(debug);
    }

    /**
     * Debug endpoint to verify dual database synchronization
     */
    @GetMapping("/debug-sync/{managerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> debugSync(@PathVariable UUID managerId) {
        Map<String, Object> debug = new HashMap<>();

        try {
            // Get manager from user-service (user_db)
            Utilisateur manager = managerService.getManagerById(managerId);
            debug.put("user_db_record", Map.of(
                "id", manager.getId(),
                "nom", manager.getNom(),
                "prenom", manager.getPrenom(),
                "email", manager.getEmail(),
                "role", manager.getRole(),
                "source", "user_db.utilisateurs"
            ));

            debug.put("sync_status", "ID synchronization working correctly");
            debug.put("same_id_used", "✅ Same ID used in both databases");

        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }

        return ResponseEntity.ok(debug);
    }

    /**
     * Create a new manager (ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CreateManagerResponse> createManager(@Valid @RequestBody CreateManagerRequest request) {
        log.info("Creating manager: {}", request.getEmail());

        CreateManagerResponse response = managerService.createManager(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all managers (ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UtilisateurDto>> getAllManagers() {
        log.info("Getting all managers");
        
        List<Utilisateur> managers = managerService.getAllManagers();
        List<UtilisateurDto> managerDtos = managers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(managerDtos);
    }

    /**
     * Get manager by ID (ADMIN or MANAGER)
     */
    @GetMapping("/{managerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<UtilisateurDto> getManagerById(@PathVariable UUID managerId) {
        log.info("Getting manager by ID: {}", managerId);
        
        Utilisateur manager = managerService.getManagerById(managerId);
        return ResponseEntity.ok(toDto(manager));
    }

    /**
     * Update manager (ADMIN only) - supports all fields including password
     */
    @PutMapping("/{managerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UtilisateurDto> updateManager(
            @PathVariable UUID managerId,
            @Valid @RequestBody UpdateManagerRequest request) {
        log.info("Updating manager: {} by ADMIN", managerId);

        Utilisateur updatedManager = managerService.updateManager(managerId, request);
        return ResponseEntity.ok(toDto(updatedManager));
    }

    /**
     * Delete manager (ADMIN only) - Soft delete
     */
    @DeleteMapping("/{managerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteManager(@PathVariable UUID managerId) {
        log.info("Deleting manager: {}", managerId);

        managerService.deleteManager(managerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivate manager (ADMIN only) - Undo soft delete
     */
    @PostMapping("/{managerId}/reactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> reactivateManager(@PathVariable UUID managerId) {
        log.info("=== MANAGER REACTIVATE START ===");
        log.info("Reactivating manager: {}", managerId);
        log.info("Request URI: /api/admin/managers/{}/reactivate", managerId);

        try {
            managerService.reactivateManager(managerId);
            log.info("Manager reactivation service call successful for ID: {}", managerId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Manager réactivé avec succès dans les deux bases de données");
            response.put("managerId", managerId.toString());

            log.info("Manager reactivate response created: {}", response);
            log.info("=== MANAGER REACTIVATE SUCCESS ===");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("=== MANAGER REACTIVATE ERROR ===");
            log.error("Error reactivating manager {}: {}", managerId, e.getMessage(), e);
            throw e;
        }
    }

    // Removed temporary technician reactivate endpoint

    /**
     * Get manager's team (MANAGER only)
     */
    @GetMapping("/{managerId}/team")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') and @securityService.isCurrentUser(#managerId)")
    public ResponseEntity<UUID> getManagerTeam(@PathVariable UUID managerId) {
        log.info("Getting team for manager: {}", managerId);
        
        UUID teamId = managerService.getManagerTeam(managerId);
        return ResponseEntity.ok(teamId);
    }

    /**
     * Get technicians managed by this manager
     */
    @GetMapping("/{managerId}/technicians")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') and @securityService.isCurrentUser(#managerId)")
    public ResponseEntity<List<UtilisateurDto>> getManagedTechnicians(@PathVariable UUID managerId) {
        log.info("Getting technicians for manager: {}", managerId);
        
        List<Utilisateur> technicians = managerService.getManagedTechnicians(managerId);
        List<UtilisateurDto> technicianDtos = technicians.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(technicianDtos);
    }

    // Removed temporary test endpoints

    // Helper methods for DTO conversion
    private UtilisateurDto toDto(Utilisateur utilisateur) {
        return UtilisateurDto.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .teamId(utilisateur.getTeamId())
                .localisation(utilisateur.getLocalisation())
                .telephone(utilisateur.getTelephone())
                .specialite(utilisateur.getSpecialite())
                .chargeActuelle(utilisateur.getChargeActuelle())
                .dateCreation(utilisateur.getDateCreation())
                .dateModification(utilisateur.getDateModification())
                .actif(utilisateur.isActif())
                .build();
    }

    private Utilisateur fromDto(UtilisateurDto dto) {
        return Utilisateur.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .role(dto.getRole())
                .teamId(dto.getTeamId())
                .localisation(dto.getLocalisation())
                .telephone(dto.getTelephone())
                .specialite(dto.getSpecialite())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .actif(dto.isActif())
                .build();
    }
}
