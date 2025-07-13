package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.model.Utilisateur;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository {
    Utilisateur save(Utilisateur utilisateur);
    Optional<Utilisateur> findById(UUID id);
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteById(UUID id);
}
