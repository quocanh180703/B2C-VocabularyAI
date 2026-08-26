package com.vocabularyai.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time utility methods
 */
public class DateUtils {
    
    private DateUtils() {}
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Get current datetime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * Format LocalDateTime to string with default format
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * Format LocalDateTime with custom format
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Parse string to LocalDateTime with default format
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (StringUtils.isBlank(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
    }
    
    /**
     * Parse string to LocalDateTime with custom format
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (StringUtils.isBlank(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Check if first date is after second date
     */
    public static boolean isAfter(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return false;
        }
        return first.isAfter(second);
    }
    
    /**
     * Check if first date is before second date
     */
    public static boolean isBefore(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return false;
        }
        return first.isBefore(second);
    }
    
    /**
     * Add minutes to LocalDateTime
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }
    
    /**
     * Add hours to LocalDateTime
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }
    
    /**
     * Add days to LocalDateTime
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }
}
