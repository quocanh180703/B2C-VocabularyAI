package com.vocabularyai.auth.security;

import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom User Details Service
 * 
 * Implements Spring Security's UserDetailsService
 * Used to load user details from database for authentication
 * 
 * SECURITY FLOW:
 * 1. Username/password authentication (login)
 * 2. Load user from database
 * 3. Validate user status (ACTIVE only)
 * 4. Return UserDetails if ACTIVE
 * 5. Throw exception if INACTIVE or BANNED
 * 
 * Status Validation:
 * - ACTIVE: ✅ Allow authentication
 * - INACTIVE: ❌ Account suspended (throw exception)
 * - BANNED: ❌ Account banned (throw exception)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("User not found with username: {}", username);
                return new UsernameNotFoundException("User not found with username: " + username);
            });
        
        // ✅ TASK 5: Validate user status
        validateUserStatus(user);
        
        log.debug("User loaded successfully for username: {} (status: {})", username, user.getStatus());
        return user;
    }
    
    /**
     * Validate user status before authentication
     * 
     * @param user User to validate
     * @throws UsernameNotFoundException If user is INACTIVE or BANNED
     */
    private void validateUserStatus(User user) {
        if (user.getStatus() == User.UserStatus.INACTIVE) {
            log.warn("User account is inactive (suspended): {}", user.getUsername());
            throw new UsernameNotFoundException(
                "User account is inactive (suspended). Please contact support."
            );
        }
        
        if (user.getStatus() == User.UserStatus.BANNED) {
            log.warn("User account is banned: {}", user.getUsername());
            throw new UsernameNotFoundException(
                "User account is banned. Please contact support for more information."
            );
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("User account has invalid status: {}", user.getStatus());
            throw new UsernameNotFoundException(
                "User account has invalid status. Please contact support."
            );
        }
    }
}
