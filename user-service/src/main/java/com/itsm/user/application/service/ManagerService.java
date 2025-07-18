package com.itsm.user.application.service;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.repository.UtilisateurRepository;
import com.itsm.user.infrastructure.external.AuthServiceClient;
import com.itsm.user.interfaces.dto.CreateManagerRequest;
import com.itsm.user.interfaces.dto.CreateManagerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for Manager operations
 * Handles manager CRUD with async auth-service integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManagerService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthServiceClient authServiceClient;
    private final TeamService teamService;

    /**
     * Create a new manager with async auth-service integration
     */
    @Transactional
    public CreateManagerResponse createManager(CreateManagerRequest request) {
        log.info("Creating manager: {}", request.getEmail());

        // Check if user already exists
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        // Create manager profile in user-service
        Utilisateur manager = Utilisateur.builder()
                .id(UUID.randomUUID())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .role(Role.MANAGER)
                .localisation(request.getLocalisation())
                .telephone(request.getTelephone())
                .specialite(request.getSpecialite())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();

        Utilisateur savedManager = utilisateurRepository.save(manager);
        log.info("Manager profile created in user-service: {}", savedManager.getId());

        // Async call to auth-service to create authentication credentials
        try {
            authServiceClient.createManagerAuth(savedManager, request.getPassword(), request.getTeamName(), request.getTeamDescription());
            log.info("Manager authentication created in auth-service: {}", savedManager.getEmail());
        } catch (Exception e) {
            log.error("Failed to create manager authentication in auth-service: {}", e.getMessage());
            // Don't rollback - the profile is created, auth can be created later
        }

        return CreateManagerResponse.builder()
                .managerId(savedManager.getId())
                .email(savedManager.getEmail())
                .nom(savedManager.getNom())
                .prenom(savedManager.getPrenom())
                .role(savedManager.getRole())
                .message("Manager créé avec succès")
                .build();
    }

    /**
     * Get all managers
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> getAllManagers() {
        log.debug("Getting all managers");
        return utilisateurRepository.findByRoleAndActif(Role.MANAGER, true);
    }

    /**
     * Get manager by ID
     */
    @Transactional(readOnly = true)
    public Utilisateur getManagerById(UUID managerId) {
        log.debug("Getting manager by ID: {}", managerId);
        return utilisateurRepository.findById(managerId)
                .filter(u -> u.getRole() == Role.MANAGER)
                .orElseThrow(() -> new IllegalArgumentException("Manager non trouvé: " + managerId));
    }

    /**
     * Update manager
     */
    @Transactional
    public Utilisateur updateManager(UUID managerId, Utilisateur updatedManager) {
        log.info("Updating manager: {}", managerId);

        Utilisateur existingManager = getManagerById(managerId);
        
        // Update allowed fields
        existingManager.setNom(updatedManager.getNom());
        existingManager.setPrenom(updatedManager.getPrenom());
        existingManager.setLocalisation(updatedManager.getLocalisation());
        existingManager.setTelephone(updatedManager.getTelephone());
        existingManager.setSpecialite(updatedManager.getSpecialite());
        existingManager.setDateModification(LocalDateTime.now());

        return utilisateurRepository.save(existingManager);
    }

    /**
     * Delete manager
     */
    @Transactional
    public void deleteManager(UUID managerId) {
        log.info("Deleting manager: {}", managerId);

        Utilisateur manager = getManagerById(managerId);
        
        // Soft delete
        manager.setActif(false);
        manager.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(manager);

        // Async call to auth-service to disable authentication
        try {
            authServiceClient.disableUserAuth(manager.getEmail());
            log.info("Manager authentication disabled in auth-service: {}", manager.getEmail());
        } catch (Exception e) {
            log.error("Failed to disable manager authentication in auth-service: {}", e.getMessage());
        }
    }

    /**
     * Get manager's team
     */
    @Transactional(readOnly = true)
    public UUID getManagerTeam(UUID managerId) {
        log.debug("Getting team for manager: {}", managerId);
        
        Utilisateur manager = getManagerById(managerId);
        return manager.getTeamId();
    }

    /**
     * Get technicians managed by this manager
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> getManagedTechnicians(UUID managerId) {
        log.debug("Getting technicians for manager: {}", managerId);
        
        Utilisateur manager = getManagerById(managerId);
        if (manager.getTeamId() == null) {
            log.warn("Manager {} has no team assigned", managerId);
            return List.of();
        }

        return utilisateurRepository.findByTeamIdAndRole(manager.getTeamId(), Role.TECHNICIEN);
    }

    /**
     * Assign manager to team
     */
    @Transactional
    public Utilisateur assignManagerToTeam(UUID managerId, UUID teamId) {
        log.info("Assigning manager {} to team {}", managerId, teamId);
        
        Utilisateur manager = getManagerById(managerId);
        manager.setTeamId(teamId);
        manager.setDateModification(LocalDateTime.now());
        
        return utilisateurRepository.save(manager);
    }
}
