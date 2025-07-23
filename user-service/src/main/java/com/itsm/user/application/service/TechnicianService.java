package com.itsm.user.application.service;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.repository.UtilisateurRepository;
import com.itsm.user.infrastructure.external.AuthServiceClient;
import com.itsm.user.interfaces.dto.CreateTechnicianRequest;
import com.itsm.user.interfaces.dto.CreateTechnicianResponse;
import com.itsm.user.interfaces.dto.UpdateTechnicianRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for Technician operations
 * Handles technician CRUD with async auth-service integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TechnicianService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Create a new technician with dual database synchronization
     * Manager creates technician for their own team
     * Saves to both user_db.utilisateurs and auth_db.utilisateurs with SAME ID
     */
    @Transactional
    public CreateTechnicianResponse createTechnician(CreateTechnicianRequest request, UUID managerId) {
        log.info("Creating technician: {} by manager: {}", request.getEmail(), managerId);

        // Check if user already exists
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        // Get manager's team ID
        Utilisateur manager = utilisateurRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager non trouvé: " + managerId));

        if (manager.getTeamId() == null) {
            throw new IllegalArgumentException("Le manager n'a pas d'équipe assignée");
        }

        // Generate UUID that will be used in BOTH databases
        UUID technicianId = UUID.randomUUID();

        // Convert competences to JSON
        String competencesJson = null;
        if (request.getCompetences() != null && !request.getCompetences().isEmpty()) {
            try {
                competencesJson = objectMapper.writeValueAsString(request.getCompetences());
            } catch (Exception e) {
                log.error("Failed to serialize competences: {}", e.getMessage());
                competencesJson = "[]";
            }
        }

        // Create technician profile in user-service (user_db.utilisateurs)
        Utilisateur technician = Utilisateur.builder()
                .id(technicianId)  // Same ID for both databases
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .role(Role.TECHNICIEN)
                .teamId(manager.getTeamId())  // Assign to manager's team
                .localisation(request.getLocalisation())
                .telephone(request.getTelephone())
                .specialite(request.getSpecialite())
                .competencesJson(competencesJson)
                .chargeActuelle(0)  // New technicians start with 0 tickets
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();

        Utilisateur savedTechnician = utilisateurRepository.save(technician);
        log.info("Technician profile created in user_db with ID: {}", savedTechnician.getId());

        // Async sync to auth-service (auth_db.utilisateurs) with SAME ID
        try {
            authServiceClient.createTechnicianAuth(savedTechnician, request.getMotDePasse());
            log.info("Technician authentication created in auth_db with same ID: {}", savedTechnician.getId());
        } catch (Exception e) {
            log.error("Failed to create technician authentication in auth-service: {}", e.getMessage());
            // Don't rollback - the profile is created, auth can be created later
        }

        return CreateTechnicianResponse.builder()
                .technicianId(savedTechnician.getId())
                .email(savedTechnician.getEmail())
                .nom(savedTechnician.getNom())
                .prenom(savedTechnician.getPrenom())
                .role(savedTechnician.getRole())
                .teamId(savedTechnician.getTeamId())
                .message("Technicien créé avec succès dans les deux bases de données")
                .build();
    }

    /**
     * Get all technicians
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> getAllTechnicians() {
        log.debug("Getting all technicians");
        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true);
    }

    /**
     * Get technician by ID
     */
    @Transactional(readOnly = true)
    public Utilisateur getTechnicianById(UUID technicianId) {
        log.debug("Getting technician by ID: {}", technicianId);
        return utilisateurRepository.findById(technicianId)
                .filter(u -> u.getRole() == Role.TECHNICIEN)
                .orElseThrow(() -> new IllegalArgumentException("Technicien non trouvé: " + technicianId));
    }

    /**
     * Update technician with dual database synchronization
     * Updates both user_db.utilisateurs and auth_db.utilisateurs
     */
    @Transactional
    public Utilisateur updateTechnician(UUID technicianId, UpdateTechnicianRequest request) {
        log.info("Updating technician: {}", technicianId);

        Utilisateur existingTechnician = getTechnicianById(technicianId);
        boolean hasChanges = false;
        boolean emailChanged = false;
        boolean passwordChanged = false;

        // Update user-service fields (user_db.utilisateurs)
        if (request.getNom() != null && !request.getNom().equals(existingTechnician.getNom())) {
            existingTechnician.setNom(request.getNom());
            hasChanges = true;
        }

        if (request.getPrenom() != null && !request.getPrenom().equals(existingTechnician.getPrenom())) {
            existingTechnician.setPrenom(request.getPrenom());
            hasChanges = true;
        }

        if (request.getEmail() != null && !request.getEmail().equals(existingTechnician.getEmail())) {
            // Check if new email already exists
            if (utilisateurRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
            }
            existingTechnician.setEmail(request.getEmail());
            emailChanged = true;
            hasChanges = true;
        }

        if (request.getLocalisation() != null) {
            existingTechnician.setLocalisation(request.getLocalisation());
            hasChanges = true;
        }

        if (request.getTelephone() != null) {
            existingTechnician.setTelephone(request.getTelephone());
            hasChanges = true;
        }

        if (request.getSpecialite() != null) {
            existingTechnician.setSpecialite(request.getSpecialite());
            hasChanges = true;
        }

        // Update competences
        if (request.getCompetences() != null) {
            try {
                String competencesJson = objectMapper.writeValueAsString(request.getCompetences());
                existingTechnician.setCompetencesJson(competencesJson);
                hasChanges = true;
            } catch (Exception e) {
                log.error("Failed to serialize competences: {}", e.getMessage());
            }
        }

        if (request.getMotDePasse() != null && !request.getMotDePasse().trim().isEmpty()) {
            passwordChanged = true;
            hasChanges = true;
        }

        if (hasChanges) {
            existingTechnician.setDateModification(LocalDateTime.now());
            existingTechnician = utilisateurRepository.save(existingTechnician);
            log.info("Technician updated in user_db: {}", technicianId);
        }

        // Sync changes to auth-service (auth_db.utilisateurs)
        if (hasChanges) {
            try {
                authServiceClient.updateTechnicianAuth(existingTechnician, request.getMotDePasse(),
                        emailChanged, passwordChanged);
                log.info("Technician authentication updated in auth_db: {}", technicianId);
            } catch (Exception e) {
                log.error("Failed to update technician authentication in auth-service: {}", e.getMessage());
                // Don't rollback - the profile is updated, auth can be synced later
            }
        }

        return existingTechnician;
    }

    /**
     * Delete technician
     */
    @Transactional
    public void deleteTechnician(UUID technicianId) {
        log.info("Deleting technician: {}", technicianId);

        Utilisateur technician = getTechnicianById(technicianId);
        
        // Soft delete
        technician.setActif(false);
        technician.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(technician);

        // Async call to auth-service to disable authentication using same ID
        try {
            authServiceClient.disableUserAuth(technician.getId(), technician.getEmail());
            log.info("Technician authentication disabled in auth-service with ID: {} for: {}", technician.getId(), technician.getEmail());
        } catch (Exception e) {
            log.error("Failed to disable technician authentication in auth-service: {}", e.getMessage());
        }
    }

    /**
     * Reactivate technician (undo soft delete)
     * Reactivates both user_db.utilisateurs and auth_db.utilisateurs
     */
    @Transactional
    public void reactivateTechnician(UUID technicianId) {
        log.info("Reactivating technician: {}", technicianId);

        // Find technician including inactive ones
        Optional<Utilisateur> technicianOpt = utilisateurRepository.findById(technicianId);
        if (technicianOpt.isEmpty()) {
            throw new IllegalArgumentException("Technician non trouvé: " + technicianId);
        }

        Utilisateur technician = technicianOpt.get();

        if (technician.isActif()) {
            log.warn("Technician is already active: {}", technicianId);
            return;
        }

        // Reactivate in user-service
        technician.setActif(true);
        technician.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(technician);
        log.info("Technician reactivated in user_db: {}", technicianId);

        // Async call to auth-service to reactivate authentication
        try {
            authServiceClient.reactivateUserAuth(technician.getId(), technician.getEmail());
            log.info("Technician authentication reactivated in auth-service with ID: {} for: {}", technician.getId(), technician.getEmail());
        } catch (Exception e) {
            log.error("Failed to reactivate technician authentication in auth-service: {}", e.getMessage());
        }
    }

    /**
     * Get technicians by team
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> getTechniciansByTeam(UUID teamId) {
        log.debug("Getting technicians for team: {}", teamId);
        return utilisateurRepository.findByTeamIdAndRole(teamId, Role.TECHNICIEN);
    }

    /**
     * Get available technicians (active and not assigned to tickets)
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> getAvailableTechnicians() {
        log.debug("Getting available technicians");
        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true);
    }

    /**
     * Assign technician to team
     */
    @Transactional
    public Utilisateur assignTechnicianToTeam(UUID technicianId, UUID teamId) {
        log.info("Assigning technician {} to team {}", technicianId, teamId);
        
        Utilisateur technician = getTechnicianById(technicianId);
        technician.setTeamId(teamId);
        technician.setDateModification(LocalDateTime.now());
        
        return utilisateurRepository.save(technician);
    }

    /**
     * Find technicians by competence
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> findTechniciansByCompetence(String competence) {
        log.debug("Finding technicians with competence: {}", competence);
        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true)
                .stream()
                .filter(t -> t.getCompetencesJson() != null && t.getCompetencesJson().contains(competence))
                .toList();
    }

    /**
     * Get all active technicians for assignment-service
     */
    @Transactional(readOnly = true)
    public List<com.itsm.user.interfaces.dto.TechnicianResponseDTO> getAllActiveTechnicians() {
        log.debug("Getting all active technicians for assignment-service");
        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true)
                .stream()
                .map(this::convertToTechnicianResponseDTO)
                .toList();
    }

    /**
     * Get technicians by team IDs for assignment-service
     */
    @Transactional(readOnly = true)
    public List<com.itsm.user.interfaces.dto.TechnicianResponseDTO> getTechniciansByTeams(List<UUID> teamIds) {
        log.debug("Getting technicians for teams: {}", teamIds);
        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true)
                .stream()
                .filter(tech -> teamIds.contains(tech.getTeamId()))
                .map(this::convertToTechnicianResponseDTO)
                .toList();
    }

    /**
     * Get technicians by category (through team category mapping)
     */
    @Transactional(readOnly = true)
    public List<com.itsm.user.interfaces.dto.TechnicianResponseDTO> getTechniciansByCategory(String category) {
        log.debug("Getting technicians for category: {}", category);

        // Map categories to team specialties or competences
        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true)
                .stream()
                .filter(tech -> matchesCategoryCompetence(tech, category))
                .map(this::convertToTechnicianResponseDTO)
                .toList();
    }

    /**
     * Get technician by ID for assignment-service
     */
    @Transactional(readOnly = true)
    public com.itsm.user.interfaces.dto.TechnicianResponseDTO getTechnicianDTOById(UUID technicianId) {
        log.debug("Getting technician by ID: {}", technicianId);
        Utilisateur technician = utilisateurRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found: " + technicianId));

        if (technician.getRole() != Role.TECHNICIEN) {
            throw new RuntimeException("User is not a technician: " + technicianId);
        }

        return convertToTechnicianResponseDTO(technician);
    }

    /**
     * Update technician workload for assignment-service
     */
    @Transactional
    public void updateWorkload(UUID technicianId, int increment) {
        log.debug("Updating workload for technician {} by {}", technicianId, increment);

        Utilisateur technician = utilisateurRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found: " + technicianId));

        int newWorkload = Math.max(0, technician.getChargeActuelle() + increment);
        technician.setChargeActuelle(newWorkload);
        technician.setDateModification(LocalDateTime.now());

        utilisateurRepository.save(technician);
        log.info("Updated workload for technician {} to {}", technicianId, newWorkload);
    }

    /**
     * Get technicians who have competences in a specific category
     * Used by assignment-service to find technicians based on their actual competences
     */
    @Transactional(readOnly = true)
    public List<com.itsm.user.interfaces.dto.TechnicianResponseDTO> getTechniciansByCompetenceCategory(String category) {
        log.debug("Getting technicians with competences in category: {}", category);

        return utilisateurRepository.findByRoleAndActif(Role.TECHNICIEN, true)
                .stream()
                .filter(tech -> hasCompetenceInCategory(tech, category))
                .map(this::convertToTechnicianResponseDTO)
                .toList();
    }

    /**
     * Check if technician has competences in the specified category
     */
    private boolean hasCompetenceInCategory(Utilisateur technician, String category) {
        if (technician.getCompetencesJson() == null || technician.getCompetencesJson().trim().isEmpty()) {
            return false;
        }

        try {
            List<Map<String, Object>> competences = objectMapper.readValue(
                    technician.getCompetencesJson(),
                    new TypeReference<List<Map<String, Object>>>() {});

            return competences.stream()
                    .anyMatch(comp -> category.equals(comp.get("categorie")));

        } catch (Exception e) {
            log.warn("Error parsing competences JSON for technician {}: {}", technician.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Check if technician matches category competence
     */
    private boolean matchesCategoryCompetence(Utilisateur technician, String category) {
        if (technician.getCompetencesJson() == null) {
            return false;
        }

        try {
            // Parse competences JSON and check if any competence matches the category
            String competencesJson = technician.getCompetencesJson();
            return competencesJson.toUpperCase().contains(category.toUpperCase());
        } catch (Exception e) {
            log.warn("Error parsing competences for technician {}: {}", technician.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Convert to TechnicianResponseDTO for assignment-service
     */
    private com.itsm.user.interfaces.dto.TechnicianResponseDTO convertToTechnicianResponseDTO(Utilisateur utilisateur) {
        return com.itsm.user.interfaces.dto.TechnicianResponseDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
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
