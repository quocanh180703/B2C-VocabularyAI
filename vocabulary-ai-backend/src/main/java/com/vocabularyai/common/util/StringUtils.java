package com.vocabularyai.common.util;

/**
 * String utility methods
 */
public class StringUtils {
    
    private StringUtils() {}
    
    /**
     * Check if string is blank (null or empty or whitespace)
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Check if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * Check if string is empty (null or empty)
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Check if string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * Trim string safely
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * Get string length safely
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }
    
    /**
     * Convert to lowercase safely
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }
    
    /**
     * Convert to uppercase safely
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }
    
    /**
     * Capitalize first character
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
