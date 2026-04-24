package org.lewap.photogallery.llm.impl;

import org.lewap.photogallery.llm.AvailableModels;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("python-vision-models")
public class PythonVisionAvailableModels implements AvailableModels {

    @Override
    public List<String> getAvailableModels () {
        return new ArrayList<>(List.of("predefined"));
    }

}