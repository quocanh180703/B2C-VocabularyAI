package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.vocabulary.entity.WordTopic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Word count for a specific topic")
public class TopicCountResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Topic code", example = "WORK")
    private WordTopic topic;

    @Schema(description = "Word count for this topic at selected level", example = "120")
    private long count;
}
