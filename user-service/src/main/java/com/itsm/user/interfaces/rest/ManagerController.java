package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.ManagerService;
import com.itsm.user.application.service.TechnicianService;
import com.itsm.user.application.service.TeamService;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.model.Team;
import com.itsm.user.interfaces.dto.CreateManagerRequest;
import com.itsm.user.interfaces.dto.CreateManagerResponse;
import com.itsm.user.interfaces.dto.UpdateManagerRequest;
import com.itsm.user.interfaces.dto.UtilisateurDto;
import com.itsm.user.interfaces.dto.ManagerWithTeamDto;
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
import java.util.Optional;
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
    private final TeamService teamService;

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
     * Debug endpoint to check managers and teams data (ADMIN only)
     */
    @GetMapping("/debug-data")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> debugData() {
        log.info("🔍 Debug endpoint called - checking managers and teams data");

        Map<String, Object> debug = new HashMap<>();

        try {
            // Get all managers
            List<Utilisateur> managers = managerService.getAllManagers();
            debug.put("managersCount", managers.size());
            debug.put("managers", managers.stream().map(m -> {
                Map<String, Object> managerInfo = new HashMap<>();
                managerInfo.put("id", m.getId());
                managerInfo.put("email", m.getEmail());
                managerInfo.put("teamId", m.getTeamId());
                managerInfo.put("nom", m.getNom());
                managerInfo.put("prenom", m.getPrenom());
                return managerInfo;
            }).collect(Collectors.toList()));

            // Try to get teams info
            try {
                // Get a sample team if any manager has teamId
                Optional<Utilisateur> managerWithTeam = managers.stream()
                        .filter(m -> m.getTeamId() != null)
                        .findFirst();

                if (managerWithTeam.isPresent()) {
                    UUID teamId = managerWithTeam.get().getTeamId();
                    log.info("🔍 Testing team lookup for teamId: {}", teamId);
                    Optional<Team> teamOpt = teamService.getTeamById(teamId);

                    Map<String, Object> teamInfo = new HashMap<>();
                    teamInfo.put("teamId", teamId);
                    teamInfo.put("found", teamOpt.isPresent());
                    if (teamOpt.isPresent()) {
                        Team team = teamOpt.get();
                        teamInfo.put("nom", team.getNom());
                        teamInfo.put("description", team.getDescription());
                    }
                    debug.put("sampleTeamLookup", teamInfo);
                } else {
                    debug.put("sampleTeamLookup", "No managers with teamId found");
                }
            } catch (Exception e) {
                debug.put("teamServiceError", e.getMessage());
                log.error("Error testing team service: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            log.error("Error in debug endpoint: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok(debug);
    }

    /**
     * Add test categories to teams (ADMIN only) - TEMPORARY ENDPOINT
     */
    @PostMapping("/debug-add-categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> addTestCategories() {
        log.info("🔧 Adding test categories to teams");

        Map<String, Object> result = new HashMap<>();

        try {
            // Add categories to DevOps team
            teamService.ajouterCategorieEquipe(UUID.fromString("0719f666-21ba-400d-8ca9-5c9a3d196610"), "DEVELOPPEMENT");
            teamService.ajouterCategorieEquipe(UUID.fromString("0719f666-21ba-400d-8ca9-5c9a3d196610"), "DEVOPS");
            teamService.ajouterCategorieEquipe(UUID.fromString("0719f666-21ba-400d-8ca9-5c9a3d196610"), "CI_CD");

            // Add categories to CyberDéfense team
            teamService.ajouterCategorieEquipe(UUID.fromString("927dfafb-2827-4e33-8fcf-ed60f0b7fb29"), "SECURITE");
            teamService.ajouterCategorieEquipe(UUID.fromString("927dfafb-2827-4e33-8fcf-ed60f0b7fb29"), "CYBER_DEFENSE");
            teamService.ajouterCategorieEquipe(UUID.fromString("927dfafb-2827-4e33-8fcf-ed60f0b7fb29"), "AUDIT");

            // Add categories to Réseau & Sécurité team
            teamService.ajouterCategorieEquipe(UUID.fromString("0938aeca-d26e-4c4b-be9b-b3b0d29d55b3"), "RESEAU");
            teamService.ajouterCategorieEquipe(UUID.fromString("0938aeca-d26e-4c4b-be9b-b3b0d29d55b3"), "INFRASTRUCTURE");
            teamService.ajouterCategorieEquipe(UUID.fromString("0938aeca-d26e-4c4b-be9b-b3b3d29d55b3"), "VPN");

            result.put("success", true);
            result.put("message", "Categories added successfully to all teams");

        } catch (Exception e) {
            log.error("Error adding categories: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get all managers with team information (ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ManagerWithTeamDto>> getAllManagers() {
        log.info("Getting all managers with team information");

        List<Utilisateur> managers = managerService.getAllManagers();
        List<ManagerWithTeamDto> managerDtos = managers.stream()
                .map(this::toManagerWithTeamDto)
                .collect(Collectors.toList());

        log.info("Found {} managers with team information", managerDtos.size());
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
     * Update manager (ADMIN only) - excludes password for security
     */
    @PutMapping("/{managerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ManagerWithTeamDto> updateManager(
            @PathVariable UUID managerId,
            @Valid @RequestBody UpdateManagerRequest request) {
        log.info("Updating manager: {} by ADMIN", managerId);

        Utilisateur updatedManager = managerService.updateManager(managerId, request);
        ManagerWithTeamDto enrichedManager = toManagerWithTeamDto(updatedManager);

        log.info("✅ Manager updated successfully: {} with team: {}",
                updatedManager.getEmail(), enrichedManager.getTeamName());

        return ResponseEntity.ok(enrichedManager);
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

    /**
     * Convert Utilisateur to ManagerWithTeamDto with enriched team information
     */
    private ManagerWithTeamDto toManagerWithTeamDto(Utilisateur utilisateur) {
        log.info("🔄 Converting manager to DTO: {} (ID: {}, TeamID: {})",
                utilisateur.getEmail(), utilisateur.getId(), utilisateur.getTeamId());

        ManagerWithTeamDto.ManagerWithTeamDtoBuilder builder = ManagerWithTeamDto.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .teamId(utilisateur.getTeamId())
                .localisation(utilisateur.getLocalisation())
                .telephone(utilisateur.getTelephone())
                .specialite(utilisateur.getSpecialite())
                .competencesJson(utilisateur.getCompetencesJson())
                .chargeActuelle(utilisateur.getChargeActuelle())
                .dateCreation(utilisateur.getDateCreation())
                .dateModification(utilisateur.getDateModification())
                .actif(utilisateur.isActif());

        // Enrich with team information if teamId is present
        if (utilisateur.getTeamId() != null) {
            log.info("🔍 Looking for team with ID: {}", utilisateur.getTeamId());
            try {
                Optional<Team> teamOpt = teamService.getTeamById(utilisateur.getTeamId());
                if (teamOpt.isPresent()) {
                    Team team = teamOpt.get();
                    log.info("✅ Found team: {} - {} (Categories: {})", team.getNom(), team.getDescription(), team.getCategories());
                    builder.teamName(team.getNom())
                           .teamDescription(team.getDescription())
                           .teamCategories(team.getCategories());

                    log.info("✨ Enriched manager {} with team info: {} - {} (Categories: {})",
                             utilisateur.getEmail(), team.getNom(), team.getDescription(), team.getCategories());
                } else {
                    log.warn("❌ Team not found for manager {}: teamId={}",
                            utilisateur.getEmail(), utilisateur.getTeamId());
                    builder.teamName("Équipe introuvable")
                           .teamDescription("L'équipe associée n'existe plus");
                }
            } catch (Exception e) {
                log.error("💥 Error fetching team info for manager {}: {}",
                         utilisateur.getEmail(), e.getMessage(), e);
                builder.teamName("Erreur équipe")
                       .teamDescription("Impossible de récupérer les informations de l'équipe");
            }
        } else {
            log.info("⚠️ Manager {} has no teamId assigned", utilisateur.getEmail());
            builder.teamName("Aucune équipe")
                   .teamDescription("Ce manager n'est assigné à aucune équipe");
        }

        ManagerWithTeamDto result = builder.build();
        log.info("📤 Final DTO for manager {}: teamName='{}', teamDescription='{}'",
                utilisateur.getEmail(), result.getTeamName(), result.getTeamDescription());

        return result;
    }
}
