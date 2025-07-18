package com.itsm.auth.application.service;

import com.itsm.auth.domain.model.Role;
import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.infrastructure.persistence.entity.UtilisateurEntity;
import com.itsm.auth.infrastructure.persistence.mapper.UtilisateurMapper;
import com.itsm.auth.infrastructure.persistence.repository.JpaUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing users in auth-service
 * Handles authentication-related user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UtilisateurService {

    private final JpaUtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create user with specific ID (for sync from user-service)
     * This ensures both services have the same ID for the same user
     */
    @Transactional
    public Utilisateur creerUtilisateurAvecId(Utilisateur utilisateur, String motDePasse) {
        log.info("Creating user with specific ID: {} for email: {}", 
                utilisateur.getId(), utilisateur.getEmail());

        // Check if user already exists
        if (existsByEmail(utilisateur.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + utilisateur.getEmail());
        }

        if (existsById(utilisateur.getId())) {
            throw new IllegalArgumentException("Un utilisateur avec cet ID existe déjà: " + utilisateur.getId());
        }

        // Hash password
        String motDePasseHashe = passwordEncoder.encode(motDePasse);
        
        // Set authentication-specific fields
        utilisateur.setMotDePasseHashe(motDePasseHashe);
        utilisateur.setDateCreation(LocalDateTime.now());
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateur.setActif(true);

        // Save to database
        UtilisateurEntity entity = utilisateurMapper.toEntity(utilisateur);
        UtilisateurEntity savedEntity = utilisateurRepository.save(entity);
        
        log.info("User created successfully with ID: {} for email: {}", 
                savedEntity.getId(), savedEntity.getEmail());
        
        return utilisateurMapper.toDomain(savedEntity);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<Utilisateur> trouverParEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return utilisateurRepository.findByEmail(email)
                .map(utilisateurMapper::toDomain);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<Utilisateur> trouverParId(UUID id) {
        log.debug("Finding user by ID: {}", id);
        return utilisateurRepository.findById(id)
                .map(utilisateurMapper::toDomain);
    }

    /**
     * Check if user exists by email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    /**
     * Check if user exists by ID
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return utilisateurRepository.existsById(id);
    }

    /**
     * Deactivate user (soft delete)
     */
    @Transactional
    public void desactiverUtilisateur(String email) {
        log.info("Deactivating user: {}", email);
        
        Optional<UtilisateurEntity> entityOpt = utilisateurRepository.findByEmail(email);
        if (entityOpt.isPresent()) {
            UtilisateurEntity entity = entityOpt.get();
            entity.setActif(false);
            entity.setDateModification(LocalDateTime.now());
            utilisateurRepository.save(entity);
            
            log.info("User deactivated successfully: {}", email);
        } else {
            log.warn("User not found for deactivation: {}", email);
            throw new IllegalArgumentException("Utilisateur non trouvé: " + email);
        }
    }

    /**
     * Update user in auth-service (for sync from user-service)
     */
    @Transactional
    public void mettreAJourUtilisateur(UUID id, String nom, String prenom, String email, Role role,
                                       boolean emailChanged, boolean passwordChanged, String newPassword) {
        log.info("Updating user in auth-service with ID: {}", id);

        Optional<UtilisateurEntity> entityOpt = utilisateurRepository.findById(id);
        if (entityOpt.isPresent()) {
            UtilisateurEntity entity = entityOpt.get();

            // Update basic fields
            entity.setNom(nom);
            entity.setPrenom(prenom);
            entity.setRole(role);
            entity.setDateModification(LocalDateTime.now());

            // Update email if changed
            if (emailChanged) {
                entity.setEmail(email);
            }

            // Update password if changed
            if (passwordChanged && newPassword != null && !newPassword.trim().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(newPassword);
                entity.setMotDePasseHashe(hashedPassword);
                log.info("Password updated for user: {}", email);
            }

            utilisateurRepository.save(entity);
            log.info("User updated successfully in auth-service: {}", id);
        } else {
            log.warn("User not found in auth-service for update: {}", id);
            throw new IllegalArgumentException("Utilisateur non trouvé dans auth-service: " + id);
        }
    }

    /**
     * Validate user credentials for login
     */
    @Transactional(readOnly = true)
    public boolean validerMotDePasse(String email, String motDePasse) {
        log.debug("Validating password for user: {}", email);

        Optional<UtilisateurEntity> entityOpt = utilisateurRepository.findByEmail(email);
        if (entityOpt.isPresent()) {
            UtilisateurEntity entity = entityOpt.get();
            if (entity.isActif()) {
                return passwordEncoder.matches(motDePasse, entity.getMotDePasseHashe());
            } else {
                log.warn("User account is inactive: {}", email);
                return false;
            }
        }

        log.warn("User not found: {}", email);
        return false;
    }
}
