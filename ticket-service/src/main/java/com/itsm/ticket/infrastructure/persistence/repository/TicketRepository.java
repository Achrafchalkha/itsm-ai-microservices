package com.itsm.ticket.infrastructure.persistence.repository;

import com.itsm.ticket.domain.model.StatutTicket;
import com.itsm.ticket.infrastructure.persistence.entity.TicketEntity;
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
}
