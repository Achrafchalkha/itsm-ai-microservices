package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.ManagerService;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.interfaces.dto.UtilisateurDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Manager profile operations
 * Handles manager-specific operations (non-admin endpoints)
 */
@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
@Slf4j
public class ManagerProfileController {

    private final ManagerService managerService;

    /**
     * Debug endpoint to test JWT authentication
     */
    @GetMapping("/debug-auth")
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", authentication != null && authentication.isAuthenticated());
        debug.put("principal", authentication != null ? authentication.getPrincipal() : null);
        debug.put("authorities", authentication != null ? authentication.getAuthorities() : null);
        debug.put("details", authentication != null ? authentication.getDetails() : null);
        
        log.info("Debug auth info: {}", debug);
        return ResponseEntity.ok(debug);
    }

    /**
     * Get manager by ID (ADMIN or MANAGER)
     */
    @GetMapping("/{managerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UtilisateurDto> getManagerById(@PathVariable UUID managerId) {
        log.info("Getting manager by ID: {}", managerId);
        
        Utilisateur manager = managerService.getManagerById(managerId);
        return ResponseEntity.ok(toDto(manager));
    }

    /**
     * Get manager's team (MANAGER only)
     */
    @GetMapping("/{managerId}/team")
    @PreAuthorize("hasRole('MANAGER') and @securityService.isCurrentUser(#managerId)")
    public ResponseEntity<UUID> getManagerTeam(@PathVariable UUID managerId) {
        log.info("Getting team for manager: {}", managerId);
        
        UUID teamId = managerService.getManagerTeam(managerId);
        return ResponseEntity.ok(teamId);
    }

    /**
     * Convert domain model to DTO
     */
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
}
