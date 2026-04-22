package org.lewap.photogallery.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

@Service("ollama")
public class OllamaProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);

    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaProvider(HttpClient client) {
        this.client = client;
    }

    @Override
    public BufferedReader generate(String inPrompt, Map<String, String> images, GenerateOptions options) {
        try {
            // Create the request body with prompt and image data
            StringBuilder requestBody = new StringBuilder();
            requestBody.append("{");
            requestBody.append("\"model\":\"llama3.2-vision:11b\",");
            requestBody.append("\"prompt\":").append(mapper.writeValueAsString(inPrompt)).append(",");

            // Add images to the request
            if (images != null && !images.isEmpty()) {
                requestBody.append("\"images\":[");
                boolean first = true;
                for (Map.Entry<String, String> entry : images.entrySet()) {
                    if (!first) {
                        requestBody.append(",");
                    }
                    first = false;

                    // Read image file and encode to base64
                    String imagePath = entry.getValue();
                    byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    requestBody.append("\"").append(base64Image).append("\"");
                }
                requestBody.append("],");
            }

            requestBody.append("\"stream\":false,");
            requestBody.append("\"options\":{\"num_predict\":20, \"repeat_penalty\": 1.2, \"temperature\": 0.1}");
            requestBody.append("}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .timeout(java.time.Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            String processedContent = processResponse(responseBody, images);

            // Return a BufferedReader wrapping the response
            return new BufferedReader(new StringReader(processedContent));

        } catch (Exception e) {
            throw new RuntimeException("Ollama call failed", e);
        }
    }

    private String processResponse(String responseBody, Map<String, String> images) {
        // Parse the JSON response and extract the "response" field
        try {
            JsonNode rootNode = mapper.readTree(responseBody);
            JsonNode responseNode = rootNode.get("response");

            //llava3.2:11b accepts only 1 image in the request, so taking the first id to concatenate with the description is OK
            Map.Entry<String, String> firstEntry = images.entrySet().iterator().next();
            String id = firstEntry.getKey();

            if (responseNode != null && responseNode.isTextual()) {
                String rawResponse = responseNode.asText();
                // Process the raw response - for example, clean up any extra whitespace
                return id + "," + rawResponse.trim() + "\n";
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama response: {}. The original response will be returned", e.getMessage());
        }

        // Return original response if parsing fails
        return responseBody + "\n";
    }

}