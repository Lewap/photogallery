package org.lewap.photogallery.llm;

import java.io.BufferedReader;
import java.util.Map;

public interface LLMProvider {

    BufferedReader generate(String prompt,
                            Map<String, String> images,
                            GenerateOptions options);

}
