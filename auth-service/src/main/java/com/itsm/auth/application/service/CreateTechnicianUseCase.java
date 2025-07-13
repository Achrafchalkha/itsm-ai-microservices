package com.itsm.auth.application.service;

import com.itsm.auth.application.dto.CreateTechnicianRequest;
import com.itsm.auth.application.dto.CreateTechnicianResponse;
import com.itsm.auth.domain.event.TechnicianAssignedEvent;
import com.itsm.auth.domain.event.TechnicianCreatedEvent;
import com.itsm.auth.domain.event.UserCreatedEvent;
import com.itsm.auth.domain.model.Role;
import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTechnicianUseCase {
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public CreateTechnicianResponse execute(CreateTechnicianRequest request) {
        // Vérifier que l'utilisateur connecté est un MANAGER
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur currentUser = (Utilisateur) auth.getPrincipal();

        log.info("Création d'un technicien par le manager: {}", currentUser.getEmail());
        
        if (currentUser.getRole() != Role.MANAGER) {
            throw new RuntimeException("Seuls les MANAGERs peuvent créer des techniciens");
        }
        
        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        
        // Note: Team information will be resolved in user-service via Kafka events
        log.info("Manager {} creating technician: {}", currentUser.getEmail(), request.getEmail());

        // Créer le technicien
        String motDePasseHashe = passwordEncoder.encode(request.getMotDePasse());
        Utilisateur technicien = Utilisateur.creerUtilisateur(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
                motDePasseHashe,
                Role.TECHNICIEN
        );

        // Sauvegarder le technicien
        Utilisateur technicienSauvegarde = utilisateurRepository.save(technicien);
        log.info("Technicien créé avec succès: {}", technicienSauvegarde.getId());

        // Publier les événements sur Kafka
        try {
            // Événement UserCreated pour le technicien
            UserCreatedEvent userEvent = UserCreatedEvent.builder()
                    .userId(technicienSauvegarde.getId())
                    .email(technicienSauvegarde.getEmail())
                    .nom(technicienSauvegarde.getNom())
                    .prenom(technicienSauvegarde.getPrenom())
                    .role(technicienSauvegarde.getRole())
                    .dateCreation(technicienSauvegarde.getDateCreation())
                    .build();

            kafkaTemplate.send("user-created", userEvent).get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Événement UserCreated publié pour le technicien: {}", technicienSauvegarde.getId());

            // Événement TechnicianCreated avec détails spécifiques et assignation d'équipe
            TechnicianCreatedEvent techEvent = TechnicianCreatedEvent.builder()
                    .technicianId(technicienSauvegarde.getId())
                    .email(technicienSauvegarde.getEmail())
                    .nom(technicienSauvegarde.getNom())
                    .prenom(technicienSauvegarde.getPrenom())
                    .teamId(null) // Will be resolved in user-service
                    .teamName("Manager Team") // Placeholder
                    .managerId(currentUser.getId())
                    .managerEmail(currentUser.getEmail())
                    .localisation(request.getLocalisation())
                    .telephone(request.getTelephone())
                    .specialite(request.getSpecialite())
                    .competences(request.getCompetences() != null ?
                        request.getCompetences().stream()
                            .map(c -> TechnicianCreatedEvent.CompetenceInfo.builder()
                                .nom(c.getNom())
                                .description(c.getDescription())
                                .categorie(c.getCategorie())
                                .niveau(c.getNiveau())
                                .build())
                            .collect(Collectors.toList()) : List.of())
                    .dateCreation(technicienSauvegarde.getDateCreation())
                    .build();

            kafkaTemplate.send("technician-created", techEvent).get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Événement TechnicianCreated publié pour le technicien: {}", technicienSauvegarde.getId());

            // Événement TechnicianAssignedToTeam pour la notification d'assignation
            TechnicianAssignedEvent assignEvent = TechnicianAssignedEvent.builder()
                    .technicianId(technicienSauvegarde.getId())
                    .technicianEmail(technicienSauvegarde.getEmail())
                    .technicianNom(technicienSauvegarde.getNom())
                    .technicianPrenom(technicienSauvegarde.getPrenom())
                    .teamId(null) // Will be resolved in user-service
                    .teamName("Manager Team") // Placeholder
                    .managerId(currentUser.getId())
                    .managerEmail(currentUser.getEmail())
                    .managerNom(currentUser.getNom())
                    .managerPrenom(currentUser.getPrenom())
                    .dateAssignation(technicienSauvegarde.getDateCreation())
                    .build();

            kafkaTemplate.send("technician-assigned", assignEvent).get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Événement TechnicianAssigned publié pour le technicien: {} assigné à l'équipe du manager",
                    technicienSauvegarde.getId());
        } catch (Exception e) {
            log.warn("Impossible de publier les événements Kafka: {}", e.getMessage());
        }

        return CreateTechnicianResponse.builder()
                .message("Technicien créé avec succès. Assignation à l'équipe en cours...")
                .technicianId(technicienSauvegarde.getId())
                .email(technicienSauvegarde.getEmail())
                .nom(technicienSauvegarde.getNom())
                .prenom(technicienSauvegarde.getPrenom())
                .role(technicienSauvegarde.getRole())
                .teamId(null) // Will be set by user-service
                .teamName("Manager Team") // Placeholder
                .localisation(request.getLocalisation())
                .telephone(request.getTelephone())
                .specialite(request.getSpecialite())
                .competences(request.getCompetences() != null ?
                    request.getCompetences().stream()
                        .map(c -> CreateTechnicianResponse.CompetenceInfo.builder()
                            .nom(c.getNom())
                            .description(c.getDescription())
                            .categorie(c.getCategorie())
                            .niveau(c.getNiveau())
                            .build())
                        .collect(Collectors.toList()) : List.of())
                .success(true)
                .build();
    }
}
