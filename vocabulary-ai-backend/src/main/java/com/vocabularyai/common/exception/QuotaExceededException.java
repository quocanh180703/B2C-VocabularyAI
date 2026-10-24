package com.vocabularyai.common.exception;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception thrown when user exceeds their daily feature quota.
 * HTTP Status: 429 Too Many Requests
 */
@Getter
public class QuotaExceededException extends RuntimeException {

    private final String feature;
    private final int limit;
    private final int used;
    private final int remaining;
    private final String resetAt;
    private final Map<String, Object> data;

    public QuotaExceededException(String feature, int limit, int used, String resetAt, String message) {
        super(message);
        this.feature = feature;
        this.limit = limit;
        this.used = used;
        this.remaining = Math.max(0, limit - used);
        this.resetAt = resetAt;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("feature", feature);
        map.put("limit", limit);
        map.put("used", used);
        map.put("remaining", this.remaining);
        map.put("resetAt", resetAt);
        this.data = map;
    }
}
