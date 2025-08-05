package com.itsm.ticket.application.service;

import com.itsm.ticket.domain.model.PrioriteTicket;
import com.itsm.ticket.domain.model.StatutTicket;
import com.itsm.ticket.domain.model.Ticket;
import com.itsm.ticket.infrastructure.kafka.TicketEventPublisher;
import com.itsm.ticket.infrastructure.kafka.event.TicketCreatedEvent;
import com.itsm.ticket.infrastructure.kafka.event.TicketNoteAddedEvent;
import com.itsm.ticket.infrastructure.kafka.event.TicketStatusChangedEvent;
import com.itsm.ticket.infrastructure.kafka.event.TicketUpdatedEvent;
import com.itsm.ticket.infrastructure.persistence.entity.TicketEntity;
import com.itsm.ticket.infrastructure.persistence.repository.TicketRepository;
import com.itsm.ticket.infrastructure.storage.FileStorageService;
import com.itsm.ticket.infrastructure.client.UserServiceClient;
import com.itsm.ticket.application.dto.TeamDashboardDto;
import com.itsm.ticket.application.dto.TechnicianStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;

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
    private final FileStorageService fileStorageService;
    private final RestTemplate restTemplate;
    private final UserServiceClient userServiceClient;
    
    /**
     * Create a new ticket
     */
    @Transactional
    public Ticket createTicket(String titre, String description, PrioriteTicket priorite,
                              String categorie, UUID utilisateurId, String utilisateurEmail, boolean enableNlp, String fichiersAttaches) {
        
        log.info("Creating ticket for user: {}", utilisateurId);
        
        // Create domain object
        Ticket ticket = Ticket.creerTicket(titre, description, priorite, categorie, utilisateurId);
        ticket.setEnableNlp(enableNlp);

        // Process and save attached files if provided
        if (fichiersAttaches != null && !fichiersAttaches.trim().isEmpty()) {
            log.info("Processing attached files for ticket: {} characters", fichiersAttaches.length());
            log.info("Raw attached files JSON: {}", fichiersAttaches.substring(0, Math.min(500, fichiersAttaches.length())));

            // Process files and save them locally
            String processedFiles = fileStorageService.processAttachedFiles(fichiersAttaches, ticket.getId());
            ticket.setFichiersAttaches(processedFiles);

            log.info("Attached files processed and saved for ticket: {}", ticket.getId());
            log.info("Processed files result: {}", processedFiles != null ? processedFiles.substring(0, Math.min(200, processedFiles.length())) : "null");
        } else {
            log.info("No attached files provided for ticket: {}", ticket.getId());
        }
        
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
                .fichiersAttaches(ticket.getFichiersAttaches())
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
                .fichiersAttaches(entity.getFichiersAttaches())
                .enableNlp(entity.getEnableNlp())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .dateFermeture(entity.getDateFermeture())
                .commentaireResolution(entity.getCommentaireResolution())
                .fichiersAttaches(entity.getFichiersAttaches())
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

        // Store old status for event
        String oldStatus = entity.getStatut().name();

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

        // Get technician information for notification
        UserServiceClient.TechnicianBasicInfo technicianInfo = null;
        try {
            technicianInfo = userServiceClient.getTechnicianBasicInfo(technicianId);
        } catch (Exception e) {
            log.warn("Could not fetch technician info for notification: {}", e.getMessage());
        }

        // Publish status changed event for user notification
        TicketStatusChangedEvent statusEvent = TicketStatusChangedEvent.builder()
                .ticketId(ticketId)
                .ticketTitre(entity.getTitre())
                .utilisateurId(entity.getUtilisateurId())
                .technicienId(technicianId)
                .technicienNom(technicianInfo != null ? technicianInfo.getNom() : "Technicien")
                .technicienPrenom(technicianInfo != null ? technicianInfo.getPrenom() : "")
                .oldStatus(oldStatus)
                .newStatus("OUVERT")
                .changeReason("Technicien a commencé le travail")
                .changedAt(java.time.LocalDateTime.now())
                .build();

        eventPublisher.publishTicketStatusChanged(statusEvent);
        log.info("🔔 PUBLISHED STATUS CHANGE EVENT: ticket={}, user={}, oldStatus={}, newStatus={}, technician={}",
                ticketId, entity.getUtilisateurId(), oldStatus, "OUVERT", technicianId);

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

        // Get technician information for notification
        UserServiceClient.TechnicianBasicInfo technicianInfo = null;
        try {
            technicianInfo = userServiceClient.getTechnicianBasicInfo(technicianId);
        } catch (Exception e) {
            log.warn("Could not fetch technician info for notification: {}", e.getMessage());
        }

        // Publish note added event for user notification
        TicketNoteAddedEvent noteEvent = TicketNoteAddedEvent.builder()
                .ticketId(ticketId)
                .ticketTitre(entity.getTitre())
                .utilisateurId(entity.getUtilisateurId())
                .technicienId(technicianId)
                .technicienNom(technicianInfo != null ? technicianInfo.getNom() : "Technicien")
                .technicienPrenom(technicianInfo != null ? technicianInfo.getPrenom() : "")
                .note(note)
                .addedAt(java.time.LocalDateTime.now())
                .build();

        eventPublisher.publishTicketNoteAdded(noteEvent);
        log.info("Published note added event for ticket {} to notify user {}", ticketId, entity.getUtilisateurId());
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

        // Store old status for event
        String oldStatus = entity.getStatut().name();

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

        // Get technician information for notification
        UserServiceClient.TechnicianBasicInfo technicianInfo = null;
        try {
            technicianInfo = userServiceClient.getTechnicianBasicInfo(technicianId);
        } catch (Exception e) {
            log.warn("Could not fetch technician info for notification: {}", e.getMessage());
        }

        // Publish status changed event for user notification
        TicketStatusChangedEvent statusEvent = TicketStatusChangedEvent.builder()
                .ticketId(ticketId)
                .ticketTitre(entity.getTitre())
                .utilisateurId(entity.getUtilisateurId())
                .technicienId(technicianId)
                .technicienNom(technicianInfo != null ? technicianInfo.getNom() : "Technicien")
                .technicienPrenom(technicianInfo != null ? technicianInfo.getPrenom() : "")
                .oldStatus(oldStatus)
                .newStatus("RESOLU")
                .changeReason("Ticket résolu par le technicien")
                .changedAt(java.time.LocalDateTime.now())
                .build();

        eventPublisher.publishTicketStatusChanged(statusEvent);
        log.info("Published status changed event for ticket {} to notify user {}", ticketId, entity.getUtilisateurId());

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

    /**
     * Delete ticket (soft delete)
     */
    @Transactional
    public void deleteTicket(UUID ticketId) {
        log.info("Deleting ticket: {}", ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // Perform soft delete by setting a deleted flag or status
        // For now, we'll actually delete the record, but in production you might want to set a deleted flag
        ticketRepository.delete(ticket);

        log.info("Ticket {} deleted successfully", ticketId);
    }

    /**
     * Get tickets for manager's team with pagination and filtering (simple approach)
     */
    public Page<Ticket> getTicketsByManagerTeam(UUID managerId, Pageable pageable, String status, String priority) {
        log.info("Getting tickets for manager {}'s team", managerId);

        try {
            // Get manager's team ID from user-service (simple approach)
            UUID teamId = userServiceClient.getManagerTeamId(managerId);
            if (teamId == null) {
                log.warn("No team found for manager {}, falling back to all tickets (manager may not be properly configured)", managerId);
                // Fallback: return all tickets if manager doesn't have a team
                if (status != null && priority != null) {
                    StatutTicket statutTicket = StatutTicket.valueOf(status.toUpperCase());
                    PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority.toUpperCase());
                    return ticketRepository.findByStatutAndPrioriteAndActifTrue(
                        statutTicket, prioriteTicket, pageable)
                        .map(this::convertToDomain);
                } else if (status != null) {
                    StatutTicket statutTicket = StatutTicket.valueOf(status.toUpperCase());
                    return ticketRepository.findByStatutAndActifTrue(statutTicket, pageable)
                        .map(this::convertToDomain);
                } else if (priority != null) {
                    PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority.toUpperCase());
                    return ticketRepository.findByPrioriteAndActifTrue(prioriteTicket, pageable)
                        .map(this::convertToDomain);
                } else {
                    return ticketRepository.findByActifTrue(pageable)
                        .map(this::convertToDomain);
                }
            }

            log.info("Manager {} belongs to team {}, filtering tickets", managerId, teamId);

            // Filter tickets by team_id with status and priority filters
            if (status != null && priority != null) {
                StatutTicket statutTicket = StatutTicket.valueOf(status.toUpperCase());
                PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority.toUpperCase());
                return ticketRepository.findByTeamIdAndStatutAndPrioriteAndActifTrue(
                    teamId, statutTicket, prioriteTicket, pageable)
                    .map(this::convertToDomain);
            } else if (status != null) {
                StatutTicket statutTicket = StatutTicket.valueOf(status.toUpperCase());
                return ticketRepository.findByTeamIdAndStatutAndActifTrue(teamId, statutTicket, pageable)
                    .map(this::convertToDomain);
            } else if (priority != null) {
                PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority.toUpperCase());
                return ticketRepository.findByTeamIdAndPrioriteAndActifTrue(teamId, prioriteTicket, pageable)
                    .map(this::convertToDomain);
            } else {
                return ticketRepository.findByTeamIdAndActifTrue(teamId, pageable)
                    .map(this::convertToDomain);
            }

        } catch (Exception e) {
            log.error("Error getting tickets for manager {}'s team: {}", managerId, e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    /**
     * Get tickets by manager team and status (simple approach)
     */
    public List<Ticket> getTicketsByManagerTeamAndStatus(UUID managerId, StatutTicket status) {
        log.info("Getting tickets for manager {}'s team with status: {}", managerId, status);

        try {
            // Get manager's team ID from user-service (simple approach)
            UUID teamId = userServiceClient.getManagerTeamId(managerId);
            if (teamId == null) {
                log.warn("No team found for manager {}, falling back to all tickets with status {}", managerId, status);
                // Fallback: return all tickets with the specified status
                return ticketRepository.findByStatutAndActifTrueOrderByDateCreationDesc(status)
                        .stream()
                        .map(this::convertToDomain)
                        .toList();
            }

            log.info("Manager {} belongs to team {}, filtering tickets by status {}", managerId, teamId, status);

            return ticketRepository.findByTeamIdAndStatutAndActifTrueOrderByDateCreationDesc(teamId, status)
                    .stream()
                    .map(this::convertToDomain)
                    .toList();

        } catch (Exception e) {
            log.error("Error getting tickets for manager {}'s team with status {}: {}",
                    managerId, status, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get ticket by ID for manager (ensures ticket belongs to manager's team)
     */
    public Ticket getTicketByIdForManager(UUID ticketId, UUID managerId) {
        log.info("Getting ticket {} for manager {}", ticketId, managerId);

        // Get the ticket
        TicketEntity ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        // For now, skip team validation (simplified approach)
        // TODO: Add team validation later

        return convertToDomain(ticketEntity);
    }

    /**
     * Get team dashboard statistics for manager
     */
    public TeamDashboardDto getTeamDashboard(UUID managerId) {
        log.info("Getting team dashboard for manager {}", managerId);

        try {
            // Get manager's team ID from user-service (simple approach)
            UUID teamId = userServiceClient.getManagerTeamId(managerId);
            if (teamId == null) {
                log.warn("No team found for manager {}, falling back to all tickets for dashboard", managerId);
                // Fallback: calculate statistics for all tickets
                long totalTickets = ticketRepository.countByActifTrue();
                long openTickets = ticketRepository.countByStatutAndActifTrue(StatutTicket.OUVERT);
                long inProgressTickets = ticketRepository.countByStatutAndActifTrue(StatutTicket.EN_COURS);
                long resolvedTickets = ticketRepository.countByStatutAndActifTrue(StatutTicket.RESOLU);
                long closedTickets = ticketRepository.countByStatutAndActifTrue(StatutTicket.FERME);
                double averageResolutionTime = 0.0;
                List<TechnicianStatsDto> technicianStats = List.of();
                return new TeamDashboardDto(totalTickets, openTickets, inProgressTickets, resolvedTickets,
                        closedTickets, averageResolutionTime, technicianStats);
            }

            log.info("Manager {} belongs to team {}, calculating team statistics", managerId, teamId);

            // Calculate statistics for the team only
            long totalTickets = ticketRepository.countByTeamIdAndActifTrue(teamId);
            long openTickets = ticketRepository.countByTeamIdAndStatutAndActifTrue(teamId, StatutTicket.OUVERT);
            long inProgressTickets = ticketRepository.countByTeamIdAndStatutAndActifTrue(teamId, StatutTicket.EN_COURS);
            long resolvedTickets = ticketRepository.countByTeamIdAndStatutAndActifTrue(teamId, StatutTicket.RESOLU);
            long closedTickets = ticketRepository.countByTeamIdAndStatutAndActifTrue(teamId, StatutTicket.FERME);

            // Calculate average resolution time (simplified)
            double averageResolutionTime = 0.0; // TODO: Implement actual calculation

            // Get technician statistics (simplified - empty for now)
            List<TechnicianStatsDto> technicianStats = List.of();

            return new TeamDashboardDto(
                    totalTickets, openTickets, inProgressTickets, resolvedTickets,
                    closedTickets, averageResolutionTime, technicianStats);

        } catch (Exception e) {
            log.error("Error getting team dashboard for manager {}: {}", managerId, e.getMessage(), e);
            return new TeamDashboardDto(0, 0, 0, 0, 0, 0.0, List.of());
        }
    }

    /**
     * Get tickets for manager's team (simple approach)
     */
    public List<Ticket> getTicketsByTeam(UUID managerId, String status, String priority) {
        log.info("Getting tickets for manager's team. Manager: {}, Status: {}, Priority: {}", managerId, status, priority);

        try {
            // Get manager's team ID from user-service (simple approach)
            UUID teamId = userServiceClient.getManagerTeamId(managerId);
            if (teamId == null) {
                log.warn("No team found for manager {}, falling back to all tickets", managerId);
                // Fallback: return all tickets with filters
                List<TicketEntity> ticketEntities = ticketRepository.findByActifTrueOrderByDateCreationDesc();

                // Apply filters if provided
                if (status != null) {
                    StatutTicket statutTicket = StatutTicket.valueOf(status);
                    ticketEntities = ticketEntities.stream()
                        .filter(ticket -> ticket.getStatut() == statutTicket)
                        .toList();
                }

                if (priority != null) {
                    PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority);
                    ticketEntities = ticketEntities.stream()
                        .filter(ticket -> ticket.getPriorite() == prioriteTicket)
                        .toList();
                }

                return ticketEntities.stream()
                        .map(this::convertToDomain)
                        .toList();
            }

            log.info("Manager {} belongs to team {}, filtering tickets", managerId, teamId);

            // Get tickets for the team with filters
            List<TicketEntity> ticketEntities;
            if (status != null && priority != null) {
                StatutTicket statutTicket = StatutTicket.valueOf(status);
                PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority);
                ticketEntities = ticketRepository.findByTeamIdAndActifTrueOrderByDateCreationDesc(teamId)
                    .stream()
                    .filter(ticket -> ticket.getStatut() == statutTicket && ticket.getPriorite() == prioriteTicket)
                    .toList();
            } else if (status != null) {
                StatutTicket statutTicket = StatutTicket.valueOf(status);
                ticketEntities = ticketRepository.findByTeamIdAndStatutAndActifTrueOrderByDateCreationDesc(teamId, statutTicket);
            } else if (priority != null) {
                PrioriteTicket prioriteTicket = PrioriteTicket.valueOf(priority);
                ticketEntities = ticketRepository.findByTeamIdAndActifTrueOrderByDateCreationDesc(teamId)
                    .stream()
                    .filter(ticket -> ticket.getPriorite() == prioriteTicket)
                    .toList();
            } else {
                ticketEntities = ticketRepository.findByTeamIdAndActifTrueOrderByDateCreationDesc(teamId);
            }

            log.info("Found {} tickets for manager {}'s team {}", ticketEntities.size(), managerId, teamId);

            return ticketEntities.stream()
                    .map(this::convertToDomain)
                    .toList();

        } catch (Exception e) {
            log.error("Error getting tickets for manager's team: {}", e.getMessage(), e);
            return List.of();
        }
    }



    /**
     * Simple DTO for technician data from user-service
     */
    public static class TechnicianDto {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;

        // Constructors
        public TechnicianDto() {}

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }


}
