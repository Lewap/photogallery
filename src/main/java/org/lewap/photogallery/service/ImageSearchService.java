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

import java.util.List;
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

        log.info("searchPrompt = " + searchPrompt);

        for (PhotoEntity photoEntity : dbPhotos) {
            String tags = photoEntity.getTags();
            String id = photoEntity.getId();
            log.info("photoID = " + id + " TAGS = " + tags);
            String prompt = "does this text: '" + photoEntity.getTags() + "' relate to '" + searchPrompt + "'? Rules: respond YES or NO, response must not be empty";

            if (tags != null && !tags.isEmpty() && !tags.equals("null")) {
                llmProvider.generate(prompt, model, null, options, new ResultListener() {

                    @Override
                    public void onResult(String id, String result) {
                        log.info("prompt sent to the model: " + prompt);
                        log.info("Id = " + id + " RESPONSE = " + result);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Search complete");
                    }

                    @Override
                    public void onError(Exception e) {
                        log.error("Error encountered while tagging image", e);
                    }
                });
            } else {
                log.info("no tags for photo: " + id);
            }
        }

    }

}
