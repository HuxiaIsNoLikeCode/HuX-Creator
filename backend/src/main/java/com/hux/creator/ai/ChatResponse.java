package com.hux.creator.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private String content;
    private String model;
    private Usage usage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
