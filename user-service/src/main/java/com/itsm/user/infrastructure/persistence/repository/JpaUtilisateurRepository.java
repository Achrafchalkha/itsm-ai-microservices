package com.itsm.user.infrastructure.persistence.repository;

import com.itsm.user.domain.model.Role;
import com.itsm.user.infrastructure.persistence.entity.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for UtilisateurEntity in user-service
 * Handles business profile operations in user_db
 */
@Repository
public interface JpaUtilisateurRepository extends JpaRepository<UtilisateurEntity, UUID> {
    
    Optional<UtilisateurEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsById(UUID id);
    
    List<UtilisateurEntity> findByRole(Role role);
    
    List<UtilisateurEntity> findByRoleAndActif(Role role, boolean actif);
    
    List<UtilisateurEntity> findByTeamId(UUID teamId);
    
    List<UtilisateurEntity> findByTeamIdAndRole(UUID teamId, Role role);
    
    @Query("SELECT u FROM UtilisateurEntity u WHERE u.role = :role AND u.actif = true")
    List<UtilisateurEntity> findActiveByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM UtilisateurEntity u WHERE u.teamId = :teamId AND u.role = 'TECHNICIEN' AND u.actif = true")
    List<UtilisateurEntity> findActiveTechniciansByTeam(@Param("teamId") UUID teamId);
    
    @Query("SELECT COUNT(u) FROM UtilisateurEntity u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM UtilisateurEntity u WHERE u.teamId = :teamId")
    long countByTeamId(@Param("teamId") UUID teamId);

    /**
     * Find technicians by team ordered by workload (least loaded first)
     */
    @Query("SELECT u FROM UtilisateurEntity u WHERE u.teamId = :teamId AND u.role = 'TECHNICIEN' AND u.actif = true ORDER BY u.chargeActuelle ASC")
    List<UtilisateurEntity> findTechniciansByTeamOrderByWorkload(@Param("teamId") UUID teamId);

    /**
     * Find available technicians (active with low workload)
     */
    @Query("SELECT u FROM UtilisateurEntity u WHERE u.role = 'TECHNICIEN' AND u.actif = true AND u.chargeActuelle < :maxWorkload ORDER BY u.chargeActuelle ASC")
    List<UtilisateurEntity> findAvailableTechnicians(@Param("maxWorkload") int maxWorkload);

    /**
     * Find technicians with specific competence and low workload
     */
    @Query("SELECT u FROM UtilisateurEntity u WHERE u.role = 'TECHNICIEN' AND u.actif = true AND u.competencesJson LIKE %:competence% ORDER BY u.chargeActuelle ASC")
    List<UtilisateurEntity> findTechniciansByCompetenceOrderByWorkload(@Param("competence") String competence);

    /**
     * Update workload for a technician
     */
    @Modifying
    @Query("UPDATE UtilisateurEntity u SET u.chargeActuelle = :workload, u.dateModification = CURRENT_TIMESTAMP WHERE u.id = :technicianId")
    void updateWorkload(@Param("technicianId") UUID technicianId, @Param("workload") int workload);
}
