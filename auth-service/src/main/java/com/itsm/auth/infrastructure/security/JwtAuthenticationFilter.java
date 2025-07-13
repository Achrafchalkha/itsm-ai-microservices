package com.itsm.auth.infrastructure.security;

import com.itsm.auth.domain.model.Utilisateur;
import com.itsm.auth.domain.repository.UtilisateurRepository;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip JWT processing for public endpoints
        String path = request.getRequestURI();
        log.debug("Processing request path: {}", path);
        if (path.startsWith("/api/auth/") || path.startsWith("/auth/") ||
            path.startsWith("/actuator/") || path.startsWith("/error") ||
            path.equals("/api/admin/debug-token")) {
            log.debug("Skipping JWT processing for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);
            log.debug("Processing request to: {}", path);
            log.debug("JWT token present: {}", StringUtils.hasText(jwt));

            if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
                String email = jwtProvider.extractEmail(jwt);
                String role = jwtProvider.extractRole(jwt);
                log.debug("JWT valid for email: {}, role: {}", email, role);

                Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

                if (utilisateur != null && utilisateur.isActif()) {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
                    );

                    log.debug("Setting authentication for user: {}, authorities: {}", email, authorities);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(utilisateur, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authentication set successfully for user: {}", email);
                } else {
                    log.warn("User not found or inactive for email: {}", email);
                }
            } else {
                log.debug("JWT token invalid or missing");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
