package com.itsm.auth.infrastructure.persistence.mapper;

import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.infrastructure.persistence.entity.UtilisateurEntity;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {
    
    public UtilisateurEntity toEntity(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        
        return UtilisateurEntity.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .motDePasseHashe(utilisateur.getMotDePasseHashe())
                .role(utilisateur.getRole())
                .dateCreation(utilisateur.getDateCreation())
                .dateModification(utilisateur.getDateModification())
                .actif(utilisateur.isActif())
                .build();
    }
    
    public Utilisateur toDomain(UtilisateurEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Utilisateur.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .email(entity.getEmail())
                .motDePasseHashe(entity.getMotDePasseHashe())
                .role(entity.getRole())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .actif(entity.isActif())
                .build();
    }
}
