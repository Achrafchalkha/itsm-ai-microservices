package com.itsm.user.application.service;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Team;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.repository.UtilisateurRepository;
import com.itsm.user.infrastructure.external.AuthServiceClient;
import com.itsm.user.interfaces.dto.CreateManagerRequest;
import com.itsm.user.interfaces.dto.CreateManagerResponse;
import com.itsm.user.interfaces.dto.UpdateManagerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
     * Create a new manager with dual database synchronization
     * Saves to both user_db.utilisateurs and auth_db.utilisateurs with SAME ID
     */
    @Transactional
    public CreateManagerResponse createManager(CreateManagerRequest request) {
        log.info("Creating manager: {}", request.getEmail());

        // Check if user already exists in user-service
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        // Generate UUID that will be used in BOTH databases
        UUID managerId = UUID.randomUUID();

        // Create manager profile in user-service (user_db.utilisateurs)
        Utilisateur manager = Utilisateur.builder()
                .id(managerId)  // Same ID for both databases
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .role(Role.MANAGER)
                .localisation(request.getLocalisation())
                .telephone(request.getTelephone())
                .specialite(request.getSpecialite())
                .chargeActuelle(0)  // Managers don't handle tickets directly
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();

        Utilisateur savedManager = utilisateurRepository.save(manager);
        log.info("Manager profile created in user_db with ID: {}", savedManager.getId());

        // Create team for the manager
        Team team = null;
        if (request.getTeamName() != null && !request.getTeamName().trim().isEmpty()) {
            team = teamService.creerEquipe(request.getTeamName(), request.getTeamDescription(), managerId);

            // Add categories to team if provided
            if (request.getTeamCategories() != null && !request.getTeamCategories().isEmpty()) {
                for (String category : request.getTeamCategories()) {
                    teamService.ajouterCategorieEquipe(team.getId(), category);
                }
            }

            // Update manager with team ID
            savedManager.setTeamId(team.getId());
            savedManager = utilisateurRepository.save(savedManager);
            log.info("Team created and assigned to manager: {}", team.getId());
        }

        // Sync to auth-service (auth_db.utilisateurs) with SAME ID
        try {
            authServiceClient.createManagerAuth(savedManager, request.getMotDePasse(),
                    request.getTeamName(), request.getTeamDescription());
            log.info("Manager authentication created in auth_db with same ID: {}", savedManager.getId());
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
                .message("Manager créé avec succès dans les deux bases de données")
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
     * Update manager with dual database synchronization
     * Updates both user_db.utilisateurs and auth_db.utilisateurs
     */
    @Transactional
    public Utilisateur updateManager(UUID managerId, UpdateManagerRequest request) {
        log.info("Updating manager: {}", managerId);

        Utilisateur existingManager = getManagerById(managerId);
        boolean hasChanges = false;
        boolean emailChanged = false;
        boolean passwordChanged = false;

        // Update user-service fields (user_db.utilisateurs)
        if (request.getNom() != null && !request.getNom().equals(existingManager.getNom())) {
            existingManager.setNom(request.getNom());
            hasChanges = true;
        }

        if (request.getPrenom() != null && !request.getPrenom().equals(existingManager.getPrenom())) {
            existingManager.setPrenom(request.getPrenom());
            hasChanges = true;
        }

        if (request.getEmail() != null && !request.getEmail().equals(existingManager.getEmail())) {
            // Check if new email already exists
            if (utilisateurRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
            }
            existingManager.setEmail(request.getEmail());
            emailChanged = true;
            hasChanges = true;
        }

        if (request.getLocalisation() != null) {
            existingManager.setLocalisation(request.getLocalisation());
            hasChanges = true;
        }

        if (request.getTelephone() != null) {
            existingManager.setTelephone(request.getTelephone());
            hasChanges = true;
        }

        if (request.getSpecialite() != null) {
            existingManager.setSpecialite(request.getSpecialite());
            hasChanges = true;
        }

        if (request.getMotDePasse() != null && !request.getMotDePasse().trim().isEmpty()) {
            passwordChanged = true;
            hasChanges = true;
        }

        if (hasChanges) {
            existingManager.setDateModification(LocalDateTime.now());
            existingManager = utilisateurRepository.save(existingManager);
            log.info("Manager updated in user_db: {}", managerId);
        }

        // Update team information if provided
        if (existingManager.getTeamId() != null &&
            (request.getTeamName() != null || request.getTeamDescription() != null || request.getTeamCategories() != null)) {

            updateManagerTeam(existingManager.getTeamId(), request);
        }

        // Sync changes to auth-service (auth_db.utilisateurs)
        if (hasChanges) {
            try {
                authServiceClient.updateManagerAuth(existingManager, request.getMotDePasse(),
                        emailChanged, passwordChanged);
                log.info("Manager authentication updated in auth_db: {}", managerId);
            } catch (Exception e) {
                log.error("Failed to update manager authentication in auth-service: {}", e.getMessage());
                // Don't rollback - the profile is updated, auth can be synced later
            }
        }

        return existingManager;
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

    /**
     * Update manager's team information
     */
    private void updateManagerTeam(UUID teamId, UpdateManagerRequest request) {
        try {
            Optional<Team> teamOpt = teamService.obtenirEquipe(teamId);
            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                boolean teamUpdated = false;

                // Update team name if provided
                if (request.getTeamName() != null && !request.getTeamName().equals(team.getNom())) {
                    team.setNom(request.getTeamName());
                    teamUpdated = true;
                }

                // Update team description if provided
                if (request.getTeamDescription() != null && !request.getTeamDescription().equals(team.getDescription())) {
                    team.setDescription(request.getTeamDescription());
                    teamUpdated = true;
                }

                if (teamUpdated) {
                    teamService.creerEquipe(team.getNom(), team.getDescription(), team.getManagerId());
                }

                // Update team categories if provided
                if (request.getTeamCategories() != null) {
                    // Clear existing categories and add new ones
                    team.getCategories().clear();
                    for (String category : request.getTeamCategories()) {
                        teamService.ajouterCategorieEquipe(teamId, category);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to update team for manager: {}", e.getMessage());
        }
    }
}
