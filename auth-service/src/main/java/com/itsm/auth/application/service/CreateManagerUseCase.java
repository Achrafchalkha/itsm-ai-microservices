package com.itsm.auth.application.service;

import com.itsm.auth.application.dto.CreateManagerRequest;
import com.itsm.auth.application.dto.CreateManagerResponse;
import com.itsm.auth.domain.event.TeamCreatedEvent;
import com.itsm.auth.domain.event.UserCreatedEvent;
import com.itsm.auth.domain.model.Role;
import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateManagerUseCase {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public CreateManagerResponse execute(CreateManagerRequest request) {
        log.info("Création d'un manager pour l'équipe: {}", request.getTeamName());

        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Créer le manager
        String motDePasseHashe = passwordEncoder.encode(request.getMotDePasse());
        Utilisateur manager = Utilisateur.creerUtilisateur(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
                motDePasseHashe,
                Role.MANAGER
        );

        // Sauvegarder le manager
        Utilisateur managerSauvegarde = utilisateurRepository.save(manager);
        log.info("Manager créé avec succès: {}", managerSauvegarde.getId());

        // Generate team ID for the team that will be created in user-service
        UUID teamId = UUID.randomUUID();
        log.info("Équipe ID générée: {} pour le manager: {}", teamId, managerSauvegarde.getId());

        // Publier les événements sur Kafka
        try {
            // Événement UserCreated pour le manager
            UserCreatedEvent userEvent = UserCreatedEvent.builder()
                    .userId(managerSauvegarde.getId())
                    .email(managerSauvegarde.getEmail())
                    .nom(managerSauvegarde.getNom())
                    .prenom(managerSauvegarde.getPrenom())
                    .role(managerSauvegarde.getRole())
                    .dateCreation(managerSauvegarde.getDateCreation())
                    .build();

            kafkaTemplate.send("user-created", userEvent).get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Événement UserCreated publié pour le manager: {}", managerSauvegarde.getId());

            // Événement TeamCreated pour l'équipe
            TeamCreatedEvent teamEvent = TeamCreatedEvent.builder()
                    .teamId(teamId)
                    .teamName(request.getTeamName())
                    .teamDescription(request.getTeamDescription() != null ? request.getTeamDescription() : "Équipe gérée par " + request.getPrenom() + " " + request.getNom())
                    .managerId(managerSauvegarde.getId())
                    .managerEmail(managerSauvegarde.getEmail())
                    .managerNom(managerSauvegarde.getNom())
                    .managerPrenom(managerSauvegarde.getPrenom())
                    .dateCreation(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("team-created", teamEvent).get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Événement TeamCreated publié pour l'équipe: {}", request.getTeamName());
        } catch (Exception e) {
            log.warn("Impossible de publier les événements Kafka: {}", e.getMessage());
        }

        return CreateManagerResponse.builder()
                .message("Manager créé avec succès et assigné à l'équipe '" + request.getTeamName() + "'")
                .managerId(managerSauvegarde.getId())
                .email(managerSauvegarde.getEmail())
                .nom(managerSauvegarde.getNom())
                .prenom(managerSauvegarde.getPrenom())
                .role(managerSauvegarde.getRole())
                .teamName(request.getTeamName())
                .teamDescription(request.getTeamDescription() != null ? request.getTeamDescription() : "Équipe gérée par " + request.getPrenom() + " " + request.getNom())
                .success(true)
                .build();
    }
}
