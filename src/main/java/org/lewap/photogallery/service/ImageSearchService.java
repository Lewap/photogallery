package org.lewap.photogallery.service;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.LLMProviderRegistry;
import org.lewap.photogallery.model.PhotoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(ImageSearchService.class);

    private final LLMProviderRegistry llmProviderRegistry;
    private final PhotoService photoService;

    private List<String> filteredPhotoIds;

    public List<String> getFilteredPhotoIds() {
        return filteredPhotoIds;
    }

    ImageSearchService (PhotoService photoService, LLMProviderRegistry llmProviderRegistry) {
        this.photoService = photoService;
        this.llmProviderRegistry = llmProviderRegistry;
    }

    public void filterImages (String provider, String model, String searchPrompt) {
        List<PhotoEntity> dbPhotos = photoService.getAllPhotoEntities();
        LLMProvider llmProvider = llmProviderRegistry.get(provider);
        GenerateOptions options = new GenerateOptions();
        Map<String, String> photoTags = new HashMap<>();
        for (PhotoEntity photoEntity : dbPhotos) {
            photoTags.put(photoEntity.getId(), photoEntity.getTags());
        }

        filteredPhotoIds = llmProvider.generateSearchResponse(searchPrompt, model, photoTags, options);

    }

    public void clearFilter() {
        this.filteredPhotoIds = null;
    }

    public String isFilterActive() {
        if (filteredPhotoIds != null && !filteredPhotoIds.isEmpty()) {
            return "yes";
        } else {
            return "no";
        }
    }

}
