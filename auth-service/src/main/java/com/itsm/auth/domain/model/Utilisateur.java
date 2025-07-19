package com.itsm.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasseHashe;
    private Role role;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;

    public static Utilisateur creerUtilisateur(String nom, String prenom, String email, String motDePasseHashe) {
        return creerUtilisateur(nom, prenom, email, motDePasseHashe, Role.UTILISATEUR);
    }

    public static Utilisateur creerUtilisateur(String nom, String prenom, String email, String motDePasseHashe, Role role) {
        return Utilisateur.builder()
                .id(UUID.randomUUID())  // Only for new users, not for sync
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .motDePasseHashe(motDePasseHashe)
                .role(role != null ? role : Role.UTILISATEUR) // Default to UTILISATEUR if role is null
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }

    /**
     * Factory method for sync operations - uses provided ID
     */
    public static Utilisateur creerUtilisateurAvecId(UUID id, String nom, String prenom, String email, String motDePasseHashe, Role role) {
        return Utilisateur.builder()
                .id(id)  // Use provided ID for dual database sync
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .motDePasseHashe(motDePasseHashe)
                .role(role != null ? role : Role.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .actif(true)
                .build();
    }

    public void mettreAJour() {
        this.dateModification = LocalDateTime.now();
    }
}
