package com.hux.creator.ai;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ModelProvider {

    DOUBAO("豆包"),
    QWEN("通义千问"),
    DEEPSEEK("DeepSeek"),
    GEMINI("Gemini");

    private final String displayName;
}
