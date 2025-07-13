package com.itsm.auth.application.service;

import com.itsm.auth.application.dto.AuthResponse;
import com.itsm.auth.application.dto.RegisterUserRequest;
import com.itsm.auth.domain.event.UserCreatedEvent;
import com.itsm.auth.domain.model.Role;
import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
import com.itsm.auth.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserUseCase {
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public AuthResponse execute(RegisterUserRequest request) {
        log.info("Auto-inscription d'un utilisateur pour l'email: {}", request.getEmail());

        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Créer l'utilisateur avec rôle UTILISATEUR automatiquement assigné
        String motDePasseHashe = passwordEncoder.encode(request.getMotDePasse());
        Utilisateur utilisateur = Utilisateur.creerUtilisateur(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
                motDePasseHashe,
                Role.UTILISATEUR // Auto-assign UTILISATEUR role for self-registration
        );

        // Sauvegarder l'utilisateur
        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur créé avec succès: {}", utilisateurSauvegarde.getId());

        // Publier l'événement UserCreated sur Kafka
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .userId(utilisateurSauvegarde.getId())
                    .email(utilisateurSauvegarde.getEmail())
                    .nom(utilisateurSauvegarde.getNom())
                    .prenom(utilisateurSauvegarde.getPrenom())
                    .role(utilisateurSauvegarde.getRole())
                    .dateCreation(utilisateurSauvegarde.getDateCreation())
                    .build();

            kafkaTemplate.send("user-created", event).get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Événement UserCreated publié pour l'utilisateur: {}", utilisateurSauvegarde.getId());
        } catch (Exception e) {
            log.warn("Impossible de publier l'événement Kafka (Kafka non disponible): {}", e.getMessage());
            // Continue without Kafka - don't fail the registration
        }

        // Générer le token JWT
        String token = jwtProvider.generateToken(utilisateurSauvegarde);

        return AuthResponse.builder()
                .token(token)
                .userId(utilisateurSauvegarde.getId())
                .email(utilisateurSauvegarde.getEmail())
                .nom(utilisateurSauvegarde.getNom())
                .prenom(utilisateurSauvegarde.getPrenom())
                .role(utilisateurSauvegarde.getRole())
                .build();
    }
}
