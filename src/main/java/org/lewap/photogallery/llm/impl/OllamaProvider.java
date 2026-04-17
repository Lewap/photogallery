package org.lewap.photogallery.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service("ollama")
public class OllamaProvider implements LLMProvider {

    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaProvider(HttpClient client) {
        this.client = client;
    }

    @Override
    public String generate(String inPrompt, List<String> imagePaths, GenerateOptions options) {
        try {
            String body = mapper.writeValueAsString(
                    new Object() {
                        public final String model = "llama3";
                        public final String prompt = inPrompt;
                    }
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .timeout(java.time.Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());
            return root.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException("Ollama call failed", e);
        }
    }
}