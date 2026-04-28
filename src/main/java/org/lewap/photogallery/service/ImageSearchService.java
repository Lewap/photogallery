package org.lewap.photogallery.service;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.LLMProviderRegistry;
import org.lewap.photogallery.llm.ResultListener;
import org.lewap.photogallery.model.PhotoEntity;
import org.lewap.photogallery.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(ImageSearchService.class);

    private final PhotoRepository photoRepository;
    private final LLMProviderRegistry llmProviderRegistry;

    ImageSearchService (PhotoRepository photoRepository, LLMProviderRegistry llmProviderRegistry) {
        this.photoRepository = photoRepository;
        this.llmProviderRegistry = llmProviderRegistry;
    }

    public void filterImages (String provider, String model, String searchPrompt) {
        List<PhotoEntity> dbPhotos = photoRepository.findAll();
        LLMProvider llmProvider = llmProviderRegistry.get(provider);
        GenerateOptions options = new GenerateOptions();
        Map<String, String> photoTags = new HashMap<>();
        for (PhotoEntity photoEntity : dbPhotos) {
            photoTags.put(photoEntity.getId(), photoEntity.getTags());
        }

        //if (tags != null && !tags.isEmpty() && !tags.equals("null")) {
        llmProvider.generateSearchResponse(searchPrompt, model, photoTags, options);

    }

}
