package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.CreatePackageRequest;
import com.vocabularyai.admin.dto.request.UpdatePackageRequest;
import com.vocabularyai.admin.dto.response.PackageDetailResponse;
import com.vocabularyai.admin.dto.response.PackageListResponse;
import com.vocabularyai.admin.dto.response.PackageStatsResponse;
import com.vocabularyai.admin.service.AdminPackagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Packages Controller
 * 
 * REST endpoints for package management
 * Base path: /api/admin/packages
 */
@RestController
@RequestMapping("/api/admin/packages")
@Tag(name = "Admin Packages Management", description = "API for managing subscription packages")
@RequiredArgsConstructor
@Slf4j
public class AdminPackagesController {
    
    private final AdminPackagesService adminPackagesService;
    
    /**
     * Get paginated list of all packages
     * 
     * @param pageable Pagination parameters
     * @return Page of packages
     */
    @GetMapping
    @Operation(summary = "List packages", description = "Get paginated list of all packages with optional filtering")
    public ResponseEntity<Page<PackageListResponse>> listPackages(
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, ARCHIVED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search term for name/code")
            @RequestParam(required = false) String search) {
        
        log.info("GET /api/admin/packages - List packages");
        
        Page<PackageListResponse> packages;
        
        if (search != null && !search.trim().isEmpty()) {
            log.debug("Searching packages with term: {}", search);
            packages = adminPackagesService.searchPackages(search, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            log.debug("Filtering packages by status: {}", status);
            packages = adminPackagesService.getPackagesByStatus(status, pageable);
        } else {
            log.debug("Listing all packages");
            packages = adminPackagesService.listPackages(pageable);
        }
        
        return ResponseEntity.ok(packages);
    }
    
    /**
     * Get package detail
     * 
     * @param packageId Package ID
     * @return Package detail
     */
    @GetMapping("/{packageId}")
    @Operation(summary = "Get package detail", description = "Get complete information of a package")
    public ResponseEntity<PackageDetailResponse> getPackageDetail(
            @Parameter(description = "Package ID")
            @PathVariable Long packageId) {
        
        log.info("GET /api/admin/packages/{} - Get package detail", packageId);
        
        PackageDetailResponse detail = adminPackagesService.getPackageDetail(packageId);
        
        if (detail == null) {
            log.warn("Package not found: {}", packageId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(detail);
    }
    
    /**
     * Get package statistics
     * 
     * @return Package statistics
     */
    @GetMapping("/stats/summary")
    @Operation(summary = "Get package statistics", description = "Get overall statistics about packages")
    public ResponseEntity<PackageStatsResponse> getStats() {
        
        log.info("GET /api/admin/packages/stats/summary - Get statistics");
        
        PackageStatsResponse stats = adminPackagesService.getStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Create new package
     * 
     * @param request Create package request
     * @return Created package detail
     */
    @PostMapping
    @Operation(summary = "Create package", description = "Create a new subscription package")
    public ResponseEntity<PackageDetailResponse> createPackage(
            @Valid @RequestBody CreatePackageRequest request) {
        
        log.info("POST /api/admin/packages - Creating package: {}", request.getName());
        
        PackageDetailResponse response = adminPackagesService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update package
     * 
     * @param packageId Package ID
     * @param request Update package request
     * @return Updated package detail
     */
    @PutMapping("/{packageId}")
    @Operation(summary = "Update package", description = "Update an existing subscription package")
    public ResponseEntity<PackageDetailResponse> updatePackage(
            @Parameter(description = "Package ID")
            @PathVariable Long packageId,
            @Valid @RequestBody UpdatePackageRequest request) {
        
        log.info("PUT /api/admin/packages/{} - Updating package", packageId);
        
        PackageDetailResponse response = adminPackagesService.updatePackage(packageId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete package
     * 
     * @param packageId Package ID
     * @return No content
     */
    @DeleteMapping("/{packageId}")
    @Operation(summary = "Delete package", description = "Delete a subscription package")
    public ResponseEntity<Void> deletePackage(
            @Parameter(description = "Package ID")
            @PathVariable Long packageId) {
        
        log.info("DELETE /api/admin/packages/{} - Deleting package", packageId);
        
        adminPackagesService.deletePackage(packageId);
        return ResponseEntity.noContent().build();
    }
}
