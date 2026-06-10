package com.hux.creator.ai.generator;

import com.hux.creator.ai.ChatMessage;
import com.hux.creator.ai.ChatResponse;
import com.hux.creator.ai.ModelAdapter;
import com.hux.creator.ai.prompt.PromptTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseGenerator {

    protected abstract PromptTemplate getTemplate();

    protected abstract String getSystemPrompt();

    protected abstract String getSectionName();

    public String generate(Map<String, String> variables, ModelAdapter adapter) {
        String rendered = getTemplate().render(variables);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role("system").content(getSystemPrompt()).build());
        messages.add(ChatMessage.builder().role("user").content(rendered).build());
        ChatResponse response = adapter.chat(messages);
        return response.getContent();
    }

    public String optimize(String originalContent, String instruction, ModelAdapter adapter) {
        String prompt = String.format(
                "请根据以下指令优化内容。\n\n【原始内容】\n%s\n\n【优化指令】\n%s\n\n请直接输出优化后的内容，不要添加额外说明。",
                originalContent, instruction);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role("system").content(getSystemPrompt()).build());
        messages.add(ChatMessage.builder().role("user").content(prompt).build());
        ChatResponse response = adapter.chat(messages);
        return response.getContent();
    }
}
