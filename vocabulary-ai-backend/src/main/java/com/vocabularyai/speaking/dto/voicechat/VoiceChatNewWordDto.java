package com.vocabularyai.speaking.dto.voicechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceChatNewWordDto {
    private String word;
    private String meaning;
    private String example;
    private String wordId;
    private boolean inDictionary;
}
