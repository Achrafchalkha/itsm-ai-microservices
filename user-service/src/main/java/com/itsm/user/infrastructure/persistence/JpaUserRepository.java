package com.itsm.user.infrastructure.persistence;

import com.itsm.user.domain.model.Role;
import com.itsm.user.domain.model.StatutTechnicien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for User entities
 * Provides database access operations for the User aggregate
 */
@Repository
public interface JpaUserRepository extends JpaRepository<JpaUserEntity, UUID> {
    
    /**
     * Find user by email
     */
    Optional<JpaUserEntity> findByEmail(String email);
    
    /**
     * Find users by role
     */
    List<JpaUserEntity> findByRole(Role role);
    
    /**
     * Find technicians by status
     */
    @Query("SELECT u FROM JpaUserEntity u WHERE u.role = 'TECHNICIEN' AND u.statutTechnicien = :statut")
    List<JpaUserEntity> findTechniciensByStatut(@Param("statut") StatutTechnicien statut);
    
    /**
     * Find technicians by team
     */
    @Query("SELECT u FROM JpaUserEntity u WHERE u.role = 'TECHNICIEN' AND u.teamId = :teamId")
    List<JpaUserEntity> findTechniciensByEquipe(@Param("teamId") UUID teamId);

    /**
     * Find technicians by manager
     */
    @Query("SELECT u FROM JpaUserEntity u WHERE u.role = 'TECHNICIEN' AND u.managerId = :managerId")
    List<JpaUserEntity> findTechniciensByManager(@Param("managerId") UUID managerId);
    
    /**
     * Find technicians by location
     */
    @Query("SELECT u FROM JpaUserEntity u WHERE u.role = 'TECHNICIEN' AND u.localisation = :localisation")
    List<JpaUserEntity> findTechniciensByLocalisation(@Param("localisation") String localisation);
    
    /**
     * Find technicians by competence
     */
    @Query("SELECT DISTINCT u FROM JpaUserEntity u " +
           "JOIN u.competences c " +
           "WHERE u.role = 'TECHNICIEN' " +
           "AND c.nom = :nomCompetence " +
           "AND c.niveau >= :niveauMinimum")
    List<JpaUserEntity> findTechniciensByCompetence(@Param("nomCompetence") String nomCompetence,
                                                   @Param("niveauMinimum") String niveauMinimum);
    
    /**
     * Find available technicians with specific competence
     */
    @Query("SELECT DISTINCT u FROM JpaUserEntity u " +
           "JOIN u.competences c " +
           "WHERE u.role = 'TECHNICIEN' " +
           "AND u.statutTechnicien = 'DISPONIBLE' " +
           "AND u.actif = true " +
           "AND c.nom = :nomCompetence " +
           "AND c.niveau >= :niveauMinimum")
    List<JpaUserEntity> findTechniciensDisponiblesAvecCompetence(@Param("nomCompetence") String nomCompetence,
                                                               @Param("niveauMinimum") String niveauMinimum);
    
    /**
     * Find available technicians in a specific location
     */
    @Query("SELECT u FROM JpaUserEntity u " +
           "WHERE u.role = 'TECHNICIEN' " +
           "AND u.statutTechnicien = 'DISPONIBLE' " +
           "AND u.actif = true " +
           "AND u.localisation = :localisation")
    List<JpaUserEntity> findTechniciensDisponiblesDansLocalisation(@Param("localisation") String localisation);
    
    /**
     * Find available technicians in a team
     */
    @Query("SELECT u FROM JpaUserEntity u " +
           "WHERE u.role = 'TECHNICIEN' " +
           "AND u.statutTechnicien = 'DISPONIBLE' " +
           "AND u.actif = true " +
           "AND u.teamId = :teamId")
    List<JpaUserEntity> findTechniciensDisponiblesDansEquipe(@Param("teamId") UUID teamId);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Count users by role
     */
    long countByRole(Role role);
    
    /**
     * Count technicians by status
     */
    @Query("SELECT COUNT(u) FROM JpaUserEntity u WHERE u.role = 'TECHNICIEN' AND u.statutTechnicien = :statut")
    long countTechniciensByStatut(@Param("statut") StatutTechnicien statut);
}
