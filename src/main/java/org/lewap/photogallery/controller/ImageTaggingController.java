package org.lewap.photogallery.controller;

import org.lewap.photogallery.service.ImageTaggingService;
import org.springframework.http.ResponseEntity;
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
            @RequestParam String model,
            @RequestParam("ids") List<String> ids
            ) {

        service.tagImages(provider, model, ids);

        return "redirect:/";
    }

    @GetMapping("/available-models")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableModels(@RequestParam String provider) {
        List<String> models = service.getLLMModels(provider);
        return ResponseEntity.ok(models);
    }

}