package com.hux.creator.ai;

import java.util.List;

public interface ModelAdapter {

    ChatResponse chat(List<ChatMessage> messages);

    ChatResponse chat(List<ChatMessage> messages, double temperature);

    ModelProvider getProvider();
}
