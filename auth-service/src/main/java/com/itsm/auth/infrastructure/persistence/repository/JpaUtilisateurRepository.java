package com.itsm.auth.infrastructure.persistence.repository;

import com.itsm.auth.infrastructure.persistence.entity.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUtilisateurRepository extends JpaRepository<UtilisateurEntity, UUID> {
    Optional<UtilisateurEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
