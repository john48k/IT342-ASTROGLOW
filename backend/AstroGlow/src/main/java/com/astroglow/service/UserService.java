package com.astroglow.service;

import com.astroglow.entity.UserEntity;
import com.astroglow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    // Password validation pattern (min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    public UserService() {
        super();
    }

    public UserEntity postUser(UserEntity user) {
        validateUserData(user);
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity putUser(int userId, UserEntity newUserDetails) {
        // First, check if the user exists
        UserEntity existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Update fields if they are provided in the request
        if (newUserDetails.getUserName() != null && !newUserDetails.getUserName().trim().isEmpty()) {
            // Check if new username is already taken by another user
            UserEntity userWithNewUsername = userRepository.findByUserName(newUserDetails.getUserName());
            if (userWithNewUsername != null && userWithNewUsername.getUserId() != userId) {
                throw new IllegalArgumentException("Username already taken");
            }
            existingUser.setUserName(newUserDetails.getUserName());
        }

        if (newUserDetails.getUserEmail() != null && !newUserDetails.getUserEmail().trim().isEmpty()) {
            // Check if new email is already taken by another user
            UserEntity userWithNewEmail = userRepository.findByUserEmail(newUserDetails.getUserEmail());
            if (userWithNewEmail != null && userWithNewEmail.getUserId() != userId) {
                throw new IllegalArgumentException("Email already taken");
            }
            existingUser.setUserEmail(newUserDetails.getUserEmail());
        }

        if (newUserDetails.getUserPassword() != null && !newUserDetails.getUserPassword().trim().isEmpty()) {
            // Encode the new password before saving
            existingUser.setUserPassword(passwordEncoder.encode(newUserDetails.getUserPassword()));
        }

        // Save and return the updated user
        return userRepository.save(existingUser);
    }

    public String deleteUser(int id) {
        String msg= "";
        if(userRepository.findById(id)!=null) {
            userRepository.deleteById(id);
            msg="User successfully deleted.";
        } else
            msg=id + " not found.";
        return msg;
    }

    // Signup Method with enhanced validation
    public UserEntity registerUser(UserEntity user) {
        logger.info("Attempting registration for email: {}", user.getUserEmail());
        validateUserData(user);

        // Check if the email already exists
        if (userRepository.findByUserEmail(user.getUserEmail()) != null) {
            logger.warn("Registration failed: Email already exists: {}", user.getUserEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if the username already exists
        if (userRepository.findByUserName(user.getUserName()) != null) {
            logger.warn("Registration failed: Username already exists: {}", user.getUserName());
            throw new IllegalArgumentException("Username already exists");
        }

        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(encodedPassword);
        logger.info("Registration successful for email: {}", user.getUserEmail());
        return userRepository.save(user);
    }

    // Login Method
    public UserEntity loginUser(String userEmail, String userPassword) {
        UserEntity user = userRepository.findByUserEmail(userEmail);
        if (user != null && passwordEncoder.matches(userPassword, user.getUserPassword())) {
            return user; // Successful login
        }
        return null; // Login failed
    }

    // Validate user data
    private void validateUserData(UserEntity user) {
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
        if (user.getUserName().toLowerCase().contains("admin") ||
            user.getUserName().toLowerCase().contains("moderator")) {
            throw new IllegalArgumentException("Username contains prohibited terms");
        }

        // Validate email
        if (user.getUserEmail() == null || user.getUserEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getUserEmail().length() > 255) {
            throw new IllegalArgumentException("Email is too long");
        }
        if (!EMAIL_PATTERN.matcher(user.getUserEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password for new users or password updates
        if (user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
            if (user.getUserPassword().length() > 128) {
                throw new IllegalArgumentException("Password is too long");
            }
            if (!PASSWORD_PATTERN.matcher(user.getUserPassword()).matches()) {
                throw new IllegalArgumentException(
                    "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                    "one lowercase letter, one number, and one special character"
                );
            }
        }
    }

    /**
     * Change a user's password after verifying the current password
     * 
     * @param userId ID of the user
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     * @return true if password was changed successfully, false if current password is incorrect
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        logger.info("Attempting to change password for user ID: {}", userId);
        
        // Find the user
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            logger.warn("Password change failed: Current password doesn't match for user ID: {}", userId);
            return false;
        }
        
        // Validate new password
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                "one lowercase letter, one number, and one special character"
            );
        }
        
        // Set and save new password
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Password successfully changed for user ID: {}", userId);
        return true;
    }

    /**
     * Find a user by ID
     * 
     * @param userId ID of the user to find
     * @return the user entity
     * @throws IllegalArgumentException if user not found
     */
    public UserEntity findById(int userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    /**
     * Update a user's profile picture
     * 
     * @param userId ID of the user
     * @param profilePictureData Base64 encoded string of the profile picture
     * @return Updated user entity
     */
    public UserEntity updateProfilePicture(int userId, String profilePictureData) {
        logger.info("Updating profile picture for user ID: {}", userId);
        
        // Find the user
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Update the profile picture
        user.setProfilePicture(profilePictureData);
        
        // Save and return the updated user
        return userRepository.save(user);
    }

    public UserEntity findByOauthId(String oauthId) {
        return userRepository.findByOauthId(oauthId);
    }
}
