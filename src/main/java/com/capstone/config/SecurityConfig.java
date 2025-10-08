package com.capstone.config;

import com.capstone.security.UserContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserContextFilter userContextFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF as we're using JWT tokens
            .csrf(AbstractHttpConfigurer::disable)

                // Disable form login and HTTP basic auth
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // Configure session management - stateless for JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - only actuator health
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                .requestMatchers("/api/v1/roadmap/mentors/assigned-learners/**").hasAnyRole("MENTOR", "ADMIN")

                // Admin-only endpoints (roadmap management)
                .requestMatchers("/api/v1/roadmap/talent-routes/**").hasAnyRole("ADMIN", "LEARNER")
                .requestMatchers("/api/v1/roadmap/growth-tracks/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/roadmap/skill-capsules/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/roadmap/skill-atoms/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/roadmap/mentors/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/roadmap/learner-onboarding/all").hasRole("ADMIN")

                // Mixed access endpoints (roadmap operations)
                .requestMatchers("/api/v1/roadmap").hasAnyRole("ADMIN", "LEARNER")
                .requestMatchers("/api/v1/roadmap/start-progress").hasRole("LEARNER")
                .requestMatchers("/api/v1/roadmap/complete-atom-progress").hasRole("LEARNER")
                .requestMatchers("/api/v1/roadmap/recalculate-progress").hasRole("ADMIN")

                // Learner endpoints (learner view) + Mentor
                .requestMatchers("/api/v1/learner/**").hasAnyRole("LEARNER", "MENTOR")
                .requestMatchers(HttpMethod.POST,"/api/v1/roadmap/learner-onboarding").hasRole("LEARNER")

                // Any other request requires authentication
                .anyRequest().authenticated()
            )

            // Add UserContext filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(userContextFilter, UsernamePasswordAuthenticationFilter.class)

            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
                })
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}