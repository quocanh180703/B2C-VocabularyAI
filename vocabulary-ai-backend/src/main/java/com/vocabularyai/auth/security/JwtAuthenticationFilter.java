package com.vocabularyai.auth.security;

import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * Intercepts HTTP requests and validates JWT token
 * If valid, sets authentication in SecurityContext
 * 
 * SECURITY FLOW:
 * 1. Extract JWT from Authorization header
 * 2. Validate JWT signature and expiration
 * 3. Extract username and role from JWT
 * 4. ✅ TASK 5: Load user from database and validate status
 * 5. Only ACTIVE users are authenticated
 * 6. INACTIVE/BANNED users are rejected (401)
 * 7. Set authentication in SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                String username = jwtProvider.getUsername(jwt, jwtProperties.getAccessToken().getSecret());
                String role = jwtProvider.getRole(jwt, jwtProperties.getAccessToken().getSecret());
                
                // ✅ TASK 5: Load user from database and validate status
                User user = userRepository.findByUsername(username)
                    .orElse(null);
                
                if (user == null) {
                    log.warn("User not found in database: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // ✅ TASK 5: Validate user status
                if (!isUserActive(user)) {
                    log.warn("User status is not ACTIVE (status: {}, username: {})", 
                             user.getStatus(), username);
                    // Don't set authentication for inactive/banned users
                    // Return 401 Unauthorized when they try to access protected resources
                    filterChain.doFilter(request, response);
                    return;
                }
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        username, null, 
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT token validated for username: {} (status: {})", username, user.getStatus());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if user is ACTIVE
     * 
     * Only ACTIVE users can be authenticated
     * INACTIVE/BANNED users are denied
     * 
     * @param user User to check
     * @return true if user is ACTIVE, false otherwise
     */
    private boolean isUserActive(User user) {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
