package org.lewap.photogallery.llm;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LLMProviderRegistry {

    private final Map<String, LLMProvider> providers;

    public LLMProviderRegistry(Map<String, LLMProvider> providers) {
        this.providers = providers;
    }

    public LLMProvider get(String name) {
        LLMProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider: " + name);
        }
        return provider;
    }
}