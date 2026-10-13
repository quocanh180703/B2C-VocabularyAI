package com.vocabularyai.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Dummy entity for admin query repositories
 * Used as a placeholder type for JpaRepository to hold query results as maps
 * This is not persisted to database
 */
@Entity(name = "AdminQueryResult")
public class AdminQueryResult {
    @Id
    private Long id;
    
    // Placeholder entity - never used
}
