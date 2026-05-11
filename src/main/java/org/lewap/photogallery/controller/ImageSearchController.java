package org.lewap.photogallery.controller;


import org.lewap.photogallery.service.ImageSearchService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/clear")
    public String clearFilter() {
        service.clearFilter();
        return "redirect:/";
    }

    @GetMapping("/is-filter-active")
    @ResponseBody
    public ResponseEntity<String> isFilterActive() {
        String res = service.isFilterActive();
        return ResponseEntity.ok(res);
    }

}