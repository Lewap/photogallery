package org.lewap.photogallery.controller;

import org.lewap.photogallery.service.ImageTaggingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
@Controller
@RequestMapping("/api/tagging")
public class ImageTaggingController {

    private final ImageTaggingService service;

    public ImageTaggingController(ImageTaggingService service) {
        this.service = service;
    }

    @PostMapping("/tag-selected")
    public String tagImageBulk(
            @RequestParam String provider,
            @RequestParam("ids") List<String> ids
            ) {

        service.tagImages(provider, ids);

        return "redirect:/";
    }

    @GetMapping("/available-models")
    public String tagImageBulk(
            @RequestParam String provider
    ) {

        service.getLLMModels(provider);
        return "redirect:/";
    }

}