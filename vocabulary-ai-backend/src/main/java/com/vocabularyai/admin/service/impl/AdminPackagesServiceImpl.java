package com.vocabularyai.admin.service.impl;

import com.vocabularyai.admin.dto.request.CreatePackageRequest;
import com.vocabularyai.admin.dto.request.UpdatePackageRequest;
import com.vocabularyai.admin.dto.response.PackageDetailResponse;
import com.vocabularyai.admin.dto.response.PackageListResponse;
import com.vocabularyai.admin.dto.response.PackageStatsResponse;
import com.vocabularyai.admin.repository.AdminPackagesRepository;
import com.vocabularyai.admin.service.AdminPackagesService;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Packages Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPackagesServiceImpl implements AdminPackagesService {
    
    private final AdminPackagesRepository adminPackagesRepository;
    private final PackageRepository packageRepository;
    
    /**
     * Convert map result to PackageListResponse
     */
    private PackageListResponse mapToListResponse(Map<String, Object> result) {
        return PackageListResponse.builder()
                .id(((Number) result.get("id")).longValue())
                .code((String) result.get("code"))
                .name((String) result.get("name"))
                .price((BigDecimal) result.get("price"))
                .durationDays(result.get("durationDays") != null ? ((Number) result.get("durationDays")).intValue() : 0)
                .status((String) result.get("status"))
                .purchaseCount(result.get("totalPurchases") != null ? ((Number) result.get("totalPurchases")).longValue() : 0L)
                .revenue(result.get("revenue") != null ? ((Number) result.get("revenue")).doubleValue() : 0.0)
                .description((String) result.get("description"))
                .createdAt(result.get("createdAt") != null ? result.get("createdAt").toString() : null)
                .updatedAt(result.get("updatedAt") != null ? result.get("updatedAt").toString() : null)
                .build();
    }
    
    /**
     * Convert map result to PackageDetailResponse
     */
    private PackageDetailResponse mapToDetailResponse(Map<String, Object> result) {
        return PackageDetailResponse.builder()
                .id(((Number) result.get("id")).longValue())
                .code((String) result.get("code"))
                .name((String) result.get("name"))
                .price((BigDecimal) result.get("price"))
                .durationDays(result.get("durationDays") != null ? ((Number) result.get("durationDays")).intValue() : 0)
                .status((String) result.get("status"))
                .description((String) result.get("description"))
                .totalPurchases(result.get("totalPurchases") != null ? ((Number) result.get("totalPurchases")).longValue() : 0L)
                .revenueTotal(result.get("revenue") != null ? ((Number) result.get("revenue")).doubleValue() : 0.0)
                .activeSubscriptions(result.get("activeSubscriptions") != null ? ((Number) result.get("activeSubscriptions")).longValue() : 0L)
                .createdAt(result.get("createdAt") != null ? result.get("createdAt").toString() : null)
                .updatedAt(result.get("updatedAt") != null ? result.get("updatedAt").toString() : null)
                .build();
    }
    
    @Override
    public Page<PackageListResponse> listPackages(Pageable pageable) {
        log.debug("Fetching packages - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<?> results = adminPackagesRepository.getPackageList(pageable);
        
        List<PackageListResponse> packages = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(packages, pageable, results.getTotalElements());
    }
    
    @Override
    public Page<PackageListResponse> getPackagesByStatus(String status, Pageable pageable) {
        log.debug("Fetching packages by status: {} - page: {}", status, pageable.getPageNumber());
        
        Page<?> results = adminPackagesRepository.getPackagesByStatus(status, pageable);
        
        List<PackageListResponse> packages = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(packages, pageable, results.getTotalElements());
    }
    
    @Override
    public Page<PackageListResponse> searchPackages(String searchTerm, Pageable pageable) {
        log.debug("Searching packages with term: {} - page: {}", searchTerm, pageable.getPageNumber());
        
        Page<?> results = adminPackagesRepository.searchPackages(searchTerm, pageable);
        
        List<PackageListResponse> packages = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(packages, pageable, results.getTotalElements());
    }
    
    @Override
    public PackageDetailResponse getPackageDetail(Long packageId) {
        log.debug("Fetching package detail for ID: {}", packageId);
        
        List<?> results = adminPackagesRepository.getPackageDetail(packageId);
        
        if (results.isEmpty()) {
            log.warn("Package not found with ID: {}", packageId);
            return null;
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        return mapToDetailResponse(result);
    }
    
    @Override
    public PackageStatsResponse getStats() {
        log.debug("Fetching package statistics");
        
        List<?> results = adminPackagesRepository.getPackageStats();
        
        if (results.isEmpty()) {
            return PackageStatsResponse.builder()
                    .totalPackages(0L)
                    .activePackages(0L)
                    .totalRevenue(0.0)
                    .totalSubscriptions(0L)
                    .build();
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        
        return PackageStatsResponse.builder()
                .totalPackages(((Number) result.get("totalPackages")).longValue())
                .activePackages(((Number) result.get("activePackages")).longValue())
                .totalRevenue(result.get("totalRevenue") != null ? ((Number) result.get("totalRevenue")).doubleValue() : 0.0)
                .totalSubscriptions(((Number) result.get("totalPurchases")).longValue())
                .build();
    }
    
    @Override
    @Transactional
    public PackageDetailResponse createPackage(CreatePackageRequest request) {
        log.info("POST /api/admin/package - Creating package: {}", request.getName());
        
        // Validate required fields
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Package code is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Package name is required");
        }
        if (request.getPrice() == null) {
            throw new IllegalArgumentException("Package price is required");
        }
        
        // Check if code already exists
        if (packageRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Package code already exists: " + request.getCode());
        }
        
        // Create new Package entity
        Package pkg = Package.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName().trim())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .description(request.getDescription())
                .build();
        
        // Save to database
        Package saved = packageRepository.save(pkg);
        
        log.debug("Package created successfully: id={}, code={}", saved.getId(), saved.getCode());
        
        // Convert to response DTO
        return PackageDetailResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .price(saved.getPrice())
                .durationDays(saved.getDurationDays())
                .status(saved.getStatus())
                .description(saved.getDescription())
                .totalPurchases(0L)
                .activeSubscriptions(0L)
                .revenueTotal(0.0)
                .createdAt(saved.getCreatedAt().toString())
                .updatedAt(saved.getUpdatedAt().toString())
                .build();
    }
    
    @Override
    @Transactional
    public PackageDetailResponse updatePackage(Long packageId, UpdatePackageRequest request) {
        log.info("PUT /api/admin/package/{} - Updating package", packageId);
        
        // Find package by ID
        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + packageId));
        
        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            pkg.setName(request.getName().trim());
        }
        if (request.getPrice() != null) {
            pkg.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            pkg.setDurationDays(request.getDurationDays());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            pkg.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            pkg.setDescription(request.getDescription());
        }
        
        // Save updated package
        Package updated = packageRepository.save(pkg);
        
        log.debug("Package updated successfully: id={}, code={}", updated.getId(), updated.getCode());
        
        // Convert to response DTO
        return PackageDetailResponse.builder()
                .id(updated.getId())
                .code(updated.getCode())
                .name(updated.getName())
                .price(updated.getPrice())
                .durationDays(updated.getDurationDays())
                .status(updated.getStatus())
                .description(updated.getDescription())
                .totalPurchases(0L)
                .activeSubscriptions(0L)
                .revenueTotal(0.0)
                .createdAt(updated.getCreatedAt().toString())
                .updatedAt(updated.getUpdatedAt().toString())
                .build();
    }
    
    @Override
    @Transactional
    public void deletePackage(Long packageId) {
        log.info("DELETE /api/admin/package/{} - Deleting package", packageId);
        
        // Find package by ID
        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + packageId));
        
        // Delete from database
        packageRepository.delete(pkg);
        
        log.debug("Package deleted successfully: id={}", packageId);
    }
}
