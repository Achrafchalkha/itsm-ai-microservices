package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.TechnicianService;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.interfaces.dto.CreateTechnicianRequest;
import com.itsm.user.interfaces.dto.CreateTechnicianResponse;
import com.itsm.user.interfaces.dto.UpdateTechnicianRequest;
import com.itsm.user.interfaces.dto.UtilisateurDto;
import com.itsm.user.infrastructure.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Technician operations
 * Handles CRUD operations for technicians with async auth-service integration
 */
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Slf4j
public class TechnicianController {

    private final TechnicianService technicianService;
    private final SecurityService securityService;

    /**
     * Create a new technician (MANAGER only) - assigns to manager's team
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CreateTechnicianResponse> createTechnician(@Valid @RequestBody CreateTechnicianRequest request) {
        log.info("Creating technician: {}", request.getEmail());

        // Get current manager ID from security context
        UUID managerId = securityService.getCurrentUserId();
        if (managerId == null) {
            throw new IllegalArgumentException("Manager ID non trouvé dans le contexte de sécurité");
        }

        CreateTechnicianResponse response = technicianService.createTechnician(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all technicians (ADMIN or MANAGER)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UtilisateurDto>> getAllTechnicians() {
        log.info("Getting all technicians");
        
        List<Utilisateur> technicians = technicianService.getAllTechnicians();
        List<UtilisateurDto> technicianDtos = technicians.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(technicianDtos);
    }

    /**
     * Get technician by ID (ADMIN, MANAGER, or own profile)
     */
    @GetMapping("/{technicianId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('TECHNICIAN') and @securityService.isCurrentUser(#technicianId))")
    public ResponseEntity<UtilisateurDto> getTechnicianById(@PathVariable UUID technicianId) {
        log.info("Getting technician by ID: {}", technicianId);
        
        Utilisateur technician = technicianService.getTechnicianById(technicianId);
        return ResponseEntity.ok(toDto(technician));
    }

    /**
     * Update technician (MANAGER only) - supports all fields including password
     */
    @PutMapping("/{technicianId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UtilisateurDto> updateTechnician(
            @PathVariable UUID technicianId,
            @Valid @RequestBody UpdateTechnicianRequest request) {
        log.info("Updating technician: {} by MANAGER", technicianId);

        Utilisateur updatedTechnician = technicianService.updateTechnician(technicianId, request);
        return ResponseEntity.ok(toDto(updatedTechnician));
    }

    /**
     * Delete technician (ADMIN or MANAGER)
     */
    @DeleteMapping("/{technicianId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteTechnician(@PathVariable UUID technicianId) {
        log.info("Deleting technician: {}", technicianId);
        
        technicianService.deleteTechnician(technicianId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get technicians by team (MANAGER only)
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UtilisateurDto>> getTechniciansByTeam(@PathVariable UUID teamId) {
        log.info("Getting technicians for team: {}", teamId);
        
        List<Utilisateur> technicians = technicianService.getTechniciansByTeam(teamId);
        List<UtilisateurDto> technicianDtos = technicians.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(technicianDtos);
    }

    /**
     * Get available technicians (MANAGER only)
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UtilisateurDto>> getAvailableTechnicians() {
        log.info("Getting available technicians");
        
        List<Utilisateur> technicians = technicianService.getAvailableTechnicians();
        List<UtilisateurDto> technicianDtos = technicians.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(technicianDtos);
    }

    /**
     * Assign technician to team (MANAGER only)
     */
    @PutMapping("/{technicianId}/team/{teamId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UtilisateurDto> assignTechnicianToTeam(
            @PathVariable UUID technicianId,
            @PathVariable UUID teamId) {
        log.info("Assigning technician {} to team {}", technicianId, teamId);
        
        Utilisateur updatedTechnician = technicianService.assignTechnicianToTeam(technicianId, teamId);
        return ResponseEntity.ok(toDto(updatedTechnician));
    }

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
                .competencesJson(utilisateur.getCompetencesJson())
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
