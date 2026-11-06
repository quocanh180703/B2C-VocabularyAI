package com.vocabularyai.user.repository;

import com.vocabularyai.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity
 * 
 * Provides database operations for users table
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by provider ID (e.g. Google sub)
     */
    Optional<User> findByProviderId(String providerId);

    java.util.List<User> findByRoleIn(java.util.Collection<User.UserRole> roles);
}
