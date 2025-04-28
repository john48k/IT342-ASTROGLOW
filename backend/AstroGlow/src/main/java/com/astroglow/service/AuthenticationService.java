package com.astroglow.service;

import com.astroglow.entity.AuthenticationEntity;
import com.astroglow.entity.UserEntity;
import com.astroglow.repository.AuthenticationRepository;
import com.astroglow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AuthenticationService {
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
        // Find the user
        Optional<UserEntity> userOptional = userRepository.findById(userId.intValue());
        if (!userOptional.isPresent()) {
            throw new Exception("User not found with ID: " + userId);
        }
        
        UserEntity user = userOptional.get();
        
        // Check if the user already has biometrics
        List<AuthenticationEntity> allAuths = authenticationRepository.findAll();
        AuthenticationEntity existingAuth = null;
        
        for (AuthenticationEntity auth : allAuths) {
            if (auth.getUser() != null && auth.getUser().getUserId() == userId.intValue()) {
                existingAuth = auth;
                break;
            }
        }
        
        if (enable) {
            // Enable biometrics
            if (existingAuth == null) {
                // Create new authentication entry
                AuthenticationEntity newAuth = new AuthenticationEntity();
                newAuth.setUser(user);
                newAuth.setBiometricEnabled(true);
                return authenticationRepository.save(newAuth);
            } else {
                // Update existing authentication
                existingAuth.setBiometricEnabled(true);
                return authenticationRepository.save(existingAuth);
            }
        } else {
            // Disable biometrics
            if (existingAuth != null) {
                // First set biometricEnabled to false
                existingAuth.setBiometricEnabled(false);
                // Save the changes
                authenticationRepository.save(existingAuth);
                // Then delete the authentication
                authenticationRepository.delete(existingAuth);
                return existingAuth;
            }
            // Already disabled
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
        // Find the user
        Optional<UserEntity> userOptional = userRepository.findById(userId.intValue());
        if (!userOptional.isPresent()) {
            throw new Exception("User not found with ID: " + userId);
        }
        
        // Check if the user has biometrics
        List<AuthenticationEntity> allAuths = authenticationRepository.findAll();
        
        for (AuthenticationEntity auth : allAuths) {
            if (auth.getUser() != null && auth.getUser().getUserId() == userId.intValue()) {
                // Check both existence and biometricEnabled status
                return auth.isBiometricEnabled();
            }
        }
        
        return false;
    }
}
