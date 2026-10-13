package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.vocabulary.dto.CategoryResponse;
import com.vocabularyai.vocabulary.dto.CreateCategoryRequest;
import com.vocabularyai.vocabulary.dto.UpdateCategoryRequest;
import com.vocabularyai.vocabulary.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Category Controller
 * 
 * API endpoints for category management
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Get all categories
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories with pagination")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting all categories");
        
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Categories retrieved successfully", categories)
        );
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        log.info("Getting category with id: {}", id);
        
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Category retrieved successfully", category)
        );
    }
    
    /**
     * Create new category (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new category",
        description = "Create a new vocabulary category (Admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Category created successfully", category)
        );
    }
    
    /**
     * Update category (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update category",
        description = "Update an existing category (Admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("Updating category with id: {}", id);
        
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
            ApiResponse.success("Category updated successfully", category)
        );
    }
    
    /**
     * Delete category (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete category",
        description = "Delete a category (Admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category with id: {}", id);
        
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
            ApiResponse.success("Category deleted successfully")
        );
    }
}
