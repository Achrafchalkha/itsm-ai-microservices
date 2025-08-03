package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.TechnicianService;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.interfaces.dto.UtilisateurDto;
import com.itsm.user.infrastructure.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Technician Self-Service operations
 * Provides endpoints similar to manager endpoints but for technician's own data access
 * Pattern: /api/technician/technicians/{technicianId} where technicianId must be current user
 */
@RestController
@RequestMapping("/api/technician/technicians")
@RequiredArgsConstructor
@Slf4j
public class TechnicianSelfController {

    private final TechnicianService technicianService;
    private final SecurityService securityService;

    /**
     * Get technician's own profile by ID
     * Similar to GET /api/manager/technicians/{technicianId} but restricted to own profile
     * URL: GET /api/technician/technicians/{technicianId}
     */
    @GetMapping("/{technicianId}")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<UtilisateurDto> getTechnicianProfile(@PathVariable UUID technicianId) {
        UUID currentUserId = securityService.getCurrentUserId();
        
        // Security check: technician can only access their own profile
        if (!currentUserId.equals(technicianId)) {
            log.warn("Technician {} attempted to access profile of technician {}", currentUserId, technicianId);
            return ResponseEntity.status(403).build();
        }
        
        log.info("Technician {} requesting own profile via technicians/{} endpoint", currentUserId, technicianId);

        try {
            Utilisateur technician = technicianService.getTechnicianById(technicianId);
            UtilisateurDto dto = toDto(technician);
            
            log.debug("Returning profile for technician: {}", technicianId);
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Error getting profile for technician {}: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update technician's own profile
     * Similar to PUT /api/manager/technicians/{technicianId} but restricted to own profile
     * URL: PUT /api/technician/technicians/{technicianId}
     */
    @PutMapping("/{technicianId}")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<UtilisateurDto> updateTechnicianProfile(
            @PathVariable UUID technicianId,
            @RequestBody UtilisateurDto updateRequest) {
        
        UUID currentUserId = securityService.getCurrentUserId();
        
        // Security check: technician can only update their own profile
        if (!currentUserId.equals(technicianId)) {
            log.warn("Technician {} attempted to update profile of technician {}", currentUserId, technicianId);
            return ResponseEntity.status(403).build();
        }
        
        log.info("Technician {} updating own profile via technicians/{} endpoint", currentUserId, technicianId);

        try {
            Utilisateur technician = technicianService.getTechnicianById(technicianId);
            
            // Update allowed fields (technicians can update limited fields)
            if (updateRequest.getLocalisation() != null) {
                technician.setLocalisation(updateRequest.getLocalisation());
            }
            if (updateRequest.getTelephone() != null) {
                technician.setTelephone(updateRequest.getTelephone());
            }
            if (updateRequest.getSpecialite() != null) {
                technician.setSpecialite(updateRequest.getSpecialite());
            }
            // Note: Competences can only be updated by managers, not by technicians themselves
            
            // Save the updated technician
            Utilisateur updatedTechnician = technicianService.updateTechnicianProfile(technician);
            UtilisateurDto dto = toDto(updatedTechnician);
            
            log.info("Profile updated for technician: {}", technicianId);
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Error updating profile for technician {}: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert Utilisateur entity to DTO
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
                .competencesJson(utilisateur.getCompetencesJson())
                .chargeActuelle(utilisateur.getChargeActuelle())
                .dateCreation(utilisateur.getDateCreation())
                .dateModification(utilisateur.getDateModification())
                .actif(utilisateur.isActif())
                .build();
    }
}
