package com.astroglow.config;

import com.astroglow.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new OAuth2LoginSuccessHandler(userRepository, passwordEncoder);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public AuthenticationSuccessHandler jsonAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            if (requestWantsJson(request)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, Object> data = new HashMap<>();
                data.put("status", "success");
                data.put("message", "Login successful");
                data.put("user", authentication.getPrincipal());
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(response.getWriter(), data);
            } else {
                // For form-based login, redirect to home
                response.sendRedirect("/home");
            }
        };
    }
    
    @Bean
    public AuthenticationFailureHandler jsonAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            if (requestWantsJson(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, Object> data = new HashMap<>();
                data.put("status", "error");
                data.put("message", "Login failed: " + exception.getMessage());
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(response.getWriter(), data);
            } else {
                // For form-based login, redirect to login page with error
                response.sendRedirect("/login?error=" + exception.getMessage());
            }
        };
    }
    
    private boolean requestWantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        
        return (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) ||
               (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE));
    }

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // Allow all requests without authentication
                )
                // Use our custom success handler for OAuth2
                .oauth2Login(oauth -> oauth
                    .successHandler(oAuth2LoginSuccessHandler))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .formLogin(formLogin -> formLogin
                    .successHandler(jsonAuthenticationSuccessHandler())
                    .failureHandler(jsonAuthenticationFailureHandler())
                )
                .build();
    }
}