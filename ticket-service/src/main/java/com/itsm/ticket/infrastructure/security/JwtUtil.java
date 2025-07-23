package com.itsm.ticket.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Utility class for token operations
 */
@Component
@Slf4j
public class JwtUtil {
    
    private final SecretKey secretKey;
    
    public JwtUtil(@Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}") String jwtSecret) {
        log.info("Initializing JWT Util with secret length: {} bytes", jwtSecret.length());
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        log.info("JWT Util initialized successfully");
    }
    
    /**
     * Extract username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract user ID from token
     */
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }
    
    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }
    
    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract a specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.warn("JWT token is null or empty");
                return false;
            }
            
            // Parse the token to validate signature and structure
            Claims claims = extractAllClaims(token);
            
            // Check if token is expired
            if (isTokenExpired(token)) {
                log.warn("JWT token is expired");
                return false;
            }
            
            // Check if required claims are present
            String userId = claims.get("userId", String.class);
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);
            
            if (userId == null || role == null || email == null) {
                log.warn("JWT token is missing required claims. UserId: {}, Role: {}, Email: {}", 
                        userId, role, email);
                return false;
            }
            
            log.debug("JWT token validation successful for user: {}", email);
            return true;
            
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
