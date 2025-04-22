package com.astroglow.Service;

import com.astroglow.Entity.AuthenticationEntity;
import com.astroglow.Entity.UserEntity;
import com.astroglow.Repository.AuthenticationRepository;
import com.astroglow.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NameNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    AuthenticationRepository authenticationRepository;
    
    @Autowired
    UserRepository userRepository;

    public AuthenticationService() {

    }

    public AuthenticationEntity postAuthentication(AuthenticationEntity authentication) {
        return authenticationRepository.save(authentication);
    }

    public List<AuthenticationEntity> getAllAuthentication() {
        return authenticationRepository.findAll();
    }

    @SuppressWarnings("finally")
    public AuthenticationEntity putAuthentication(int id, AuthenticationEntity newAuthentication) {
        AuthenticationEntity authentication = new AuthenticationEntity();
        try {
            authentication = authenticationRepository.findById(id).get();
            authentication.setUser(newAuthentication.getUser());
            authentication.setUserBiometricId(newAuthentication.getUserBiometricId());
            authentication.setBiometricEnabled(newAuthentication.isBiometricEnabled());
            logger.info("Updated authentication record for ID {} with biometric_enabled={}", id, newAuthentication.isBiometricEnabled());
        } catch (NoSuchElementException nex) {
            throw new NameNotFoundException("Authentication is " + id + " not found!");
        } finally {
            return authenticationRepository.save(authentication);
        }
    }

    public String deleteAuthentication(int id) {
        String msg = "";
        if (authenticationRepository.existsById(id)) {
            authenticationRepository.deleteById(id);
            msg = "Biometrics Successfully deleted.";
        } else {
            msg = id + " not found.";
        }
        return msg;
    }
    
    /**
     * Toggle biometrics authentication for a user
     * @param userId The user ID
     * @param enable Whether to enable or disable biometrics
     * @return The updated AuthenticationEntity
     * @throws Exception if the user doesn't exist
     */
    public AuthenticationEntity toggleBiometrics(Long userId, boolean enable) throws Exception {
        logger.info("Toggling biometrics for user {} to {}", userId, enable);
        
        // Find the user by converting Long to Integer
        Optional<UserEntity> userOptional = userRepository.findById(userId.intValue());
        if (!userOptional.isPresent()) {
            logger.error("User not found with ID: {}", userId);
            throw new Exception("User not found with ID: " + userId);
        }
        
        UserEntity user = userOptional.get();
        
        // Check if authentication record exists
        Optional<AuthenticationEntity> existingAuth = authenticationRepository.findAll().stream()
                .filter(auth -> auth.getUser().getUserId() == userId.intValue())
                .findFirst();

        if (enable) {
            // Enable biometrics
            if (existingAuth.isPresent()) {
                // Update existing record using putAuthentication
                AuthenticationEntity auth = existingAuth.get();
                AuthenticationEntity updatedAuth = new AuthenticationEntity();
                updatedAuth.setUser(user);
                updatedAuth.setUserBiometricId(auth.getUserBiometricId());
                updatedAuth.setBiometricEnabled(true);
                logger.info("Updating existing biometric record for user {}", userId);
                return putAuthentication(auth.getUserBiometricId(), updatedAuth);
            } else {
                // Create new record
                AuthenticationEntity newAuth = new AuthenticationEntity();
                newAuth.setUserBiometricId(1); // Default ID for new records
                newAuth.setBiometricEnabled(true);
                newAuth.setUser(user);
                logger.info("Creating new biometric record for user {}", userId);
                return authenticationRepository.save(newAuth);
            }
        } else {
            // Disable biometrics
            if (existingAuth.isPresent()) {
                // Update the record using putAuthentication
                AuthenticationEntity auth = existingAuth.get();
                AuthenticationEntity updatedAuth = new AuthenticationEntity();
                updatedAuth.setUser(user);
                updatedAuth.setUserBiometricId(auth.getUserBiometricId()); // Keep the same ID
                updatedAuth.setBiometricEnabled(false);
                logger.info("Disabling biometrics for user {}", userId);
                return putAuthentication(auth.getUserBiometricId(), updatedAuth);
            }
            logger.info("No biometric record found for user {}, nothing to disable", userId);
            return null;
        }
    }

    /**
     * Check if a user has biometrics enabled
     * @param userId The user ID
     * @return true if biometrics are enabled, false otherwise
     * @throws Exception if the user doesn't exist
     */
    public boolean hasBiometrics(Long userId) throws Exception {
        logger.info("Checking biometrics for user {}", userId);
        
        // Find the user
        Optional<UserEntity> userOptional = userRepository.findById(userId.intValue());
        if (!userOptional.isPresent()) {
            logger.error("User not found with ID: {}", userId);
            throw new Exception("User not found with ID: " + userId);
        }
        
        // Check if the user has biometrics and if it's enabled
        boolean hasBiometrics = authenticationRepository.findAll().stream()
                .anyMatch(auth -> auth.getUser().getUserId() == userId.intValue() && 
                        auth.isBiometricEnabled());
        
        logger.info("Biometrics for user {}: {}", userId, hasBiometrics);
        return hasBiometrics;
    }
}
