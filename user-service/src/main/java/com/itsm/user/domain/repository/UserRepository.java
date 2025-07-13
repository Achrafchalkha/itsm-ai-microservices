package com.itsm.user.domain.repository;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
import com.itsm.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for User aggregate
 * Defines the contract for user persistence operations
 */
public interface UserRepository {
    
    /**
     * Save a user
     */
    User save(User user);
    
    /**
     * Find user by ID
     */
    Optional<User> findById(UUID id);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find all users
     */
    List<User> findAll();
    
    /**
     * Find users by role
     */
    List<User> findByRole(Role role);
    
    /**
     * Find available technicians by status
     */
    List<User> findTechniciensByStatut(StatutTechnicien statut);
    
    /**
     * Find technicians by team
     */
    List<User> findTechniciensByEquipe(UUID equipeId);
    
    /**
     * Find technicians by location
     */
    List<User> findTechniciensByLocalisation(String localisation);
    
    /**
     * Find technicians by competence
     */
    List<User> findTechniciensByCompetence(String nomCompetence, int niveauMinimum);
    
    /**
     * Find available technicians with specific competence
     */
    List<User> findTechniciensDisponiblesAvecCompetence(String nomCompetence, int niveauMinimum);
    
    /**
     * Find available technicians in a specific location
     */
    List<User> findTechniciensDisponiblesDansLocalisation(String localisation);
    
    /**
     * Find available technicians in a team
     */
    List<User> findTechniciensDisponiblesDansEquipe(UUID equipeId);
    
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
     * Count technicians by status
     */
    long countTechniciensByStatut(StatutTechnicien statut);
}
