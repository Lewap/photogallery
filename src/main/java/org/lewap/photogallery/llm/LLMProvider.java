package org.lewap.photogallery.llm;

import java.util.List;
import java.util.Map;

public interface LLMProvider {

    void generateImageTags(String prompt,
                            String model,
                            Map<String, String> images,
                            GenerateOptions options,
                            ResultListener listener);

    List<String> generateSearchResponse(String searchPrompt,
                                        String model,
                                        Map<String, String> photoTags,
                                        GenerateOptions options);

}
