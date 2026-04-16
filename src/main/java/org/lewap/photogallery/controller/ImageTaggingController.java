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

    @PostMapping("/single")
    public String tagImage(
            @RequestParam String provider,
            @RequestParam String description) {

        service.tagImageAsync(provider, description);

        System.out.println("Controller: Tagging started in background");
        return "redirect:/";
    }

    @PostMapping("/tag-selected")
    public String tagImageBulk(
            //@RequestParam String provider,
            //@RequestParam String description
            @RequestParam("ids") List<String> ids
            ) {
                for (String id : ids) {
                    System.out.println("input list el = " + id);
                }

        //service.tagImageAsync(provider, description);

        System.out.println("Controller: Tagging bulk");
        return "redirect:/";
    }

}