package com.astroglow.controller;

import com.astroglow.entity.AuthenticationEntity;
import com.astroglow.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    AuthenticationService aserv;

    @PostMapping("/postAuthentication")
    public AuthenticationEntity postAnalytics(@RequestBody AuthenticationEntity authentication) {
        return aserv.postAuthentication(authentication);
    }

    @GetMapping("/getAllAuthentication")
    public List<AuthenticationEntity> getAllAnalytics() {
        return aserv.getAllAuthentication();
    }

    @PutMapping("/putAuthentication")
    public ResponseEntity<?> putAnalytics(@RequestParam int id, @RequestBody AuthenticationEntity newAuthentication) {
        try {
            AuthenticationEntity updatedAuth = aserv.putAuthentication(id, newAuthentication);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Authentication updated successfully");
            response.put("data", updatedAuth);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating authentication: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/deleteAuthentication/{id}")
    public String deleteAuthentication(@PathVariable int id) {
        return aserv.deleteAuthentication(id);
    }

    @PostMapping("/toggleBiometrics/{userId}")
    public ResponseEntity<?> toggleBiometrics(@PathVariable Long userId, @RequestBody Map<String, Boolean> request) {
        boolean enable = request.getOrDefault("enable", false);
        logger.info("Received request to toggle biometrics for user {} to {}", userId, enable);
        
        try {
            AuthenticationEntity result = aserv.toggleBiometrics(userId, enable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", enable ? "Biometrics enabled successfully" : "Biometrics disabled successfully");
            response.put("data", result);
            
            logger.info("Successfully toggled biometrics for user {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error toggling biometrics for user {}: {}", userId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/checkBiometrics/{userId}")
    public ResponseEntity<?> checkBiometrics(@PathVariable Long userId) {
        logger.info("Checking biometrics status for user {}", userId);
        
        try {
            boolean hasBiometrics = aserv.hasBiometrics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasBiometrics", hasBiometrics);
            
            logger.info("Biometrics status for user {}: {}", userId, hasBiometrics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking biometrics for user {}: {}", userId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
