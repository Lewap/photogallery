package org.lewap.photogallery.llm.impl;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service("python-vision")
public class PythonVisionProvider implements LLMProvider {

    private final String scriptName = "smolvlm_cli";
    private final String scriptSuffix = ".py";
    private Path scriptFromResource;

    public void setScriptFromResource() {
        try {
            // Method 1: Load as InputStream (Recommended)
            String scriptPath = "python/" + scriptName + scriptSuffix;
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(scriptPath);

            if (inputStream == null) {
                throw new RuntimeException("Could not find resource: " + scriptPath);
            }

            // Create temporary file from the resource
            scriptFromResource = createTempScriptFromResource(inputStream);

            // Execute Python script
            //executePythonScript(tempScript);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set temp script path", e);
        }
    }

    private Path createTempScriptFromResource(InputStream inputStream) throws IOException {
        Path tempFile = Files.createTempFile(scriptName, scriptSuffix);
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit(); // Clean up on exit
        return tempFile;
    }

    @Override
    public String generate(
            String prompt,
            List<String> imagePaths,
            GenerateOptions options
    ) {
        try {

            setScriptFromResource();

            // make configurable
            String pythonExecutable = "python";

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(pythonExecutable, scriptFromResource.toString());
            // Add image paths as separate arguments
            for (String imagePath : imagePaths) {
                pb.command().add(imagePath);
            }

            // Add prompt as last argument
            pb.command().add(prompt);

            // Merge stderr into stdout for easier debugging
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Python script failed: " + output);
            }

            return output.toString().trim();

        } catch (Exception e) {
            throw new RuntimeException("Python vision execution failed", e);
        }
    }
}
