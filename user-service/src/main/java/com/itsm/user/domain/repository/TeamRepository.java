package com.itsm.user.domain.repository;

import com.itsm.user.domain.model.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Team aggregate
 * Defines the contract for team persistence operations
 */
public interface TeamRepository {
    
    /**
     * Save a team
     */
    Team save(Team team);
    
    /**
     * Find team by ID
     */
    Optional<Team> findById(UUID id);
    
    /**
     * Find all teams
     */
    List<Team> findAll();
    
    /**
     * Find teams by manager
     */
    List<Team> findByManagerId(UUID managerId);
    
    /**
     * Find teams that handle a specific category
     */
    List<Team> findByCategory(String category);
    
    /**
     * Find active teams
     */
    List<Team> findByActif(boolean actif);

    /**
     * Find all active teams (convenience method)
     */
    List<Team> findByActifTrue();

    /**
     * Check if team exists by ID
     */
    boolean existsById(UUID id);
    
    /**
     * Delete team by ID
     */
    void deleteById(UUID id);
    
    /**
     * Count teams by manager
     */
    long countByManagerId(UUID managerId);
}
