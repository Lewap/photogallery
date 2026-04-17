package org.lewap.photogallery.controller;

import org.lewap.photogallery.service.ImageTaggingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        service.tagImagesAsync(provider, ids);

        System.out.println("Controller: Tagging bulk");
        return "redirect:/";
    }

}