package com.vocabularyai.speaking.dto.voicechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceChatScoreResponse {
    private VoiceChatCriterionScoreDto grammar;
    private VoiceChatCriterionScoreDto vocabulary;
    private VoiceChatCriterionScoreDto fluency;
    private VoiceChatCriterionScoreDto content;
    private VoiceChatCriterionScoreDto pronunciation;
    private Integer overall;
    private String summary;
    private List<VoiceChatNewWordDto> newWords;
}
