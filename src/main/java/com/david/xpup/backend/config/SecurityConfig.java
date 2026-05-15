package com.david.xpup.backend.config;

import com.david.xpup.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =========================
                        // SUPERADMIN
                        // =========================

                        .requestMatchers(HttpMethod.PATCH, "/api/admin/users/*/role")
                        .hasRole("SUPERADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/admin/users/*")
                        .hasRole("SUPERADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/*")
                        .hasRole("SUPERADMIN")

                        // =========================
                        // ADMIN / SUPERADMIN
                        // =========================

                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("ADMIN", "SUPERADMIN")

                        // Resto de endpoints autenticados
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}