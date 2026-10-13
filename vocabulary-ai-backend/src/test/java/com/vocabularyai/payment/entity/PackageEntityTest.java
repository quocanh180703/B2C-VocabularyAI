package com.vocabularyai.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Package entity
 */
public class PackageEntityTest {
    
    private Package pkg;
    
    @BeforeEach
    public void setUp() {
        pkg = new Package();
    }
    
    /**
     * Test Package creation with builder
     */
    @Test
    public void testPackageBuilder() {
        LocalDateTime now = LocalDateTime.now();
        
        Package testPkg = Package.builder()
            .id(1L)
            .code("TEST_PACKAGE")
            .name("Test Package")
            .price(new BigDecimal("100000"))
            .durationDays(30)
            .status("ACTIVE")
            .description("Test description")
            .createdAt(now)
            .updatedAt(now)
            .build();
        
        assertEquals(1L, testPkg.getId());
        assertEquals("TEST_PACKAGE", testPkg.getCode());
        assertEquals("Test Package", testPkg.getName());
        assertEquals(100000, testPkg.getPrice().intValue());
        assertEquals(30, testPkg.getDurationDays());
        assertEquals("ACTIVE", testPkg.getStatus());
        assertEquals("Test description", testPkg.getDescription());
    }
    
    /**
     * Test Package getters and setters
     */
    @Test
    public void testPackageGettersSetters() {
        pkg.setId(1L);
        pkg.setCode("PREMIUM_MONTHLY");
        pkg.setName("Premium Monthly");
        pkg.setPrice(new BigDecimal("99000"));
        pkg.setDurationDays(30);
        pkg.setStatus("ACTIVE");
        pkg.setDescription("Premium subscription for 1 month");
        
        assertEquals(1L, pkg.getId());
        assertEquals("PREMIUM_MONTHLY", pkg.getCode());
        assertEquals("Premium Monthly", pkg.getName());
        assertEquals(99000, pkg.getPrice().intValue());
        assertEquals(30, pkg.getDurationDays());
        assertEquals("ACTIVE", pkg.getStatus());
        assertEquals("Premium subscription for 1 month", pkg.getDescription());
    }
    
    /**
     * Test Package lifecycle callbacks
     */
    @Test
    public void testPackageOnCreateCallback() {
        LocalDateTime beforeCreate = LocalDateTime.now();
        pkg.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now();
        
        assertNotNull(pkg.getCreatedAt());
        assertNotNull(pkg.getUpdatedAt());
        assertTrue(pkg.getCreatedAt().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(pkg.getCreatedAt().isBefore(afterCreate.plusSeconds(1)));
        assertEquals(pkg.getCreatedAt(), pkg.getUpdatedAt());
    }
    
    /**
     * Test Package onUpdate callback
     */
    @Test
    public void testPackageOnUpdateCallback() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        pkg.setCreatedAt(createdTime);
        pkg.setUpdatedAt(createdTime);
        
        LocalDateTime beforeUpdate = LocalDateTime.now();
        pkg.onUpdate();
        LocalDateTime afterUpdate = LocalDateTime.now();
        
        assertEquals(createdTime, pkg.getCreatedAt(), "CreatedAt should not change");
        assertNotNull(pkg.getUpdatedAt());
        assertTrue(pkg.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(pkg.getUpdatedAt().isBefore(afterUpdate.plusSeconds(1)));
        assertTrue(pkg.getUpdatedAt().isAfter(pkg.getCreatedAt()));
    }
    
    /**
     * Test Package with premium monthly values
     */
    @Test
    public void testPremiumMonthlyPackage() {
        pkg.setCode("PREMIUM_MONTHLY");
        pkg.setName("Premium Monthly");
        pkg.setPrice(new BigDecimal("99000"));
        pkg.setDurationDays(30);
        pkg.setStatus("ACTIVE");
        
        assertEquals("PREMIUM_MONTHLY", pkg.getCode());
        assertEquals("Premium Monthly", pkg.getName());
        assertEquals(99000, pkg.getPrice().intValue());
        assertEquals(30, pkg.getDurationDays());
        assertEquals("ACTIVE", pkg.getStatus());
    }
    
    /**
     * Test Package with premium yearly values
     */
    @Test
    public void testPremiumYearlyPackage() {
        pkg.setCode("PREMIUM_YEARLY");
        pkg.setName("Premium Yearly");
        pkg.setPrice(new BigDecimal("899000"));
        pkg.setDurationDays(365);
        pkg.setStatus("ACTIVE");
        
        assertEquals("PREMIUM_YEARLY", pkg.getCode());
        assertEquals("Premium Yearly", pkg.getName());
        assertEquals(899000, pkg.getPrice().intValue());
        assertEquals(365, pkg.getDurationDays());
        assertEquals("ACTIVE", pkg.getStatus());
    }
    
    /**
     * Test Package price comparison
     */
    @Test
    public void testPackagePriceComparison() {
        Package monthlyPkg = Package.builder()
            .price(new BigDecimal("99000"))
            .build();
        
        Package yearlyPkg = Package.builder()
            .price(new BigDecimal("899000"))
            .build();
        
        assertTrue(monthlyPkg.getPrice().compareTo(yearlyPkg.getPrice()) < 0);
        assertTrue(yearlyPkg.getPrice().compareTo(monthlyPkg.getPrice()) > 0);
    }
    
    /**
     * Test Package with different statuses
     */
    @Test
    public void testPackageStatuses() {
        String[] statuses = {"ACTIVE", "INACTIVE", "ARCHIVED"};
        
        for (String status : statuses) {
            pkg.setStatus(status);
            assertEquals(status, pkg.getStatus());
        }
    }
    
    /**
     * Test Package with null description
     */
    @Test
    public void testPackageWithNullDescription() {
        pkg.setDescription(null);
        assertNull(pkg.getDescription());
    }
    
    /**
     * Test Package equality by values
     */
    @Test
    public void testPackageEquality() {
        Package pkg1 = Package.builder()
            .code("PREMIUM_MONTHLY")
            .name("Premium Monthly")
            .price(new BigDecimal("99000"))
            .durationDays(30)
            .status("ACTIVE")
            .build();
        
        Package pkg2 = Package.builder()
            .code("PREMIUM_MONTHLY")
            .name("Premium Monthly")
            .price(new BigDecimal("99000"))
            .durationDays(30)
            .status("ACTIVE")
            .build();
        
        // Same code should indicate same package
        assertEquals(pkg1.getCode(), pkg2.getCode());
    }
}
