package com.vocabularyai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vocabularyai.admin.entity.AdminQueryResult;

import java.util.List;

/**
 * Admin Packages Repository
 * 
 * Provides queries for package management (admin system-wide view)
 * Supports listing, searching, and filtering packages
 */
@Repository
public interface AdminPackagesRepository extends JpaRepository<AdminQueryResult, Long> {
    
    /**
     * Get paginated list of all packages
     * 
     * @param pageable Pagination and sorting
     * @return Page of packages
     */
    @Query("SELECT new map(" +
           "p.id as id, " +
           "p.code as code, " +
           "p.name as name, " +
           "p.price as price, " +
           "p.durationDays as durationDays, " +
           "p.status as status, " +
           "0 as totalPurchases, " +
           "0.0 as revenue, " +
           "p.description as description, " +
           "p.createdAt as createdAt, " +
           "p.updatedAt as updatedAt) " +
           "FROM Package p " +
           "ORDER BY p.createdAt DESC")
    Page<?> getPackageList(Pageable pageable);
    
    /**
     * Get packages by status
     * 
     * @param status Package status (ACTIVE, INACTIVE, ARCHIVED)
     * @param pageable Pagination
     * @return Page of packages with this status
     */
    @Query("SELECT new map(" +
           "p.id as id, " +
           "p.code as code, " +
           "p.name as name, " +
           "p.price as price, " +
           "p.durationDays as durationDays, " +
           "p.status as status, " +
           "0 as totalPurchases, " +
           "0.0 as revenue, " +
           "p.description as description, " +
           "p.createdAt as createdAt, " +
           "p.updatedAt as updatedAt) " +
           "FROM Package p " +
           "WHERE p.status = :status " +
           "ORDER BY p.createdAt DESC")
    Page<?> getPackagesByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * Search packages by name or code
     * 
     * @param searchTerm Search query (case-insensitive)
     * @param pageable Pagination
     * @return Page of matching packages
     */
    @Query("SELECT new map(" +
           "p.id as id, " +
           "p.code as code, " +
           "p.name as name, " +
           "p.price as price, " +
           "p.durationDays as durationDays, " +
           "p.status as status, " +
           "0 as totalPurchases, " +
           "0.0 as revenue, " +
           "p.description as description, " +
           "p.createdAt as createdAt, " +
           "p.updatedAt as updatedAt) " +
           "FROM Package p " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<?> searchPackages(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Get package detail
     * 
     * @param packageId Package ID
     * @return Map with package details
     */
    @Query("SELECT new map(" +
           "p.id as id, " +
           "p.code as code, " +
           "p.name as name, " +
           "p.price as price, " +
           "p.durationDays as durationDays, " +
           "p.status as status, " +
           "p.description as description, " +
           "0 as totalPurchases, " +
           "0.0 as revenue, " +
           "0 as activeSubscriptions, " +
           "p.createdAt as createdAt, " +
           "p.updatedAt as updatedAt) " +
           "FROM Package p " +
           "WHERE p.id = :packageId")
    List<?> getPackageDetail(@Param("packageId") Long packageId);
    
    /**
     * Get package statistics
     * 
     * @return Map with package statistics
     */
    @Query("SELECT new map(" +
           "COUNT(p) as totalPackages, " +
           "COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END) as activePackages, " +
           "0.0 as totalRevenue, " +
           "0 as totalPurchases) " +
           "FROM Package p")
    List<?> getPackageStats();
}
