package org.lewap.photogallery.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lewap.photogallery.llm.AvailableModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service("ollama-models")
public class OllamaAvailableModels implements AvailableModels {

    private static final Logger log = LoggerFactory.getLogger(OllamaAvailableModels.class);

    @Override
    public List<String> getAvailableModels () {
        try {

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            String response = restTemplate.getForObject("http://localhost:11434/api/tags", String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode modelsNode = rootNode.get("models");

            List<String> modelNames = new ArrayList<>();
            if (modelsNode != null && modelsNode.isArray()) {
                for (JsonNode modelNode : modelsNode) {
                    JsonNode nameNode = modelNode.get("name");
                    if (nameNode != null && nameNode.isTextual()) {
                        modelNames.add(nameNode.asText());
                    }
                }
            }
            log.info("Available Ollama models for tagging: " + modelNames);
            return modelNames;
        } catch (Exception e) {
            // Return empty list if Ollama is not available
            log.warn("Cannot get available Ollama models: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
