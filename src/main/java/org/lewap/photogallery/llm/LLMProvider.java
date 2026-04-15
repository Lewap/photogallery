package org.lewap.photogallery.llm;

public interface LLMProvider {

    String generate(String prompt,
                          String imagePath,
                          GenerateOptions options);

}
