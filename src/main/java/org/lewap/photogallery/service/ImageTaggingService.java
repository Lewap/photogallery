package org.lewap.photogallery.service;

import org.lewap.photogallery.llm.*;
import org.lewap.photogallery.model.PhotoEntity;
import org.lewap.photogallery.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
//import java.util.concurrent.CompletableFuture;

@Service
public class ImageTaggingService {

    private static final Logger log = LoggerFactory.getLogger(ImageTaggingService.class);

    private final LLMProviderRegistry registry;
    private final AvailableModelsRegistry availableModelsRegistry;
    private final PhotoRepository photoRepository;

    public ImageTaggingService(LLMProviderRegistry registry, AvailableModelsRegistry availableModelsRegistry, PhotoRepository photoRepository) {
        this.registry = registry;
        this.availableModelsRegistry = availableModelsRegistry;
        this.photoRepository = photoRepository;
    }

    @Value("${llm.tagging.prompt}")
    private String prompt;

    @Async
    public void /*CompletableFuture<Void>*/ tagImages(String providerName, List<String> ids) {

        LLMProvider provider = registry.get(providerName);
        GenerateOptions options = new GenerateOptions();
        List<PhotoEntity> photoEntities = photoRepository.findByIdIn(ids);
        Map<String, String> photos = new HashMap<>();

        for (PhotoEntity photoEntity : photoEntities) {
            photos.put(photoEntity.getId(), photoEntity.getPath());
        }

        provider.generate(prompt, photos, options, new ResultListener() {

            @Override
            public void onResult(String id, String result) {
                Optional<PhotoEntity> photoToSaveOpt = photoEntities.stream().filter(i -> id.equals(i.getId())).findAny();
                if (photoToSaveOpt.isPresent()) {
                    PhotoEntity photoToSave = photoToSaveOpt.get();
                    photoToSave.setTags(result);
                    photoRepository.save(photoToSave);
                    log.info("Tags persisted for id = " + photoToSave.getId() + " TAGS = " + result);
                } else {
                    log.warn("Photo not found on storage " + id);
                }
                //immediate consumption
            }

            @Override
            public void onComplete() {
                log.info("Bulk tagging done");
            }

            @Override
            public void onError(Exception e) {
                log.error("Error encountered while tagging image", e);
            }
        });

        //return CompletableFuture.completedFuture(null);
    }

    public List<String> getLLMModels (String providerName) {

        AvailableModels provider = availableModelsRegistry.get(providerName);
        return provider.getAvailableModels();

    }

}