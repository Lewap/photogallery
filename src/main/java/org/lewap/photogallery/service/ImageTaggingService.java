package org.lewap.photogallery.service;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.LLMProviderRegistry;
import org.lewap.photogallery.model.PhotoEntity;
import org.lewap.photogallery.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@Service
public class ImageTaggingService {

    private static final Logger log = LoggerFactory.getLogger(ImageTaggingService.class);

    private final LLMProviderRegistry registry;
    private final PhotoRepository photoRepository;

    public ImageTaggingService(LLMProviderRegistry registry, PhotoRepository photoRepository) {
        this.registry = registry;
        this.photoRepository = photoRepository;
    }

    @Value("${llm.tagging.prompt}")
    private String prompt;

    @Async
    public void tagImagesAsync(String providerName, List<String> ids) {

        LLMProvider provider = registry.get(providerName);
        GenerateOptions options = new GenerateOptions();
        List<PhotoEntity> photoEntities = photoRepository.findByIdIn(ids);
        Map<String, String> photos = new HashMap<>();

        for (PhotoEntity photoEntity : photoEntities) {
            photos.put(photoEntity.getId(), photoEntity.getPath());
        }

        try (BufferedReader reader = provider.generate(prompt, photos, options)) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("LINE = " + line);
                String id = line.substring(0, line.indexOf(','));
                String tags = line.substring(line.indexOf(',')+1);

                Optional<PhotoEntity> photoToSaveOpt = photoEntities.stream().filter(i -> id.equals(i.getId())).findAny();
                if (photoToSaveOpt.isPresent()) {
                    PhotoEntity photoToSave = photoToSaveOpt.get();
                    photoToSave.setTags(tags);
                    photoRepository.save(photoToSave);
                    log.info("Tags persisted for id = " + photoToSave.getId());
                } else {
                    log.warn("Photo not found on storage " + id);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read LLM output", e);
        }
    }

}