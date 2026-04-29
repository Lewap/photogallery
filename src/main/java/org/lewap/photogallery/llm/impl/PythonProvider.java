package org.lewap.photogallery.llm.impl;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.llm.ResultListener;
import org.lewap.photogallery.util.FilePathParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("python-vision")
public class PythonProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(PythonProvider.class);

    @Value("${llm.python.vision.script}")
    private String visionScript;

    @Value("${llm.python.text.script}")
    private String textScript;

    @Value("${llm.python.executable}")
    private String pythonExecutable;

    private Path scriptFromResource;

    public void setScriptFromResource(String script) {
        try {

            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(script);

            if (inputStream == null) {
                throw new RuntimeException("Could not find resource: " + script);
            }

            // Create temporary file from the resource
            scriptFromResource = createTempScriptFromResource(inputStream, script);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set temp script path", e);
        }
    }

    private Path createTempScriptFromResource(InputStream inputStream, String script) throws IOException {

        FilePathParser filePathParser = new FilePathParser(script);
        Path tempFile = Files.createTempFile(filePathParser.getFileNameWithoutExtension(), filePathParser.getFileExtension());
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit(); // Clean up on exit
        return tempFile;
    }

    @Override
    public List<String> generateSearchResponse (
            String searchPrompt,
            String model,
            Map<String, String> photoTags,
            GenerateOptions options
    ) {
        log.info("Search for the PythonProvider not yet implemented");
        return new ArrayList<>();
    }

    @Override
    public void generateImageTags(
            String prompt,
            String model,
            Map<String, String> images,
            GenerateOptions options,
            ResultListener listener
    ) {
        log.info("Python Vision tagging started");
        try {

            setScriptFromResource(visionScript);

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(pythonExecutable, scriptFromResource.toString());

            for (Map.Entry<String, String> entry : images.entrySet()) {
                pb.command().add(entry.getKey());
                pb.command().add(entry.getValue());
            }

            // Add prompt as last argument
            pb.command().add(prompt);

            // Merge stderr into stdout for easier debugging
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String id = line.substring(0, line.indexOf(','));
                    String result = line.substring(line.indexOf(',')+1);
                    listener.onResult(id, result); // streamed result
                }
            }

            process.waitFor();
            listener.onComplete();

        } catch (Exception e) {
            listener.onError(e);
            throw new RuntimeException("Python vision execution failed", e);
        }
    }
}
