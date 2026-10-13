package com.vocabularyai.payment.repository;

import com.vocabularyai.payment.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Package entity
 */
@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    
    /**
     * Find package by unique code
     * @param code Package code (e.g., PREMIUM_MONTHLY, PREMIUM_YEARLY)
     * @return Package if found
     */
    Optional<Package> findByCode(String code);
    
    /**
     * Check if package with code exists
     * @param code Package code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Find all active packages
     * @return List of active packages
     */
    List<Package> findByStatus(String status);
    
    /**
     * Find all active packages sorted by price
     * @return List of active packages
     */
    List<Package> findByStatusOrderByPriceAsc(String status);
}
