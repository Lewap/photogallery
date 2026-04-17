package org.lewap.photogallery.llm;

import java.util.List;

public interface LLMProvider {

    String generate(String prompt,
                          List<String> imagePaths,
                          GenerateOptions options);

}
