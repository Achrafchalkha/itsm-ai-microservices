package com.itsm.auth.interfaces.rest;

import com.itsm.auth.domain.model.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", utilisateur.getId());
        profile.put("email", utilisateur.getEmail());
        profile.put("nom", utilisateur.getNom());
        profile.put("prenom", utilisateur.getPrenom());
        profile.put("role", utilisateur.getRole());
        profile.put("actif", utilisateur.isActif());
        
        return ResponseEntity.ok(profile);
    }
}
