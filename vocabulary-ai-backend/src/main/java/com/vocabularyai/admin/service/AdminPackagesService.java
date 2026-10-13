package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreatePackageRequest;
import com.vocabularyai.admin.dto.request.UpdatePackageRequest;
import com.vocabularyai.admin.dto.response.PackageDetailResponse;
import com.vocabularyai.admin.dto.response.PackageListResponse;
import com.vocabularyai.admin.dto.response.PackageStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Packages Service Interface
 * 
 * Defines operations for package management from admin perspective
 */
public interface AdminPackagesService {
    
    /**
     * Get paginated list of all packages
     * 
     * @param pageable Pagination parameters
     * @return Page of packages
     */
    Page<PackageListResponse> listPackages(Pageable pageable);
    
    /**
     * Get packages by status
     * 
     * @param status Package status (ACTIVE, INACTIVE, ARCHIVED)
     * @param pageable Pagination parameters
     * @return Page of packages with this status
     */
    Page<PackageListResponse> getPackagesByStatus(String status, Pageable pageable);
    
    /**
     * Search packages
     * 
     * @param searchTerm Search query
     * @param pageable Pagination parameters
     * @return Page of matching packages
     */
    Page<PackageListResponse> searchPackages(String searchTerm, Pageable pageable);
    
    /**
     * Get package detail
     * 
     * @param packageId Package ID
     * @return Package detail
     */
    PackageDetailResponse getPackageDetail(Long packageId);
    
    /**
     * Get package statistics
     * 
     * @return Package statistics
     */
    PackageStatsResponse getStats();
    
    /**
     * Create new package
     * 
     * @param request Create package request
     * @return Created package detail
     */
    PackageDetailResponse createPackage(CreatePackageRequest request);
    
    /**
     * Update existing package
     * 
     * @param packageId Package ID
     * @param request Update package request
     * @return Updated package detail
     */
    PackageDetailResponse updatePackage(Long packageId, UpdatePackageRequest request);
    
    /**
     * Delete package
     * 
     * @param packageId Package ID
     */
    void deletePackage(Long packageId);
}
