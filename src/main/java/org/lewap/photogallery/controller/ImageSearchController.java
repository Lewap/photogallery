package org.lewap.photogallery.controller;


import org.lewap.photogallery.service.ImageSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/search")
public class ImageSearchController {

    private final ImageSearchService service;

    public ImageSearchController(ImageSearchService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public String getFilteredImages(@RequestParam String provider,
                                  @RequestParam String model,
                                  @RequestParam String searchPrompt) {
        service.filterImages(provider, model, searchPrompt);
        return "redirect:/";
    }

}