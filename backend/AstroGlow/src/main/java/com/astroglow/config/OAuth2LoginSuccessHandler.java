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
        
        if (attributes.containsKey("email")) {
            email = (String) attributes.get("email");
        } else if (attributes.containsKey("emails")) {
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
        
        if (attributes.containsKey("name")) {
            name = (String) attributes.get("name");
        } else if (attributes.containsKey("login")) {
            name = (String) attributes.get("login");
        }
        
        if (email != null) {
            UserEntity existingUser = userRepository.findByUserEmail(email);
            
            if (existingUser == null) {
                UserEntity newUser = new UserEntity();
                newUser.setUserEmail(email);

                // Store the OAuth ID
                String oauthId = (String) attributes.get("sub");
                newUser.setOauthId(oauthId);
                
                // Use name from OAuth or generate one based on email
                String baseUsername;
                if (name != null && !name.isEmpty()) {
                    baseUsername = name;
                } else {
                    baseUsername = email.split("@")[0];
                }

                String finalUsername = baseUsername;
                int attempt = 1;
                while (userRepository.findByUserName(finalUsername) != null) {
                    finalUsername = baseUsername + "_" + attempt;
                    attempt++;
                }

                newUser.setUserName(finalUsername);

                String randomPassword = UUID.randomUUID().toString();
                newUser.setUserPassword(passwordEncoder.encode(randomPassword));
                
                logger.info("Creating new user from OAuth2 login: {} with username: {}", email, finalUsername);
                userRepository.save(newUser);
            } else {
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
        
        response.sendRedirect("http://localhost:5173/oauth2/redirect");
    }
}
