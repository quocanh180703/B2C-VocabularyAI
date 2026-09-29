package com.vocabularyai.payment.entity;

import com.vocabularyai.payment.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Payment entities are properly configured with JPA annotations
 */
@SpringBootTest
public class PaymentSchemaVerificationTest {
    
    @Autowired
    private PackageRepository packageRepository;
    
    @Autowired
    private PaymentOrderRepository paymentOrderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    /**
     * Verify repositories are autowired correctly
     */
    @Test
    public void testRepositoriesAreInjected() {
        assertNotNull(packageRepository, "PackageRepository should be injected");
        assertNotNull(paymentOrderRepository, "PaymentOrderRepository should be injected");
        assertNotNull(paymentRepository, "PaymentRepository should be injected");
        assertNotNull(subscriptionRepository, "SubscriptionRepository should be injected");
    }
    
    /**
     * Verify initial packages were inserted (V3 migration)
     */
    @Test
    public void testInitialPackagesInserted() {
        long packageCount = packageRepository.count();
        assertEquals(2, packageCount, "Should have 2 initial packages (PREMIUM_MONTHLY, PREMIUM_YEARLY)");
    }
    
    /**
     * Verify package codes are created correctly
     */
    @Test
    public void testPackageCodesExist() {
        var premiumMonthly = packageRepository.findByCode("PREMIUM_MONTHLY");
        assertTrue(premiumMonthly.isPresent(), "PREMIUM_MONTHLY package should exist");
        
        var premiumYearly = packageRepository.findByCode("PREMIUM_YEARLY");
        assertTrue(premiumYearly.isPresent(), "PREMIUM_YEARLY package should exist");
    }
    
    /**
     * Verify Package entity has correct properties
     */
    @Test
    public void testPackageProperties() {
        var pkg = packageRepository.findByCode("PREMIUM_MONTHLY").orElseThrow();
        
        assertEquals("PREMIUM_MONTHLY", pkg.getCode());
        assertEquals("Premium Monthly", pkg.getName());
        assertEquals(99000, pkg.getPrice().intValue());
        assertEquals(30, pkg.getDurationDays());
        assertEquals("ACTIVE", pkg.getStatus());
        assertNotNull(pkg.getCreatedAt());
        assertNotNull(pkg.getUpdatedAt());
    }
    
    /**
     * Verify Package yearly subscription
     */
    @Test
    public void testPremiumYearlyPackage() {
        var pkg = packageRepository.findByCode("PREMIUM_YEARLY").orElseThrow();
        
        assertEquals("PREMIUM_YEARLY", pkg.getCode());
        assertEquals("Premium Yearly", pkg.getName());
        assertEquals(899000, pkg.getPrice().intValue());
        assertEquals(365, pkg.getDurationDays());
        assertEquals("ACTIVE", pkg.getStatus());
    }
    
    /**
     * Verify Repository methods exist and are callable
     */
    @Test
    public void testRepositoryMethods() {
        // Test PackageRepository methods
        assertTrue(packageRepository.existsByCode("PREMIUM_MONTHLY"));
        assertFalse(packageRepository.existsByCode("NONEXISTENT"));
        
        var activePackages = packageRepository.findByStatus("ACTIVE");
        assertEquals(2, activePackages.size());
    }
}
