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

        /*String prompt = """
            Generate concise tags for this image:
            %s
            Return as comma-separated list.
            """.formatted(imageDescription);*/

        String prompt = "tag this image, return as comma-separated list";

        GenerateOptions options = new GenerateOptions();

        String tags = provider.generate(prompt, "/home/lewap/IdeaProjects/PhotoGallery/uploads/630f6f84-db2d-4c0c-a0bd-2d60a5fd3188.jpg", options);

        // TODO: persist result (DB/file/etc.)
        System.out.println("Generated tags: " + tags);
    }
}