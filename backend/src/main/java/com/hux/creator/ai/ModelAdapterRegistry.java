package com.hux.creator.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ModelAdapterRegistry {

    private final List<ModelAdapter> adapters;

    private Map<ModelProvider, ModelAdapter> adapterMap;

    private Map<ModelProvider, ModelAdapter> getAdapterMap() {
        if (adapterMap == null) {
            adapterMap = new EnumMap<>(ModelProvider.class);
            for (ModelAdapter adapter : adapters) {
                adapterMap.put(adapter.getProvider(), adapter);
            }
        }
        return adapterMap;
    }

    public ModelAdapter getAdapter(ModelProvider provider) {
        ModelAdapter adapter = getAdapterMap().get(provider);
        if (adapter == null) {
            throw new IllegalArgumentException("不支持的模型提供商: " + provider.getDisplayName());
        }
        return adapter;
    }

    public ModelAdapter getDefaultAdapter() {
        if (adapters.isEmpty()) {
            throw new IllegalStateException("没有可用的模型适配器");
        }
        return adapters.get(0);
    }

    public List<ModelProvider> getAvailableProviders() {
        return List.copyOf(getAdapterMap().keySet());
    }
}
