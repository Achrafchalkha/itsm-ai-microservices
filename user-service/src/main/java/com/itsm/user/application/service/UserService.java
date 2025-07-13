package com.itsm.user.application.service;

import com.itsm.user.domain.model.Competence;
import com.itsm.user.domain.model.NiveauCompetence;
import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
import com.itsm.user.domain.model.User;
import com.itsm.user.domain.repository.UserRepository;
import com.itsm.user.infrastructure.kafka.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for User management
 * Orchestrates business operations and enforces business rules
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Create user profile from auth service event
     */
    public User creerProfilUtilisateurDepuisAuth(UserCreatedEvent event) {
        log.info("Creating user profile from auth service for userId: {}, email: {}", 
                event.getUserId(), event.getEmail());
        
        // Check if user already exists
        if (userRepository.existsById(event.getUserId())) {
            log.warn("User profile already exists for userId: {}", event.getUserId());
            return userRepository.findById(event.getUserId()).orElseThrow();
        }
        
        // Create user profile from auth event
        User user = User.creerDepuisAuthService(
                event.getUserId(),
                event.getNom(),
                event.getPrenom(),
                event.getEmail(),
                event.getRole(),
                event.getDateCreation()
        );
        
        User savedUser = userRepository.save(user);
        log.info("Created user profile for userId: {}, email: {}, role: {}", 
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        
        return savedUser;
    }
    
    /**
     * Get user profile by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> obtenirProfilUtilisateur(UUID userId) {
        log.debug("Getting user profile for userId: {}", userId);
        return userRepository.findById(userId);
    }
    
    /**
     * Get user profile by email
     */
    @Transactional(readOnly = true)
    public Optional<User> obtenirProfilParEmail(String email) {
        log.debug("Getting user profile for email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Update user profile
     */
    public User mettreAJourProfil(UUID userId, String nom, String prenom, String localisation) {
        log.info("Updating user profile for userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));
        
        // Update basic information
        user.setNom(nom);
        user.setPrenom(prenom);
        user.changerLocalisation(localisation);
        
        return userRepository.save(user);
    }
    
    /**
     * Assign user to team
     */
    public User assignerEquipe(UUID userId, UUID equipeId) {
        log.info("Assigning user {} to team {}", userId, equipeId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));
        
        user.assignerEquipe(equipeId);
        return userRepository.save(user);
    }
    
    /**
     * Change technician status
     */
    public User changerStatutTechnicien(UUID userId, StatutTechnicien nouveauStatut) {
        log.info("Changing technician status for userId: {} to {}", userId, nouveauStatut);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));
        
        user.changerStatut(nouveauStatut);
        return userRepository.save(user);
    }
    
    /**
     * Add competence to technician
     */
    public User ajouterCompetence(UUID userId, String nomCompetence, String description,
                                 String categorie, NiveauCompetence niveau) {
        log.info("Adding competence {} to user {}", nomCompetence, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));

        Competence competence = Competence.creerCompetence(nomCompetence, description, categorie, niveau);
        user.ajouterCompetence(competence);

        return userRepository.save(user);
    }

    /**
     * Add competence to technician (with numeric level for compatibility)
     */
    public User ajouterCompetence(UUID userId, String nomCompetence, String description,
                                 String categorie, int niveauExpertise) {
        NiveauCompetence niveau = convertNiveauFromInt(niveauExpertise);
        return ajouterCompetence(userId, nomCompetence, description, categorie, niveau);
    }

    /**
     * Convert numeric level to enum
     */
    private NiveauCompetence convertNiveauFromInt(int niveau) {
        switch (niveau) {
            case 1: return NiveauCompetence.JUNIOR;
            case 2: return NiveauCompetence.SENIOR;
            case 3: return NiveauCompetence.EXPERT;
            default: return NiveauCompetence.JUNIOR;
        }
    }
    
    /**
     * Remove competence from technician
     */
    public User supprimerCompetence(UUID userId, UUID competenceId) {
        log.info("Removing competence {} from user {}", competenceId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));
        
        user.supprimerCompetence(competenceId);
        return userRepository.save(user);
    }
    
    /**
     * Get all users by role
     */
    @Transactional(readOnly = true)
    public List<User> obtenirUtilisateursParRole(Role role) {
        log.debug("Getting users by role: {}", role);
        return userRepository.findByRole(role);
    }

    /**
     * Get all users (for debugging)
     */
    @Transactional(readOnly = true)
    public List<User> obtenirTousLesUtilisateurs() {
        log.debug("Getting all users");
        return userRepository.findAll();
    }

    /**
     * Delete user (for debugging/cleanup)
     */
    @Transactional
    public void supprimerUtilisateur(UUID userId) {
        log.info("Deleting user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Utilisateur non trouvé: " + userId);
        }

        userRepository.deleteById(userId);
    }
    
    /**
     * Get technicians by status
     */
    @Transactional(readOnly = true)
    public List<User> obtenirTechniciensParStatut(StatutTechnicien statut) {
        log.debug("Getting technicians by status: {}", statut);
        return userRepository.findTechniciensByStatut(statut);
    }
    
    /**
     * Get technicians by team
     */
    @Transactional(readOnly = true)
    public List<User> obtenirTechniciensParEquipe(UUID equipeId) {
        log.debug("Getting technicians by team: {}", equipeId);
        return userRepository.findTechniciensByEquipe(equipeId);
    }
    
    /**
     * Get technicians by location
     */
    @Transactional(readOnly = true)
    public List<User> obtenirTechniciensParLocalisation(String localisation) {
        log.debug("Getting technicians by location: {}", localisation);
        return userRepository.findTechniciensByLocalisation(localisation);
    }
}
