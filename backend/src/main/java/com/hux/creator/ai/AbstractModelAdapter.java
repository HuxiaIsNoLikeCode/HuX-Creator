package com.hux.creator.ai;

import java.util.List;

public abstract class AbstractModelAdapter implements ModelAdapter {

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return chat(messages, 0.7);
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages, double temperature) {
        return doChat(messages, temperature);
    }

    protected abstract ChatResponse doChat(List<ChatMessage> messages, double temperature);
}
