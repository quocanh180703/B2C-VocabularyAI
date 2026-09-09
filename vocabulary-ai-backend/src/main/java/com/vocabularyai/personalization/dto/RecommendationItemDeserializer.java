package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * Custom deserializer cho RecommendationItem
 * Xử lý trường hợp Gemini trả string thay vì object
 */
public class RecommendationItemDeserializer extends JsonDeserializer<RecommendationDto.RecommendationItem> {
    
    @Override
    public RecommendationDto.RecommendationItem deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
        
        // Nếu là string, convert sang RecommendationItem với default values
        if (p.currentToken().isScalarValue()) {
            String text = p.getValueAsString();
            return RecommendationDto.RecommendationItem.builder()
                .priority(1)
                .title(text.length() > 100 ? text.substring(0, 100) : text)
                .description(text)
                .type("PRACTICE")
                .expectedImpact("MEDIUM")
                .effortLevel("MODERATE")
                .estimatedTimeMinutes(30)
                .frequency("mỗi tuần")
                .build();
        }
        
        // Nếu là object, dùng default deserializer
        return ctxt.readValue(p, RecommendationDto.RecommendationItem.class);
    }
}
