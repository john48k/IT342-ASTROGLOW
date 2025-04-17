package com.astroglow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    /**
     * Endpoint that provides login information in JSON format
     * This is helpful when testing with Postman
     */
    @GetMapping("/login-options")
    public ResponseEntity<Map<String, Object>> getLoginOptions() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> formLogin = new HashMap<>();
        formLogin.put("endpoint", "/login");
        formLogin.put("method", "POST");
        formLogin.put("usernameField", "username");
        formLogin.put("passwordField", "password");
        
        Map<String, String> oauth2Options = new HashMap<>();
        oauth2Options.put("github", "/oauth2/authorization/github");
        oauth2Options.put("google", "/oauth2/authorization/google");
        
        response.put("formLogin", formLogin);
        response.put("oauth2Login", oauth2Options);
        response.put("message", "Use these endpoints to authenticate with AstroGlow");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint that responds with login status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Not authenticated");
        response.put("message", "Please login to access this resource");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
} 
//Test Commit