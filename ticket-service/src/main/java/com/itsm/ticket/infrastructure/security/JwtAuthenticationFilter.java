package com.itsm.ticket.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT Authentication Filter
 * Validates JWT tokens and sets up Spring Security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authorizationHeader != null ? "Bearer [PRESENT]" : "null");

        String username = null;
        String jwt = null;
        UUID userId = null;
        String role = null;

        // Extract JWT token from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            log.debug("Extracted JWT token, length: {}", jwt.length());

            try {
                username = jwtUtil.extractUsername(jwt);
                userId = jwtUtil.extractUserId(jwt);
                role = jwtUtil.extractRole(jwt);

                log.info("Extracted from JWT - Username: {}, UserId: {}, Role: {}", username, userId, role);
            } catch (Exception e) {
                log.error("Error extracting information from JWT token: {}", e.getMessage(), e);
            }
        } else {
            log.debug("No valid Authorization header found");
        }
        
        // If we have a valid token and no authentication is set
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwt)) {
                log.debug("JWT token is valid for user: {}", username);

                // Create authentication token with role
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(authority));

                // Add custom details including user ID
                UserAuthenticationDetails details = new UserAuthenticationDetails(userId, username, role);
                authToken.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("JWT authentication successful for user: {} (ID: {}, Role: {})", username, userId, role);
            } else {
                log.warn("JWT token validation failed for user: {}", username);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Custom authentication details to store user information
     */
    public static class UserAuthenticationDetails {
        private final UUID userId;
        private final String username;
        private final String role;
        
        public UserAuthenticationDetails(UUID userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
        
        public UUID getUserId() {
            return userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getRole() {
            return role;
        }
    }
}
