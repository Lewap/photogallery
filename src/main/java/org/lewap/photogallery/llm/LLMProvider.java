package org.lewap.photogallery.llm;

import java.util.Map;

public interface LLMProvider {

    void generate(String prompt,
                            String model,
                            Map<String, String> images,
                            GenerateOptions options,
                            ResultListener listener);

}
