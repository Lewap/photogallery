package org.lewap.photogallery.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.ResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service("ollama")
public class OllamaProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);

    @Value("${llm.ollama.url}")
    private String ollamaUrl;

    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaProvider(HttpClient client) {
        this.client = client;
    }

    @Override
    public List<String> generateSearchResponse(String searchPrompt,
                                               String model,
                                               Map<String, String> photoTags,
                                               GenerateOptions options) {

        log.info("Ollama searching started using model " + model);

        List<String> res = new ArrayList<>();

        for (Map.Entry<String, String> entry : photoTags.entrySet()) {

            String prompt = "does this text '" + entry.getValue() + "' relate to this one: '" + searchPrompt + "'? RULES: respond YES or NO, no empty response, no other text";

            try {
                String body = """
                        {
                          "model": "%s",
                          "prompt": "%s",
                          "stream": false,
                          "options": {"num_predict": 1, "repeat_penalty": 1.2, "temperature": 0}
                        }
                        """.formatted(model, prompt);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ollamaUrl + "/api/generate"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                String LLMResponse = parse(response.body());
                String photoID = entry.getKey();
                log.info("SEARCH id = " + photoID + " search result: " + LLMResponse);
                if ("Y".equalsIgnoreCase(LLMResponse) || "YES".equalsIgnoreCase(LLMResponse)) {
                    res.add(photoID);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        return res;

    }

    @Override
    public void generateImageTags(String prompt, String model, Map<String, String> images, GenerateOptions options, ResultListener listener) {
        log.info("Ollama tagging started using model " + model);
        try {

            if (images != null && !images.isEmpty()) {
                boolean first = true;
                for (Map.Entry<String, String> entry : images.entrySet()) {
                    // Read image file and encode to base64
                    String imagePath = entry.getValue();
                    byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    String result = tagSingle(base64Image, prompt, model); //blocking
                    listener.onResult(entry.getKey(), result);   // emit immediately
                }
            }
            listener.onComplete();
        } catch (Exception e) {
            listener.onError(e);
        }
    }

    public String tagSingle(String base64Image, String prompt, String model) {
        try {
            String body = """
            {
              "model": "%s",
              "prompt": "%s",
              "images": ["%s"],
              "stream": false,
              "options": {"num_predict": 20, "repeat_penalty": 1.2, "temperature": 0.1}
            }
            """.formatted(model, prompt, base64Image);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return parse(response.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String parse(String responseBody) {
        // Parse the JSON response and extract the "response" field
        try {
            JsonNode rootNode = mapper.readTree(responseBody);
            JsonNode responseNode = rootNode.get("response");

            if (responseNode != null && responseNode.isTextual()) {
                String rawResponse = responseNode.asText();
                return rawResponse.trim() /*+ "\n"*/;
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama response: {}. The original response will be returned", e.getMessage());
        }

        // Return original response if parsing fails
        return responseBody /*+ "\n"*/;
    }

}