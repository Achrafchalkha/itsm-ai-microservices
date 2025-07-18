package com.itsm.auth.interfaces.rest;

import com.itsm.auth.application.dto.AuthResponse;
import com.itsm.auth.application.dto.CreateManagerRequest;
import com.itsm.auth.application.dto.CreateManagerResponse;
import com.itsm.auth.application.service.CreateManagerUseCase;
import com.itsm.auth.infrastructure.security.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final CreateManagerUseCase createManagerUseCase;
    private final JwtProvider jwtProvider;

    @GetMapping("/public-test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Public endpoint working");
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAdminAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Test endpoint - Authentication: {}", auth);
        log.info("Test endpoint - Authorities: {}", auth != null ? auth.getAuthorities() : "null");
        log.info("Test endpoint - Principal: {}", auth != null ? auth.getPrincipal() : "null");
        return ResponseEntity.ok("Admin access test successful. Auth: " + auth);
    }

    @PostMapping("/debug-token")
    public ResponseEntity<String> debugToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            log.info("Received token: {}", token);

            boolean isValid = jwtProvider.validateToken(token);
            log.info("Token valid: {}", isValid);

            if (isValid) {
                String email = jwtProvider.extractEmail(token);
                String role = jwtProvider.extractRole(token);
                String userId = jwtProvider.extractUserId(token);

                log.info("Token email: {}", email);
                log.info("Token role: {}", role);
                log.info("Token userId: {}", userId);

                return ResponseEntity.ok(String.format(
                    "Token Debug - Valid: %s, Email: %s, Role: %s, UserId: %s",
                    isValid, email, role, userId));
            } else {
                return ResponseEntity.ok("Token is invalid");
            }
        } catch (Exception e) {
            log.error("Error debugging token", e);
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @PostMapping("/createmanager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateManagerResponse> createManager(@Valid @RequestBody CreateManagerRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("CreateManager - Authentication: {}", auth);
            log.info("CreateManager - Authorities: {}", auth != null ? auth.getAuthorities() : "null");
            log.info("ADMIN demande de création d'un manager pour l'équipe: {}", request.getTeamName());
            CreateManagerResponse response = createManagerUseCase.execute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du manager: {}", e.getMessage());
            throw e;
        }
    }
}
