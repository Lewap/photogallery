package org.lewap.photogallery.llm;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AvailableModelsRegistry {

    private final Map<String, AvailableModels> providers;

    public AvailableModelsRegistry(Map<String, AvailableModels> providers) {
        this.providers = providers;
    }

    public AvailableModels get(String name) {
        AvailableModels provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider: " + name);
        }
        return provider;
    }
}