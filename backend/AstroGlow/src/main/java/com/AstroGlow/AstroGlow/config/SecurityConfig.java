package com.AstroGlow.AstroGlow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/**").permitAll() // Allow access to all endpoints under /users
                        .requestMatchers("/users/print").permitAll() // Explicitly allow access to /users/print
                        .anyRequest().authenticated() // Secure all other endpoints
                )
                .httpBasic(); // Enable basic authentication (optional)

        return http.build();
    }
}