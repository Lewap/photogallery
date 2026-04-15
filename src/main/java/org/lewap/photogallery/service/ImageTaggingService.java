package org.lewap.photogallery.service;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.LLMProviderRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ImageTaggingService {

    private final LLMProviderRegistry registry;

    public ImageTaggingService(LLMProviderRegistry registry) {
        this.registry = registry;
    }

    @Async
    public void tagImageAsync(String providerName, String imageDescription) {

        LLMProvider provider = registry.get(providerName);

        String prompt = """
            Generate concise tags for this image:
            %s
            Return as comma-separated list.
            """.formatted(imageDescription);

        GenerateOptions options = new GenerateOptions();

        String tags = provider.generate(prompt, "null", options);

        // TODO: persist result (DB/file/etc.)
        System.out.println("Generated tags: " + tags);
    }
}