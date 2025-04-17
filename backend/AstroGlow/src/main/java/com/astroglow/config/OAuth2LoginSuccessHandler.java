package com.astroglow.config;

import com.astroglow.Entity.UserEntity;
import com.astroglow.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();
        
        logger.info("OAuth2 login success. Provider attributes: {}", attributes);
        
        String email = null;
        String name = null;
        
        // Handle different OAuth providers
        if (attributes.containsKey("email")) {
            // Google typically provides email directly
            email = (String) attributes.get("email");
        } else if (attributes.containsKey("emails")) {
            // Some providers might put email in an array
            Object emails = attributes.get("emails");
            if (emails instanceof Iterable) {
                for (Object item : (Iterable<?>) emails) {
                    if (item instanceof Map) {
                        Map<?, ?> emailMap = (Map<?, ?>) item;
                        if (emailMap.containsKey("value")) {
                            email = (String) emailMap.get("value");
                            break;
                        }
                    }
                }
            }
        }
        
        // Get name from attributes
        if (attributes.containsKey("name")) {
            name = (String) attributes.get("name");
        } else if (attributes.containsKey("login")) {
            // GitHub uses "login" for username
            name = (String) attributes.get("login");
        }
        
        if (email != null) {
            // Check if user already exists
            UserEntity existingUser = userRepository.findByUserEmail(email);
            
            if (existingUser == null) {
                // Create new user
                UserEntity newUser = new UserEntity();
                newUser.setUserEmail(email);
                
                // Store the OAuth ID
                String oauthId = (String) attributes.get("sub");
                newUser.setOauthId(oauthId);
                
                // Use name from OAuth or generate one based on email
                if (name != null && !name.isEmpty()) {
                    newUser.setUserName(name);
                } else {
                    // Extract username from email (before @)
                    String username = email.split("@")[0];
                    newUser.setUserName(username);
                }
                
                // Generate a secure random password for OAuth users
                // They won't use this password but we need it for the database
                String randomPassword = UUID.randomUUID().toString();
                newUser.setUserPassword(passwordEncoder.encode(randomPassword));
                
                logger.info("Creating new user from OAuth2 login: {}", email);
                userRepository.save(newUser);
            } else {
                // Update existing user's OAuth ID if it's not set
                if (existingUser.getOauthId() == null) {
                    String oauthId = (String) attributes.get("sub");
                    existingUser.setOauthId(oauthId);
                    userRepository.save(existingUser);
                    logger.info("Updated OAuth ID for existing user: {}", email);
                }
                logger.info("Existing user logged in via OAuth2: {}", email);
            }
        } else {
            logger.warn("Could not extract email from OAuth2 attributes");
        }
        
        // Redirect to our frontend OAuth2 redirect component
        response.sendRedirect("http://localhost:5173/oauth2/redirect");
    }
} 
//Test Commit