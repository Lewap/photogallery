package org.lewap.photogallery.llm.impl;

import org.lewap.photogallery.llm.GenerateOptions;
import org.lewap.photogallery.llm.LLMProvider;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service("python-vision")
public class PythonVisionProvider implements LLMProvider {

    private final String pythonExecutable = "python"; // make configurable
    private final String scriptPath = "smolvlm_cli.py";

    @Override
    public String generate(
            String prompt,
            String imagePath,
            GenerateOptions options
    ) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath,
                    imagePath,
                    prompt
            );

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
