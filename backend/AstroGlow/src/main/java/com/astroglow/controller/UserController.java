package com.astroglow.controller;

import com.astroglow.Entity.UserEntity;
import com.astroglow.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UserController {
    @GetMapping
    public String index(){
        return "<h1>Welcome, This is a landing page</h1>";
    }
    @Autowired
    UserService userService;

    @PostMapping(value ="/postUser", consumes = "application/json")
    public UserEntity postUser(@RequestBody UserEntity user) {
        return userService.postUser(user);
    }

    @GetMapping("/getAllUser")
    public List<UserEntity> getAllUser(){
        return userService.getAllUsers();
    }

    @PutMapping("/putUser")
    public UserEntity putUser(@RequestParam int id, @RequestBody UserEntity newUserDetails) {
        return userService.putUser(id, newUserDetails);
    }

    @DeleteMapping("/deleteUser/{id}")  // Fixed path variable in URL
    public String deleteUser(@PathVariable int id) {
        return userService.deleteUser(id);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserEntity user) {
        try {
            validateSignupData(user);
            UserEntity newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred during signup");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity user) {
        try {
            // Validate login data
            if (user.getUserEmail() == null || user.getUserEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (user.getUserPassword() == null || user.getUserPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }

            UserEntity loggedInUser = userService.loginUser(user.getUserEmail(), user.getUserPassword());
            if (loggedInUser != null) {
                return ResponseEntity.ok(loggedInUser);
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred during login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // This is for the Google and Github JSON user-info
    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User != null) {
            return oAuth2User.getAttributes();
        } else {
            return Collections.emptyMap();
        }
    }

    // Helper method to validate signup data
    private void validateSignupData(UserEntity user) {
        // Validate username
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getUserName().length() < 3 || user.getUserName().length() > 30) {
            throw new IllegalArgumentException("Username must be between 3 and 30 characters");
        }
        if (!user.getUserName().matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }

        // Validate email
        if (user.getUserEmail() == null || user.getUserEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!user.getUserEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password
        if (user.getUserPassword() == null || user.getUserPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getUserPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!user.getUserPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")) {
            throw new IllegalArgumentException("Password must include at least one uppercase letter, one lowercase letter, one number, and one special character");
        }
    }
}