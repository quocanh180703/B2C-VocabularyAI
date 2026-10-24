package com.vocabularyai.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pagination utility methods
 */
public class PaginationUtils {
    
    private PaginationUtils() {}
    
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 1000;
    
    /**
     * Create Pageable with default sort
     */
    public static Pageable createPageable(Integer page, Integer size) {
        int pageNum = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(pageNum, pageSize);
    }
    
    /**
     * Create Pageable with custom sort
     */
    public static Pageable createPageable(Integer page, Integer size, Sort.Direction direction, String... properties) {
        int pageNum = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        Sort sort = Sort.by(direction, properties);
        return PageRequest.of(pageNum, pageSize, sort);
    }
    
    /**
     * Create Pageable with sort ascending
     */
    public static Pageable createPageableAsc(Integer page, Integer size, String... properties) {
        return createPageable(page, size, Sort.Direction.ASC, properties);
    }
    
    /**
     * Create Pageable with sort descending
     */
    public static Pageable createPageableDesc(Integer page, Integer size, String... properties) {
        return createPageable(page, size, Sort.Direction.DESC, properties);
    }
    
    /**
     * Create Pageable with sort string (e.g., "createdAt,desc" or "createdAt")
     * Format: "fieldName" (defaults to ASC) or "fieldName,direction" where direction is "asc" or "desc"
     */
    public static Pageable createPageable(Integer page, Integer size, String sort) {
        int pageNum = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(pageNum, pageSize);
        }
        
        String[] parts = sort.split(",");
        String fieldName = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Sort sortObj = Sort.by(direction, fieldName);
        return PageRequest.of(pageNum, pageSize, sortObj);
    }
    
    /**
     * Get default page number
     */
    public static int getDefaultPage() {
        return DEFAULT_PAGE;
    }
    
    /**
     * Get default page size
     */
    public static int getDefaultSize() {
        return DEFAULT_SIZE;
    }
    
    /**
     * Get max page size
     */
    public static int getMaxSize() {
        return MAX_SIZE;
    }
}
