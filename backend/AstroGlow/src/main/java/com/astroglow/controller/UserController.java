package com.astroglow.controller;

import com.astroglow.Entity.UserEntity;
import com.astroglow.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
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

    @PutMapping(value = "/putUser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putUserWithRequestParam(@RequestParam(value = "id", required = true) int id, @RequestBody Map<String, String> userDetails) {
        try {
            UserEntity newUserDetails = new UserEntity();
            
            // Log the received request for debugging
            System.out.println("Received putUser request for id: " + id);
            System.out.println("Request body: " + userDetails);
            
            // Check what fields are provided in the request
            if (userDetails.containsKey("userName")) {
                newUserDetails.setUserName(userDetails.get("userName"));
            }
            
            if (userDetails.containsKey("userEmail")) {
                newUserDetails.setUserEmail(userDetails.get("userEmail"));
            }
            
            if (userDetails.containsKey("userPassword")) {
                newUserDetails.setUserPassword(userDetails.get("userPassword"));
            }
            
            UserEntity updatedUser = userService.putUser(id, newUserDetails);
            
            // Create response without sensitive information
            Map<String, Object> response = new HashMap<>();
            response.put("userId", updatedUser.getUserId());
            response.put("userName", updatedUser.getUserName());
            response.put("userEmail", updatedUser.getUserEmail());
            
            // Add message based on what was updated
            if (userDetails.containsKey("userPassword")) {
                response.put("message", "Password updated successfully");
            } else if (userDetails.containsKey("userName")) {
                response.put("message", "Username updated successfully");
            } else if (userDetails.containsKey("userEmail")) {
                response.put("message", "Email updated successfully");
            } else {
                response.put("message", "User information updated successfully");
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An error occurred while updating the user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/putUser/{id}")
    public ResponseEntity<?> putUser(@PathVariable int id, @RequestBody UserEntity newUserDetails) {
        try {
            UserEntity updatedUser = userService.putUser(id, newUserDetails);
            
            // Create response without sensitive information
            Map<String, Object> response = new HashMap<>();
            response.put("userId", updatedUser.getUserId());
            response.put("userName", updatedUser.getUserName());
            response.put("userEmail", updatedUser.getUserEmail());
            
            // Add message based on what was updated
            if (newUserDetails.getUserPassword() != null && !newUserDetails.getUserPassword().isEmpty()) {
                response.put("message", "Password updated successfully");
            } else if (newUserDetails.getUserName() != null && !newUserDetails.getUserName().isEmpty()) {
                response.put("message", "Username updated successfully");
            } else if (newUserDetails.getUserEmail() != null && !newUserDetails.getUserEmail().isEmpty()) {
                response.put("message", "Email updated successfully");
            } else {
                response.put("message", "User information updated successfully");
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred while updating the user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map<String, Object> response = new HashMap<>();
            
            // Get the OAuth ID (sub) from the attributes
            String oauthId = (String) attributes.get("sub");
            if (oauthId != null) {
                // Find user by OAuth ID
                UserEntity user = userService.findByOauthId(oauthId);
                if (user != null) {
                    response.put("userId", user.getUserId());
                    response.put("userName", user.getUserName());
                    response.put("userEmail", user.getUserEmail());
                    response.put("oauthId", user.getOauthId());
                }
            }
            
            return response;
        } else {
            return Collections.emptyMap();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Invalidate the session if exists
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error during logout");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    @PostMapping("/changePassword/{id}")
    public ResponseEntity<?> changePassword(
            @PathVariable int id, 
            @RequestBody Map<String, String> passwordData) {
        
        try {
            // Validate request data
            if (!passwordData.containsKey("currentPassword") || !passwordData.containsKey("newPassword")) {
                throw new IllegalArgumentException("Current password and new password are required");
            }
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            // Check if current password and new password are empty
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("Current password is required");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("New password is required");
            }
            
            // Call service method to change password
            boolean passwordChanged = userService.changePassword(id, currentPassword, newPassword);
            
            if (passwordChanged) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password changed successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Current password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An error occurred while changing the password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Simple test endpoint to ensure JSON is working properly in Postman
     */
    @GetMapping("/test-json")
    public ResponseEntity<Map<String, String>> testJson() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "JSON is working properly");
        return ResponseEntity.ok(response);
    }

    /**
     * Special endpoint for password changes with simple JSON structure
     */
    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam("id") int id, @RequestBody Map<String, String> passwordData) {
        try {
            if (!passwordData.containsKey("password")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Password is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String newPassword = passwordData.get("password");
            
            // Handle empty password
            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Password cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            UserEntity user = new UserEntity();
            user.setUserPassword(newPassword);
            
            UserEntity updatedUser = userService.putUser(id, user);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Password updated successfully for user: " + updatedUser.getUserName());
            response.put("userId", String.valueOf(updatedUser.getUserId()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Simple test endpoint that always returns JSON to verify Postman can access the API
     */
    @GetMapping(value = "/api-test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> apiTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API is working properly");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Public endpoint specifically for Postman user updates without authentication
     */
    @PutMapping(value = "/public-update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publicUpdateUser(@RequestParam("id") int id, @RequestBody Map<String, String> userDetails) {
        try {
            // Log the received request for debugging
            System.out.println("Received public-update request for id: " + id);
            System.out.println("Request body: " + userDetails);
            
            // Create user entity with provided details
            UserEntity userToUpdate = new UserEntity();
            
            if (userDetails.containsKey("userName")) {
                userToUpdate.setUserName(userDetails.get("userName"));
            }
            
            if (userDetails.containsKey("userEmail")) {
                userToUpdate.setUserEmail(userDetails.get("userEmail"));
            }
            
            if (userDetails.containsKey("userPassword")) {
                userToUpdate.setUserPassword(userDetails.get("userPassword"));
            }
            
            // Call service to update user
            UserEntity updatedUser = userService.putUser(id, userToUpdate);
            
            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User updated successfully");
            response.put("userId", updatedUser.getUserId());
            response.put("userName", updatedUser.getUserName());
            response.put("userEmail", updatedUser.getUserEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Emergency endpoint with minimal security for updating users
     */
    @PutMapping("/emergency-update/{id}")
    public ResponseEntity<?> emergencyUpdate(@PathVariable int id, @RequestBody Map<String, Object> userData) {
        try {
            System.out.println("Received emergency update request for ID: " + id);
            
            UserEntity userToUpdate = new UserEntity();
            
            // Handle string fields
            if (userData.containsKey("userName") && userData.get("userName") != null) {
                userToUpdate.setUserName(userData.get("userName").toString());
            }
            
            if (userData.containsKey("userEmail") && userData.get("userEmail") != null) {
                userToUpdate.setUserEmail(userData.get("userEmail").toString());
            }
            
            if (userData.containsKey("userPassword") && userData.get("userPassword") != null) {
                userToUpdate.setUserPassword(userData.get("userPassword").toString());
            }
            
            UserEntity updatedUser = userService.putUser(id, userToUpdate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User updated successfully via emergency endpoint");
            response.put("userId", updatedUser.getUserId());
            response.put("userName", updatedUser.getUserName());
            response.put("userEmail", updatedUser.getUserEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint to update a user's profile picture
     */
    @PutMapping("/update-profile-picture/{id}")
    public ResponseEntity<?> updateProfilePicture(
            @PathVariable int id, 
            @RequestBody Map<String, String> requestBody) {
        
        try {
            // Log the request for debugging
            System.out.println("Received profile picture update request for user ID: " + id);
            
            // Get the profile picture data from the request body
            if (!requestBody.containsKey("profilePicture")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Profile picture data is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String profilePictureData = requestBody.get("profilePicture");
            
            // Validate the profile picture data (basic check)
            if (profilePictureData == null || profilePictureData.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Profile picture data cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Create a user entity with just the profile picture to update
            UserEntity userToUpdate = new UserEntity();
            userToUpdate.setProfilePicture(profilePictureData);
            
            // Update the user
            UserEntity updatedUser = userService.updateProfilePicture(id, profilePictureData);
            
            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Profile picture updated successfully");
            response.put("userId", updatedUser.getUserId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An error occurred while updating the profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Endpoint to get a user's profile picture
     */
    @GetMapping("/profile-picture/{id}")
    public ResponseEntity<?> getProfilePicture(@PathVariable int id) {
        try {
            UserEntity user = userService.findById(id);
            
            Map<String, Object> response = new HashMap<>();
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                response.put("profilePicture", user.getProfilePicture());
                response.put("status", "success");
            } else {
                response.put("status", "success");
                response.put("message", "No profile picture found");
                response.put("profilePicture", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An error occurred while retrieving the profile picture");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
//Test Commit