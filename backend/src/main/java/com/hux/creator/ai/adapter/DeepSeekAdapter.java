package com.hux.creator.ai.adapter;

import com.hux.creator.ai.AbstractModelAdapter;
import com.hux.creator.ai.ChatMessage;
import com.hux.creator.ai.ChatResponse;
import com.hux.creator.ai.ModelProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "hux.ai.providers.deepseek", name = "api-key")
public class DeepSeekAdapter extends AbstractModelAdapter {

    @Value("${hux.ai.providers.deepseek.api-key}")
    private String apiKey;

    @Value("${hux.ai.providers.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${hux.ai.providers.deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate aiRestTemplate;

    @Override
    protected ChatResponse doChat(List<ChatMessage> messages, double temperature) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toList()));
        body.put("temperature", temperature);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> response = aiRestTemplate.postForObject(
                baseUrl + "/chat/completions", request, Map.class);

        return parseResponse(response);
    }

    @Override
    public ModelProvider getProvider() {
        return ModelProvider.DEEPSEEK;
    }

    @SuppressWarnings("unchecked")
    private ChatResponse parseResponse(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        ChatResponse.Usage usageObj = null;
        if (usage != null) {
            usageObj = ChatResponse.Usage.builder()
                    .promptTokens(toInt(usage.get("prompt_tokens")))
                    .completionTokens(toInt(usage.get("completion_tokens")))
                    .totalTokens(toInt(usage.get("total_tokens")))
                    .build();
        }

        return ChatResponse.builder()
                .content(content)
                .model((String) response.get("model"))
                .usage(usageObj)
                .build();
    }

    private Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
}
