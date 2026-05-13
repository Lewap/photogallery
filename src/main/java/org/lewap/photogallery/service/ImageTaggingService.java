package org.lewap.photogallery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lewap.photogallery.llm.*;
import org.lewap.photogallery.model.PhotoEntity;
import org.lewap.photogallery.model.TaskInfo;
import org.lewap.photogallery.model.TaskStatus;
import org.lewap.photogallery.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String, TaskInfo> tasks = new ConcurrentHashMap<>();

    @Async
    public /*CompletableFuture<Void>*/ void tagImages(String providerName, String model, List<String> ids, String taskId) {

        TaskInfo task = new TaskInfo(TaskStatus.RUNNING, 0, "Tagging...");
        tasks.put(taskId, task);

        LLMProvider provider = registry.get(providerName);
        GenerateOptions options = new GenerateOptions();
        List<PhotoEntity> photoEntities = photoRepository.findByIdIn(ids);
        Map<String, String> photos = new HashMap<>();

        for (PhotoEntity photoEntity : photoEntities) {
            photos.put(photoEntity.getId(), photoEntity.getPath());
        }

        provider.generateImageTags(prompt, model, photos, options, new ResultListener() {

            @Override
            public void onResult(String id, String result) {
                Optional<PhotoEntity> photoToSaveOpt = photoEntities.stream().filter(i -> id.equals(i.getId())).findAny();
                if (photoToSaveOpt.isPresent()) {
                    PhotoEntity photoToSave = photoToSaveOpt.get();
                    if (!isJsonTagPresent(result,"error") && result != null && !result.isEmpty() && !result.equals("null")) {
                        photoToSave.setTags(result);
                        photoRepository.save(photoToSave);
                        log.info("Tags persisted for id = " + photoToSave.getId() + " TAGS = " + result);
                    } else {
                        log.warn("Tags NOT persisted (error or empty) for id = " + photoToSave.getId() + " TAGS = " + result);
                    }
                } else {
                    log.warn("Photo not found on storage " + id);
                }
                //immediate consumption
            }

            @Override
            public void onComplete() {
                log.info("Tagging done");
                task.setProgress(100);
                task.setStatus(TaskStatus.COMPLETED);
                task.setMessage("Tagging done");
            }

            @Override
            public void onError(Exception e) {
                log.warn("Failed to tag image", e);
                task.setStatus(TaskStatus.FAILED);
                task.setMessage("Tagging failed");
            }
        });

        //return CompletableFuture.completedFuture(null);
    }

    @Async
    public void complementTags (String providerName, String model, String taskId) {
        log.info("Complementing tags");
        List<String> ids = new ArrayList<>();
        List<PhotoEntity> photoEntities = photoRepository.findByTagsIsNull();
        if (!photoEntities.isEmpty()) {
            for (PhotoEntity photoEntity : photoEntities) {
                ids.add(photoEntity.getId());
            }
            tagImages(providerName, model, ids, taskId);
        } else {
            log.info("No photos with empty tags found - not complementing");
        }

    }

    public List<String> getLLMModels (String providerName) {

        AvailableModels provider = availableModelsRegistry.get(providerName);
        return provider.getAvailableModels();

    }

    private boolean isJsonTagPresent(String json, String tag) {
        // Parse the JSON response and extract the "response" field
        boolean res = false;
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode responseNode = rootNode.get(tag);

            if (responseNode != null && responseNode.isTextual()) {
                res = true;
            }
        } catch (Exception e) {
            log.warn("Failed to parse json " + e.getMessage());
        }

        return res;
    }

    public TaskInfo getTask(String taskId) {
        return tasks.getOrDefault(
                taskId,
                new TaskInfo(TaskStatus.FAILED, 0, "Task not found")
        );
    }

}