package com.itsm.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Team entities
 * Provides database access operations for the Team aggregate
 */
@Repository
public interface JpaTeamRepository extends JpaRepository<JpaTeamEntity, UUID> {
    
    /**
     * Find teams by manager
     */
    List<JpaTeamEntity> findByManagerId(UUID managerId);
    
    /**
     * Find teams that handle a specific category
     */
    @Query("SELECT DISTINCT t FROM JpaTeamEntity t JOIN t.categories c WHERE c = :category")
    List<JpaTeamEntity> findByCategory(@Param("category") String category);
    
    /**
     * Find active teams
     */
    List<JpaTeamEntity> findByActif(boolean actif);
    
    /**
     * Count teams by manager
     */
    long countByManagerId(UUID managerId);
}
