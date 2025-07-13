package com.itsm.auth.application.service;

import com.itsm.auth.application.dto.AuthResponse;
import com.itsm.auth.application.dto.LoginUserRequest;
import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
import com.itsm.auth.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUserUseCase {
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthResponse execute(LoginUserRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());
        
        // Trouver l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasseHashe())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        
        // Vérifier si l'utilisateur est actif
        if (!utilisateur.isActif()) {
            throw new RuntimeException("Compte utilisateur désactivé");
        }

        log.info("Connexion réussie pour l'utilisateur: {}", utilisateur.getId());

        // Générer le token JWT
        String token = jwtProvider.generateToken(utilisateur);

        return AuthResponse.builder()
                .token(token)
                .userId(utilisateur.getId())
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .build();
    }
}
