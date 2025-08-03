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
 * REST Controller for Technician Profile operations
 * Handles technician's own profile management and competences
 */
@RestController
@RequestMapping("/api/technician")
@RequiredArgsConstructor
@Slf4j
public class TechnicianProfileController {

    private final TechnicianService technicianService;
    private final SecurityService securityService;

    /**
     * Get current technician's own profile with competences
     * Similar to GET /api/manager/technicians/{technicianId} but for self-access
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<UtilisateurDto> getMyProfile() {
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} requesting own profile", technicianId);

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
     * Get current technician's own detailed profile (alternative endpoint)
     * Equivalent to GET /api/manager/technicians/{technicianId} but for technician's own access
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<UtilisateurDto> getMyDetailedProfile() {
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} requesting own detailed profile via /me endpoint", technicianId);

        try {
            Utilisateur technician = technicianService.getTechnicianById(technicianId);
            UtilisateurDto dto = toDto(technician);

            log.debug("Returning detailed profile for technician: {}", technicianId);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            log.error("Error getting detailed profile for technician {}: {}", technicianId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update current technician's own profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<UtilisateurDto> updateMyProfile(@RequestBody UtilisateurDto updateRequest) {
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} updating own profile", technicianId);

        try {
            // For now, we'll implement basic profile update
            // You can extend this with a proper UpdateTechnicianProfileRequest DTO
            Utilisateur technician = technicianService.getTechnicianById(technicianId);
            
            // Update allowed fields (technicians can update their own basic info)
            if (updateRequest.getLocalisation() != null) {
                technician.setLocalisation(updateRequest.getLocalisation());
            }
            if (updateRequest.getTelephone() != null) {
                technician.setTelephone(updateRequest.getTelephone());
            }
            if (updateRequest.getSpecialite() != null) {
                technician.setSpecialite(updateRequest.getSpecialite());
            }
            
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
     * Update technician availability status
     * Note: Status management might be handled differently in this system
     */
    @PutMapping("/status")
    @PreAuthorize("hasRole('TECHNICIEN')")
    public ResponseEntity<UtilisateurDto> updateMyStatus(@RequestBody StatusUpdateRequest request) {
        UUID technicianId = securityService.getCurrentUserId();
        log.info("Technician {} updating status (feature not implemented yet)", technicianId);

        try {
            Utilisateur technician = technicianService.getTechnicianById(technicianId);
            // Note: Status update functionality would need to be implemented
            // when the Utilisateur entity is extended with status fields

            UtilisateurDto dto = toDto(technician);

            log.info("Status update requested for technician: {}", technicianId);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            log.error("Error updating status for technician {}: {}", technicianId, e.getMessage(), e);
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

    /**
     * Request DTO for status updates
     */
    public static class StatusUpdateRequest {
        private String statutTechnicien;

        public String getStatutTechnicien() {
            return statutTechnicien;
        }

        public void setStatutTechnicien(String statutTechnicien) {
            this.statutTechnicien = statutTechnicien;
        }
    }
}
