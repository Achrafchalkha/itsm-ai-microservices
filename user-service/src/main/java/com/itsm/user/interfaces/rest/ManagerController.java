package com.itsm.user.interfaces.rest;

import com.itsm.user.application.service.ManagerService;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.interfaces.dto.CreateManagerRequest;
import com.itsm.user.interfaces.dto.CreateManagerResponse;
import com.itsm.user.interfaces.dto.UtilisateurDto;
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
 * REST Controller for Manager operations
 * Handles CRUD operations for managers with async auth-service integration
 */
@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {

    private final ManagerService managerService;

    /**
     * Create a new manager (ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateManagerResponse> createManager(@Valid @RequestBody CreateManagerRequest request) {
        log.info("Creating manager: {}", request.getEmail());
        
        CreateManagerResponse response = managerService.createManager(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all managers (ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UtilisateurDto> getManagerById(@PathVariable UUID managerId) {
        log.info("Getting manager by ID: {}", managerId);
        
        Utilisateur manager = managerService.getManagerById(managerId);
        return ResponseEntity.ok(toDto(manager));
    }

    /**
     * Update manager (ADMIN or own profile)
     */
    @PutMapping("/{managerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isCurrentUser(#managerId))")
    public ResponseEntity<UtilisateurDto> updateManager(
            @PathVariable UUID managerId,
            @Valid @RequestBody UtilisateurDto managerDto) {
        log.info("Updating manager: {}", managerId);
        
        Utilisateur updatedManager = managerService.updateManager(managerId, fromDto(managerDto));
        return ResponseEntity.ok(toDto(updatedManager));
    }

    /**
     * Delete manager (ADMIN only)
     */
    @DeleteMapping("/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteManager(@PathVariable UUID managerId) {
        log.info("Deleting manager: {}", managerId);
        
        managerService.deleteManager(managerId);
        return ResponseEntity.noContent().build();
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
     * Get technicians managed by this manager
     */
    @GetMapping("/{managerId}/technicians")
    @PreAuthorize("hasRole('MANAGER') and @securityService.isCurrentUser(#managerId)")
    public ResponseEntity<List<UtilisateurDto>> getManagedTechnicians(@PathVariable UUID managerId) {
        log.info("Getting technicians for manager: {}", managerId);
        
        List<Utilisateur> technicians = managerService.getManagedTechnicians(managerId);
        List<UtilisateurDto> technicianDtos = technicians.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(technicianDtos);
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
