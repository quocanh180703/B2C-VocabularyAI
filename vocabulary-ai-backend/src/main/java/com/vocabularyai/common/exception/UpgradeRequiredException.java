package com.vocabularyai.common.exception;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception thrown when a free user tries to access a feature/data reserved for Premium.
 * HTTP Status: 429 Too Many Requests (or 403, using 429 as unified quota/paywall interceptor contract)
 */
@Getter
public class UpgradeRequiredException extends RuntimeException {

    private final String feature;
    private final Map<String, Object> data;

    public UpgradeRequiredException(String feature, String message) {
        super(message);
        this.feature = feature;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("feature", feature);
        this.data = map;
    }
}
