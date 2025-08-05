package com.itsm.ticket.infrastructure.persistence.repository;

import com.itsm.ticket.domain.model.PrioriteTicket;
import com.itsm.ticket.domain.model.StatutTicket;
import com.itsm.ticket.infrastructure.persistence.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Ticket entities
 */
@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {
    
    /**
     * Find tickets by user ID (active only)
     */
    List<TicketEntity> findByUtilisateurIdAndActifTrueOrderByDateCreationDesc(UUID utilisateurId);
    
    /**
     * Find tickets by technician ID (custom query for better control)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId = :technicienId AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByTechnicienIdAndActifTrueOrderByDateCreationDesc(@Param("technicienId") UUID technicienId);

    /**
     * Find tickets by technician ID and status (custom query)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId = :technicienId AND t.statut = :statut AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByTechnicienIdAndStatutAndActifTrueOrderByDateCreationDesc(@Param("technicienId") UUID technicienId, @Param("statut") StatutTicket statut);
    
    /**
     * Find tickets by team ID
     */
    List<TicketEntity> findByTeamIdAndActifTrueOrderByDateCreationDesc(UUID teamId);
    
    /**
     * Find tickets by category
     */
    List<TicketEntity> findByCategorieAndActifTrueOrderByDateCreationDesc(String categorie);
    
    /**
     * Find tickets by status
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.statut = :statut AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByStatut(@Param("statut") String statut);
    
    /**
     * Find tickets by priority
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.priorite = :priorite AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByPriorite(@Param("priorite") String priorite);
    
    /**
     * Find unassigned tickets
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId IS NULL AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findUnassignedTickets();
    
    /**
     * Find tickets created between dates
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.dateCreation BETWEEN :startDate AND :endDate AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByDateCreationBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count tickets by status
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.statut = :statut AND t.actif = true")
    long countByStatut(@Param("statut") String statut);
    
    /**
     * Count tickets by user
     */
    long countByUtilisateurIdAndActifTrue(UUID utilisateurId);
    
    /**
     * Count tickets by technician
     */
    long countByTechnicienIdAndActifTrue(UUID technicienId);

    // ===== MANAGER TEAM METHODS =====

    /**
     * Find tickets by technician IDs (for team queries) with pagination
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.actif = true")
    Page<TicketEntity> findByTechnicienIdIn(@Param("technicienIds") List<UUID> technicienIds, Pageable pageable);

    /**
     * Find tickets by technician IDs and status with pagination
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.statut = :statut AND t.actif = true")
    Page<TicketEntity> findByTechnicienIdInAndStatut(@Param("technicienIds") List<UUID> technicienIds,
                                                     @Param("statut") StatutTicket statut, Pageable pageable);

    /**
     * Find tickets by technician IDs and priority with pagination
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.priorite = :priorite AND t.actif = true")
    Page<TicketEntity> findByTechnicienIdInAndPriorite(@Param("technicienIds") List<UUID> technicienIds,
                                                       @Param("priorite") PrioriteTicket priorite, Pageable pageable);

    /**
     * Find tickets by technician IDs, status and priority with pagination
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.statut = :statut AND t.priorite = :priorite AND t.actif = true")
    Page<TicketEntity> findByTechnicienIdInAndStatutAndPriorite(@Param("technicienIds") List<UUID> technicienIds,
                                                               @Param("statut") StatutTicket statut,
                                                               @Param("priorite") PrioriteTicket priorite, Pageable pageable);

    /**
     * Find tickets by technician IDs and status (without pagination)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.statut = :statut AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByTechnicienIdInAndStatut(@Param("technicienIds") List<UUID> technicienIds,
                                                     @Param("statut") StatutTicket statut);

    // ===== COUNTING METHODS FOR TEAM STATISTICS =====

    /**
     * Count tickets by technician IDs
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.actif = true")
    long countByTechnicienIdIn(@Param("technicienIds") List<UUID> technicienIds);

    /**
     * Count tickets by technician IDs and status
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.technicienId IN :technicienIds AND t.statut = :statut AND t.actif = true")
    long countByTechnicienIdInAndStatut(@Param("technicienIds") List<UUID> technicienIds, @Param("statut") StatutTicket statut);

    /**
     * Count tickets by single technician ID
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.technicienId = :technicienId AND t.actif = true")
    long countByTechnicienId(@Param("technicienId") UUID technicienId);

    /**
     * Count tickets by single technician ID and status
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.technicienId = :technicienId AND t.statut = :statut AND t.actif = true")
    long countByTechnicienIdAndStatut(@Param("technicienId") UUID technicienId, @Param("statut") StatutTicket statut);

    // ===== MISSING METHODS FOR MANAGER OPERATIONS =====

    /**
     * Find tickets by status and priority with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.statut = :statut AND t.priorite = :priorite AND t.actif = true")
    Page<TicketEntity> findByStatutAndPrioriteAndActifTrue(@Param("statut") StatutTicket statut,
                                                          @Param("priorite") PrioriteTicket priorite,
                                                          Pageable pageable);

    /**
     * Find tickets by status with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.statut = :statut AND t.actif = true")
    Page<TicketEntity> findByStatutAndActifTrue(@Param("statut") StatutTicket statut, Pageable pageable);

    /**
     * Find tickets by priority with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.priorite = :priorite AND t.actif = true")
    Page<TicketEntity> findByPrioriteAndActifTrue(@Param("priorite") PrioriteTicket priorite, Pageable pageable);

    /**
     * Find all active tickets with pagination
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.actif = true")
    Page<TicketEntity> findByActifTrue(Pageable pageable);

    /**
     * Find tickets by status (active only) ordered by creation date
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.statut = :statut AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByStatutAndActifTrueOrderByDateCreationDesc(@Param("statut") StatutTicket statut);

    /**
     * Find all active tickets ordered by creation date
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByActifTrueOrderByDateCreationDesc();

    /**
     * Count all active tickets
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.actif = true")
    long countByActifTrue();

    /**
     * Count tickets by status (active only)
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.statut = :statut AND t.actif = true")
    long countByStatutAndActifTrue(@Param("statut") StatutTicket statut);

    // ===== TEAM-BASED METHODS FOR MANAGER OPERATIONS =====

    /**
     * Find tickets by team ID with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.teamId = :teamId AND t.actif = true")
    Page<TicketEntity> findByTeamIdAndActifTrue(@Param("teamId") UUID teamId, Pageable pageable);

    /**
     * Find tickets by team ID and status with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.teamId = :teamId AND t.statut = :statut AND t.actif = true")
    Page<TicketEntity> findByTeamIdAndStatutAndActifTrue(@Param("teamId") UUID teamId,
                                                        @Param("statut") StatutTicket statut,
                                                        Pageable pageable);

    /**
     * Find tickets by team ID and priority with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.teamId = :teamId AND t.priorite = :priorite AND t.actif = true")
    Page<TicketEntity> findByTeamIdAndPrioriteAndActifTrue(@Param("teamId") UUID teamId,
                                                          @Param("priorite") PrioriteTicket priorite,
                                                          Pageable pageable);

    /**
     * Find tickets by team ID, status and priority with pagination (active only)
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.teamId = :teamId AND t.statut = :statut AND t.priorite = :priorite AND t.actif = true")
    Page<TicketEntity> findByTeamIdAndStatutAndPrioriteAndActifTrue(@Param("teamId") UUID teamId,
                                                                   @Param("statut") StatutTicket statut,
                                                                   @Param("priorite") PrioriteTicket priorite,
                                                                   Pageable pageable);

    /**
     * Find tickets by team ID and status (active only) ordered by creation date
     */
    @Query("SELECT t FROM TicketEntity t WHERE t.teamId = :teamId AND t.statut = :statut AND t.actif = true ORDER BY t.dateCreation DESC")
    List<TicketEntity> findByTeamIdAndStatutAndActifTrueOrderByDateCreationDesc(@Param("teamId") UUID teamId,
                                                                               @Param("statut") StatutTicket statut);

    /**
     * Count tickets by team ID (active only)
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.teamId = :teamId AND t.actif = true")
    long countByTeamIdAndActifTrue(@Param("teamId") UUID teamId);

    /**
     * Count tickets by team ID and status (active only)
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.teamId = :teamId AND t.statut = :statut AND t.actif = true")
    long countByTeamIdAndStatutAndActifTrue(@Param("teamId") UUID teamId, @Param("statut") StatutTicket statut);
}
