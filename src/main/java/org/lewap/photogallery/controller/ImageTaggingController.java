package org.lewap.photogallery.controller;

import org.lewap.photogallery.model.TaskInfo;
import org.lewap.photogallery.service.ImageTaggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//@RestController
@Controller
@RequestMapping("/api/tagging")
public class ImageTaggingController {

    private static final Logger log = LoggerFactory.getLogger(ImageTaggingController.class);

    private final ImageTaggingService service;

    public ImageTaggingController(ImageTaggingService service) {
        this.service = service;
    }

    @PostMapping("/tag-selected")
    public String tagImageBulk( Model modelUI,
            @RequestParam String provider,
            @RequestParam String model,
            @RequestParam("ids") List<String> ids
            ) {

        String taskId = UUID.randomUUID().toString();
        log.info("task ID = " + taskId);
        service.tagImages(provider, model, ids, taskId);
        modelUI.addAttribute("taskId", taskId);

        return "redirect:/?taskId=" + taskId;
    }

    @PostMapping("/complement-tags")
    public String complementTags( Model modelUI,
            @RequestParam String provider,
            @RequestParam String model
    ) {

        String taskId = UUID.randomUUID().toString();
        service.complementTags(provider, model, taskId);
        modelUI.addAttribute("taskId", taskId);

        return "redirect:/";
    }

    @GetMapping("/available-models")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableModels(@RequestParam String provider) {
        List<String> models = service.getLLMModels(provider);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/status/{taskId}")
    @ResponseBody
    public TaskInfo getStatus(@PathVariable String taskId) {
        return service.getTask(taskId);
    }

}