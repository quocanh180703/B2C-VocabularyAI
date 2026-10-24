package com.vocabularyai.ai.entity;

/**
 * Status of an AI Request
 */
public enum AiRequestLogStatus {
    SUCCESS,
    FAILED,
    CIRCUIT_BREAKER_OPEN,
    FALLBACK
}
