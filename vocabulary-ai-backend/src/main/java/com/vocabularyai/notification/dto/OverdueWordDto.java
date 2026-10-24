package com.vocabularyai.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueWordDto {
    private String word;
    private String meaning;
    private long overdueDays;
    private String level;
}
