package com.astroglow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection for API endpoints
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/**").permitAll() // Allow all requests to /api/** endpoints
                        .anyRequest().authenticated()
                )
                // .oauth2Login(oauth -> oauth.defaultSuccessUrl("/api/user/user-info", true))
                .oauth2Login(oauth -> oauth.defaultSuccessUrl("http://localhost:5173/Home", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                // .formLogin(formLogin -> formLogin.defaultSuccessUrl("/api/user/user-info", true))
                .formLogin(formLogin ->     formLogin.defaultSuccessUrl("http://localhost:5173/Home", true))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}