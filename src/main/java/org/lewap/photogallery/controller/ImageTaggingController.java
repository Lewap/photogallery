package org.lewap.photogallery.controller;

import org.lewap.photogallery.service.ImageTaggingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//@RestController
@Controller
@RequestMapping("/api/tagging")
public class ImageTaggingController {

    private final ImageTaggingService service;

    public ImageTaggingController(ImageTaggingService service) {
        this.service = service;
    }

    @PostMapping
    public String tagImage(
            @RequestParam String provider,
            @RequestParam String description) {

        service.tagImageAsync(provider, description);

        return "Tagging started in background";
    }
}