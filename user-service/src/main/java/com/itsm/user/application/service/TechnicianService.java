package com.itsm.user.application.service;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Utilisateur;
import com.itsm.user.domain.repository.UtilisateurRepository;
import com.itsm.user.infrastructure.external.AuthServiceClient;
import com.itsm.user.interfaces.dto.CreateTechnicianRequest;
import com.itsm.user.interfaces.dto.CreateTechnicianResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Create a new technician with async auth-service integration
     */
    @Transactional
    public CreateTechnicianResponse createTechnician(CreateTechnicianRequest request) {
        log.info("Creating technician: {}", request.getEmail());

        // Check if user already exists
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        // Create technician profile in user-service
        Utilisateur technician = Utilisateur.builder()
                .id(UUID.randomUUID())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .role(Role.TECHNICIEN)
                .teamId(request.getTeamId())
                .localisation(request.getLocalisation())
                .telephone(request.getTelephone())
                .specialite(request.getSpecialite())
                .competencesJson(request.getCompetencesJson())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();

        Utilisateur savedTechnician = utilisateurRepository.save(technician);
        log.info("Technician profile created in user-service: {}", savedTechnician.getId());

        // Async call to auth-service to create authentication credentials
        try {
            authServiceClient.createTechnicianAuth(savedTechnician, request.getPassword());
            log.info("Technician authentication created in auth-service: {}", savedTechnician.getEmail());
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
                .message("Technicien créé avec succès")
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
     * Update technician
     */
    @Transactional
    public Utilisateur updateTechnician(UUID technicianId, Utilisateur updatedTechnician) {
        log.info("Updating technician: {}", technicianId);

        Utilisateur existingTechnician = getTechnicianById(technicianId);
        
        // Update allowed fields
        existingTechnician.setNom(updatedTechnician.getNom());
        existingTechnician.setPrenom(updatedTechnician.getPrenom());
        existingTechnician.setLocalisation(updatedTechnician.getLocalisation());
        existingTechnician.setTelephone(updatedTechnician.getTelephone());
        existingTechnician.setSpecialite(updatedTechnician.getSpecialite());
        existingTechnician.setCompetencesJson(updatedTechnician.getCompetencesJson());
        existingTechnician.setDateModification(LocalDateTime.now());

        return utilisateurRepository.save(existingTechnician);
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

        // Async call to auth-service to disable authentication
        try {
            authServiceClient.disableUserAuth(technician.getEmail());
            log.info("Technician authentication disabled in auth-service: {}", technician.getEmail());
        } catch (Exception e) {
            log.error("Failed to disable technician authentication in auth-service: {}", e.getMessage());
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
}
