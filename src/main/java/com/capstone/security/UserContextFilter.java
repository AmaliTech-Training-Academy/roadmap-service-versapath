package com.capstone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class UserContextFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);

        if (userId != null && userRole != null && userEmail != null) {
            log.debug("Setting authentication context for user: {} with role: {}", userId, userRole);

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.toUpperCase());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(authority));

            authToken.setDetails(new UserContext(userId, userRole, userEmail));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("Authentication context set successfully for user: {}", userId);
        } else {
            log.debug("Missing required headers - userId: {}, userRole: {}, userEmail: {}",
                    userId != null, userRole != null, userEmail != null);
        }

        filterChain.doFilter(request, response);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class UserContext {
        private String userId;
        private String role;
        private String email;
    }
}