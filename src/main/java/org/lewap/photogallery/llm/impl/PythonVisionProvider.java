package org.lewap.photogallery.llm.impl;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.lewap.photogallery.util.FilePathParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service("python-vision")
public class PythonVisionProvider implements LLMProvider {

    @Value("${llm.python.script}")
    private String script;

    @Value("${llm.python.executable}")
    private String pythonExecutable;

    private Path scriptFromResource;

    public void setScriptFromResource() {
        try {

            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(script);

            if (inputStream == null) {
                throw new RuntimeException("Could not find resource: " + script);
            }

            // Create temporary file from the resource
            scriptFromResource = createTempScriptFromResource(inputStream);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set temp script path", e);
        }
    }

    private Path createTempScriptFromResource(InputStream inputStream) throws IOException {

        FilePathParser filePathParser = new FilePathParser(script);
        Path tempFile = Files.createTempFile(filePathParser.getFileNameWithoutExtension(), filePathParser.getFileExtension());
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit(); // Clean up on exit
        return tempFile;
    }

    @Override
    public BufferedReader generate(
            String prompt,
            Map<String, String> images,
            GenerateOptions options
    ) {
        try {

            setScriptFromResource();

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

            // Return a BufferedReader wrapping the process input stream
            return new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

        } catch (Exception e) {
            throw new RuntimeException("Python vision execution failed", e);
        }
    }
}
