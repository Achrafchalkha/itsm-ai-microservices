package com.itsm.user.domain.repository;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.Utilisateur;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Utilisateur domain model
 * Defines operations for managing user business profiles
 */
public interface UtilisateurRepository {
    
    /**
     * Save a user
     */
    Utilisateur save(Utilisateur utilisateur);
    
    /**
     * Find user by ID
     */
    Optional<Utilisateur> findById(UUID id);
    
    /**
     * Find user by email
     */
    Optional<Utilisateur> findByEmail(String email);
    
    /**
     * Find all users
     */
    List<Utilisateur> findAll();
    
    /**
     * Find users by role
     */
    List<Utilisateur> findByRole(Role role);
    
    /**
     * Find users by role and active status
     */
    List<Utilisateur> findByRoleAndActif(Role role, boolean actif);
    
    /**
     * Find users by team ID
     */
    List<Utilisateur> findByTeamId(UUID teamId);
    
    /**
     * Find users by team ID and role
     */
    List<Utilisateur> findByTeamIdAndRole(UUID teamId, Role role);
    
    /**
     * Check if user exists by ID
     */
    boolean existsById(UUID id);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Delete user by ID
     */
    void deleteById(UUID id);
    
    /**
     * Count users by role
     */
    long countByRole(Role role);
    
    /**
     * Count users by team ID
     */
    long countByTeamId(UUID teamId);
}
