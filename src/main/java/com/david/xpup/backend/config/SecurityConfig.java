package com.david.xpup.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // desactiva CSRF (necesario para APIs)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 🔥 permite TODO
                );

        return http.build();
    }
}