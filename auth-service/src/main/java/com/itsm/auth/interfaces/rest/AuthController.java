package com.itsm.auth.interfaces.rest;

import com.itsm.auth.application.dto.AuthResponse;
import com.itsm.auth.application.dto.LoginUserRequest;
import com.itsm.auth.application.dto.RegisterUserRequest;
import com.itsm.auth.application.service.LoginUserUseCase;
import com.itsm.auth.application.service.RegisterUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        try {
            log.info("Demande d'inscription reçue pour l'email: {}", request.getEmail());
            AuthResponse response = registerUserUseCase.execute(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de l'inscription: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginUserRequest request) {
        try {
            log.info("Demande de connexion reçue pour l'email: {}", request.getEmail());
            AuthResponse response = loginUserUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la connexion: {}", e.getMessage());
            throw e;
        }
    }
}
