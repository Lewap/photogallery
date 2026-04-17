package org.lewap.photogallery.service;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.LLMProviderRegistry;
import org.lewap.photogallery.model.PhotoEntity;
import org.lewap.photogallery.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageTaggingService {

    private final LLMProviderRegistry registry;
    private final PhotoRepository photoRepository;

    public ImageTaggingService(LLMProviderRegistry registry, PhotoRepository photoRepository) {
        this.registry = registry;
        this.photoRepository = photoRepository;
    }

    @Async
    public void tagImagesAsync(String providerName, List<String> ids) {

        LLMProvider provider = registry.get(providerName);
        String prompt = "tag this image, return as comma-separated list";
        GenerateOptions options = new GenerateOptions();
        List<PhotoEntity> photoEntities = photoRepository.findByIdIn(ids);
        List<String> photoPaths = new ArrayList<>();

        for (PhotoEntity photoEntity : photoEntities) {
            photoPaths.add(photoEntity.getPath());
        }

        String tags = provider.generate(prompt, photoPaths, options);

        // TODO: persist result (DB/file/etc.)
        System.out.println("Generated tags: " + tags);
    }

}