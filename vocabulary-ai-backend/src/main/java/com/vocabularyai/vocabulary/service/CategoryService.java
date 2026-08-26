package com.vocabularyai.vocabulary.service;

import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.vocabulary.dto.CategoryResponse;
import com.vocabularyai.vocabulary.dto.CreateCategoryRequest;
import com.vocabularyai.vocabulary.dto.UpdateCategoryRequest;
import com.vocabularyai.vocabulary.entity.Category;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Category Service
 * 
 * Business logic for category management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Get all categories with pagination
     */
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.info("Getting all categories with pagination");
        Page<Category> categories = categoryRepository.findAll(pageable);
        return new PageImpl<>(
            categories.getContent().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            categories.getTotalElements()
        );
    }
    
    /**
     * Get category by ID
     */
    public CategoryResponse getCategoryById(Long categoryId) {
        log.info("Getting category with id: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> ResourceNotFoundException.of("Category", "id", categoryId));
        return CategoryResponse.fromEntity(category);
    }
    
    /**
     * Create new category
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Category name already exists: {}", request.getName());
            throw DuplicateResourceException.of("Category name", request.getName());
        }
        
        Category category = Category.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());
        
        return CategoryResponse.fromEntity(savedCategory);
    }
    
    /**
     * Update category
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        log.info("Updating category with id: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> ResourceNotFoundException.of("Category", "id", categoryId));
        
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                log.warn("Category name already exists: {}", request.getName());
                throw DuplicateResourceException.of("Category name", request.getName());
            }
            category.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", categoryId);
        
        return CategoryResponse.fromEntity(updatedCategory);
    }
    
    /**
     * Delete category
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with id: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> ResourceNotFoundException.of("Category", "id", categoryId));
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", categoryId);
    }
    
    /**
     * Get category by name (internal use)
     */
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
            .orElseThrow(() -> ResourceNotFoundException.of("Category", "name", name));
    }
}
