package com.itsm.auth.infrastructure.security;

import com.itsm.auth.domain.model.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;

    public JwtProvider(@Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}") String jwtSecret,
                       @Value("${app.jwt.expiration:86400000}") long jwtExpirationMs) {
        log.info("Initializing JWT Provider with secret length: {} bytes", jwtSecret.length());
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        log.info("JWT Provider initialized successfully");
    }

    public String generateToken(Utilisateur utilisateur) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", utilisateur.getId().toString());
        claims.put("email", utilisateur.getEmail());
        claims.put("nom", utilisateur.getNom());
        claims.put("prenom", utilisateur.getPrenom());
        claims.put("role", utilisateur.getRole().name());

        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(utilisateur.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            log.debug("Validating JWT token...");
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            log.debug("JWT token validation successful");
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected JWT validation error: {}", ex.getMessage(), ex);
        }
        return false;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
