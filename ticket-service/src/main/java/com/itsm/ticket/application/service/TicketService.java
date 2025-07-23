package com.itsm.ticket.application.service;

import com.itsm.ticket.domain.model.PrioriteTicket;
import com.itsm.ticket.domain.model.StatutTicket;
import com.itsm.ticket.domain.model.Ticket;
import com.itsm.ticket.infrastructure.kafka.TicketEventPublisher;
import com.itsm.ticket.infrastructure.kafka.event.TicketCreatedEvent;
import com.itsm.ticket.infrastructure.persistence.entity.TicketEntity;
import com.itsm.ticket.infrastructure.persistence.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for ticket operations
 * Handles business logic for ticket management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    
    private final TicketRepository ticketRepository;
    private final TicketEventPublisher eventPublisher;
    
    /**
     * Create a new ticket
     */
    @Transactional
    public Ticket createTicket(String titre, String description, PrioriteTicket priorite,
                              String categorie, UUID utilisateurId, String utilisateurEmail, boolean enableNlp) {
        
        log.info("Creating ticket for user: {}", utilisateurId);
        
        // Create domain object
        Ticket ticket = Ticket.creerTicket(titre, description, priorite, categorie, utilisateurId);
        ticket.setEnableNlp(enableNlp);
        
        // Convert to entity and save
        TicketEntity ticketEntity = convertToEntity(ticket);
        ticketEntity = ticketRepository.save(ticketEntity);
        
        // Convert back to domain object
        ticket = convertToDomain(ticketEntity);
        
        // Publish event for assignment-service
        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .ticketId(ticket.getId())
                .titre(ticket.getTitre())
                .description(ticket.getDescription())
                .categorie(ticket.getCategorie())
                .priorite(ticket.getPriorite().name())
                .statut(ticket.getStatut().name())
                .utilisateurId(ticket.getUtilisateurId())
                .utilisateurEmail(utilisateurEmail)
                .enableNlp(ticket.getEnableNlp())
                .dateCreation(ticket.getDateCreation())
                .nlpConfidence(enableNlp ? 0.8 : null)
                .nlpReasoning(enableNlp ? "Analyse NLP activée" : null)
                .requiredSkills(determineRequiredSkills(categorie))
                .urgencyLevel(determineUrgencyLevel(priorite))
                .build();
        
        eventPublisher.publishTicketCreated(event);
        
        log.info("Successfully created ticket: {}", ticket.getId());
        return ticket;
    }
    
    /**
     * Get ticket by ID
     */
    public Ticket getTicketById(UUID ticketId) {
        TicketEntity entity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        return convertToDomain(entity);
    }

    /**
     * Get all tickets (for debugging)
     */
    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        List<TicketEntity> entities = ticketRepository.findAll();
        return entities.stream()
                .map(this::convertToDomain)
                .toList();
    }
    
    /**
     * Get tickets by user
     */
    public List<Ticket> getTicketsByUser(UUID utilisateurId) {
        List<TicketEntity> entities = ticketRepository.findByUtilisateurIdAndActifTrueOrderByDateCreationDesc(utilisateurId);
        
        return entities.stream()
                .map(this::convertToDomain)
                .toList();
    }
    
    /**
     * Update ticket assignment
     */
    @Transactional
    public void updateTicketAssignment(UUID ticketId, UUID technicienId, UUID teamId) {
        TicketEntity entity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        
        Ticket ticket = convertToDomain(entity);
        ticket.assignerTechnicien(technicienId, teamId);
        
        TicketEntity updatedEntity = convertToEntity(ticket);
        ticketRepository.save(updatedEntity);
        
        log.info("Updated ticket {} assignment to technician {}", ticketId, technicienId);
    }
    
    /**
     * Convert domain object to entity
     */
    private TicketEntity convertToEntity(Ticket ticket) {
        return TicketEntity.builder()
                .id(ticket.getId())
                .titre(ticket.getTitre())
                .description(ticket.getDescription())
                .statut(ticket.getStatut())
                .priorite(ticket.getPriorite())
                .categorie(ticket.getCategorie())
                .utilisateurId(ticket.getUtilisateurId())
                .technicienId(ticket.getTechnicienId())
                .teamId(ticket.getTeamId())
                .dateLimiteSla(ticket.getDateLimiteSla())
                .datePremiereReponse(ticket.getDatePremiereReponse())
                .slaRespecte(ticket.getSlaRespecte())
                .tempsResolutionMinutes(ticket.getTempsResolutionMinutes())
                .statutSla(ticket.getStatutSla())
                .nombreReassignations(ticket.getNombreReassignations())
                .tempsPremiereReponseMinutes(ticket.getTempsPremiereReponseMinutes())
                .enableNlp(ticket.getEnableNlp())
                .dateCreation(ticket.getDateCreation())
                .dateModification(ticket.getDateModification())
                .dateFermeture(ticket.getDateFermeture())
                .commentaireResolution(ticket.getCommentaireResolution())
                .actif(ticket.getActif())
                .build();
    }
    
    /**
     * Convert entity to domain object
     */
    private Ticket convertToDomain(TicketEntity entity) {
        return Ticket.builder()
                .id(entity.getId())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .statut(entity.getStatut())
                .priorite(entity.getPriorite())
                .categorie(entity.getCategorie())
                .utilisateurId(entity.getUtilisateurId())
                .technicienId(entity.getTechnicienId())
                .teamId(entity.getTeamId())
                .dateLimiteSla(entity.getDateLimiteSla())
                .datePremiereReponse(entity.getDatePremiereReponse())
                .slaRespecte(entity.getSlaRespecte())
                .tempsResolutionMinutes(entity.getTempsResolutionMinutes())
                .statutSla(entity.getStatutSla())
                .nombreReassignations(entity.getNombreReassignations())
                .tempsPremiereReponseMinutes(entity.getTempsPremiereReponseMinutes())
                .enableNlp(entity.getEnableNlp())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .dateFermeture(entity.getDateFermeture())
                .commentaireResolution(entity.getCommentaireResolution())
                .actif(entity.getActif())
                .build();
    }

    /**
     * Determine required skills based on category for assignment-service
     */
    private String determineRequiredSkills(String categorie) {
        return switch (categorie) {
            case "SECURITE" -> "Sécurité informatique, Analyse de vulnérabilités, Pentesting";
            case "AUDIT" -> "Audit informatique, Conformité, Analyse de risques";
            case "CONFORMITE" -> "Réglementations, GDPR, Normes de sécurité";
            case "DEVELOPPEMENT" -> "Développement logiciel, Debug, Programmation";
            case "DEVOPS" -> "CI/CD, Docker, Kubernetes, Infrastructure";
            case "CLOUD" -> "AWS, Azure, Architecture cloud, Migration";
            default -> "Compétences générales IT";
        };
    }

    /**
     * Determine urgency level based on priority for assignment-service
     */
    private String determineUrgencyLevel(PrioriteTicket priorite) {
        return switch (priorite) {
            case CRITIQUE -> "IMMEDIATE";
            case HAUTE -> "URGENT";
            case NORMALE -> "STANDARD";
            case BASSE -> "LOW";
        };
    }

    /**
     * Update ticket assignment (used by assignment-service)
     */
    @Transactional
    public void updateAssignment(UUID ticketId, UUID technicianId, UUID teamId) {
        log.info("Updating assignment for ticket {}: technician={}, team={}", ticketId, technicianId, teamId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        ticket.setTechnicienId(technicianId);
        ticket.setTeamId(teamId);
        ticket.setDateModification(java.time.LocalDateTime.now());

        ticketRepository.save(ticket);

        log.info("Successfully updated assignment for ticket {}", ticketId);
    }

    /**
     * Get tickets assigned to a technician
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByTechnician(UUID technicianId) {
        log.info("Getting tickets for technician: {}", technicianId);

        if (technicianId == null) {
            log.warn("Technician ID is null, returning empty list");
            return List.of();
        }

        List<TicketEntity> entities = ticketRepository.findByTechnicienIdAndActifTrueOrderByDateCreationDesc(technicianId);

        log.info("Found {} tickets for technician {}", entities.size(), technicianId);

        // Debug: Log ALL tickets to verify filtering
        entities.forEach(ticket ->
            log.info("Ticket {}: title='{}', assigned to technician {}, created by user {}",
                ticket.getId(), ticket.getTitre(), ticket.getTechnicienId(), ticket.getUtilisateurId())
        );

        // Double-check filtering in Java (safety net)
        List<TicketEntity> filteredEntities = entities.stream()
                .filter(ticket -> technicianId.equals(ticket.getTechnicienId()))
                .toList();

        if (filteredEntities.size() != entities.size()) {
            log.warn("Repository query returned {} tickets but only {} match technician ID {}. Using filtered list.",
                entities.size(), filteredEntities.size(), technicianId);
            entities = filteredEntities;
        }

        return entities.stream()
                .map(this::convertToDomain)
                .toList();
    }

    /**
     * Get tickets by technician and status
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByTechnicianAndStatus(UUID technicianId, StatutTicket status) {
        log.debug("Getting tickets for technician {} with status: {}", technicianId, status);

        List<TicketEntity> entities = ticketRepository.findByTechnicienIdAndStatutAndActifTrueOrderByDateCreationDesc(technicianId, status);
        return entities.stream()
                .map(this::convertToDomain)
                .toList();
    }

    /**
     * Start working on a ticket (change status to EN_COURS)
     */
    @Transactional
    public Ticket startWorkingOnTicket(UUID ticketId, UUID technicianId) {
        log.info("Technician {} starting work on ticket {}", technicianId, ticketId);

        TicketEntity entity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // Verify technician is assigned to this ticket
        if (!technicianId.equals(entity.getTechnicienId())) {
            throw new RuntimeException("Technician not assigned to this ticket");
        }

        // Update status to OUVERT (technician starts working)
        entity.setStatut(StatutTicket.OUVERT);
        entity.setDateModification(java.time.LocalDateTime.now());

        // Mark first response if not already marked
        if (entity.getDatePremiereReponse() == null) {
            entity.setDatePremiereReponse(java.time.LocalDateTime.now());
            entity.setTempsPremiereReponseMinutes(
                (int) java.time.temporal.ChronoUnit.MINUTES.between(entity.getDateCreation(), entity.getDatePremiereReponse()));
        }

        // Add work start comment
        addCommentToTicket(entity, "Travail commencé sur le ticket");

        entity = ticketRepository.save(entity);

        return convertToDomain(entity);
    }

    /**
     * Add work note/comment to a ticket (replaces existing comments)
     */
    @Transactional
    public void addWorkNote(UUID ticketId, UUID technicianId, String note) {
        log.info("Adding work note to ticket {} by technician {}", ticketId, technicianId);

        TicketEntity entity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // Verify technician is assigned to this ticket
        if (!technicianId.equals(entity.getTechnicienId())) {
            throw new RuntimeException("Technician not assigned to this ticket");
        }

        // Replace comment with new work note (not append)
        replaceCommentInTicket(entity, "Note de travail: " + note);
        entity.setDateModification(java.time.LocalDateTime.now());

        ticketRepository.save(entity);
    }

    /**
     * Resolve a ticket with solution and comments (replaces existing comments)
     */
    @Transactional
    public Ticket resolveTicket(UUID ticketId, UUID technicianId, String solution) {
        log.info("Resolving ticket {} by technician {}", ticketId, technicianId);

        TicketEntity entity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // Verify technician is assigned to this ticket
        if (!technicianId.equals(entity.getTechnicienId())) {
            throw new RuntimeException("Technician not assigned to this ticket");
        }

        // Replace comment with resolution solution (not append)
        replaceCommentInTicket(entity, "RÉSOLUTION: " + solution);

        // Update ticket to resolved
        entity.setStatut(StatutTicket.RESOLU);
        entity.setDateModification(java.time.LocalDateTime.now());

        // Calculate resolution time
        entity.setTempsResolutionMinutes(
            (int) java.time.temporal.ChronoUnit.MINUTES.between(entity.getDateCreation(), java.time.LocalDateTime.now()));

        // Evaluate SLA compliance
        if (entity.getDateLimiteSla() != null) {
            entity.setSlaRespecte(java.time.LocalDateTime.now().isBefore(entity.getDateLimiteSla()) ||
                                 java.time.LocalDateTime.now().isEqual(entity.getDateLimiteSla()));
        }

        entity = ticketRepository.save(entity);

        return convertToDomain(entity);
    }

    /**
     * Request reassignment of a ticket
     */
    @Transactional
    public void requestReassignment(UUID ticketId, UUID technicianId, String reason) {
        log.info("Technician {} requesting reassignment of ticket {}: {}", technicianId, ticketId, reason);

        TicketEntity entity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // Verify technician is assigned to this ticket
        if (!technicianId.equals(entity.getTechnicienId())) {
            throw new RuntimeException("Technician not assigned to this ticket");
        }

        // Add reassignment request comment
        addCommentToTicket(entity, "DEMANDE DE RÉASSIGNATION: " + reason);
        entity.setDateModification(java.time.LocalDateTime.now());

        ticketRepository.save(entity);
    }

    /**
     * Helper method to add timestamped comments to commentaire_resolution field (APPEND)
     */
    private void addCommentToTicket(TicketEntity entity, String comment) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedComment = String.format("[%s] %s", timestamp, comment);

        String existingComments = entity.getCommentaireResolution();
        if (existingComments == null || existingComments.trim().isEmpty()) {
            entity.setCommentaireResolution(formattedComment);
        } else {
            entity.setCommentaireResolution(existingComments + "\n" + formattedComment);
        }
    }

    /**
     * Helper method to replace commentaire_resolution field with new timestamped comment (REPLACE)
     */
    private void replaceCommentInTicket(TicketEntity entity, String comment) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedComment = String.format("[%s] %s", timestamp, comment);

        // Replace existing comments with new comment
        entity.setCommentaireResolution(formattedComment);
    }

    /**
     * Get technician dashboard data
     */
    @Transactional(readOnly = true)
    public com.itsm.ticket.presentation.controller.TechnicianController.TechnicianDashboardResponse getTechnicianDashboard(UUID technicianId) {
        log.debug("Getting dashboard for technician {}", technicianId);

        List<TicketEntity> allTickets = ticketRepository.findByTechnicienIdAndActifTrueOrderByDateCreationDesc(technicianId);

        int totalAssigned = allTickets.size();
        int openTickets = (int) allTickets.stream().filter(t -> t.getStatut() == StatutTicket.OUVERT).count();  // Technician working
        int inProgress = (int) allTickets.stream().filter(t -> t.getStatut() == StatutTicket.EN_COURS || t.getStatut() == StatutTicket.EN_ATTENTE).count();  // New tickets + waiting

        java.time.LocalDate today = java.time.LocalDate.now();
        int resolvedToday = (int) allTickets.stream()
                .filter(t -> t.getStatut() == StatutTicket.RESOLU)
                .filter(t -> t.getDateModification() != null && t.getDateModification().toLocalDate().equals(today))
                .count();

        int overdueTickets = (int) allTickets.stream()
                .filter(t -> t.getDateLimiteSla() != null && java.time.LocalDateTime.now().isAfter(t.getDateLimiteSla()))
                .filter(t -> t.getStatut() != StatutTicket.RESOLU && t.getStatut() != StatutTicket.FERME)
                .count();

        double avgResolutionTime = allTickets.stream()
                .filter(t -> t.getTempsResolutionMinutes() != null)
                .mapToInt(TicketEntity::getTempsResolutionMinutes)
                .average()
                .orElse(0.0);

        int urgentTicketsCount = (int) allTickets.stream()
                .filter(t -> t.getPriorite() == PrioriteTicket.CRITIQUE ||
                           t.getPriorite() == PrioriteTicket.HAUTE)
                .filter(t -> t.getStatut() != StatutTicket.RESOLU && t.getStatut() != StatutTicket.FERME)
                .count();

        int newTicketsCount = (int) allTickets.stream()
                .filter(t -> t.getStatut() == StatutTicket.EN_COURS || t.getStatut() == StatutTicket.EN_ATTENTE)  // New tickets are EN_COURS
                .count();

        return com.itsm.ticket.presentation.controller.TechnicianController.TechnicianDashboardResponse.builder()
                .totalAssignedTickets(totalAssigned)
                .openTickets(openTickets)
                .inProgressTickets(inProgress)
                .resolvedToday(resolvedToday)
                .overdueTickets(overdueTickets)
                .averageResolutionTime(avgResolutionTime)
                .urgentTicketsCount(urgentTicketsCount)
                .newTicketsCount(newTicketsCount)
                .build();
    }
}
