// src/main/java/org/lewap/photogallery/controller/PhotoController.java
package org.lewap.photogallery.controller;

import org.lewap.photogallery.api.exception.BadRequestException;
import org.lewap.photogallery.api.exception.StorageException;
import org.lewap.photogallery.model.Photo;
import org.lewap.photogallery.service.ImageTaggingService;
import org.lewap.photogallery.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class PhotoController {
    @Autowired
    private PhotoService photoService;
    @Autowired
    private ImageTaggingService imageTaggingService;

    @GetMapping("/")
    public String gallery(Model model) {
        List<Photo> photos = photoService.getAllPhotos();
        model.addAttribute("photos", photos);
        model.addAttribute("uploadDir", photoService.getUploadDir());
        model.addAttribute("thumbnailDir", photoService.getThumbnailDir());
        return "gallery";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {

        try {
            Photo photo = photoService.uploadPhoto(file);
            if (photo != null) {
                redirectAttributes.addFlashAttribute("message", "Photo uploaded successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to upload photo!");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error uploading photo: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/api/upload")
    @ResponseBody
    public Photo uploadPhotoApi(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        try {
            Photo photo = photoService.uploadPhoto(file);
            if (photo == null) {
                throw new StorageException("Failed to store photo", null);
            }
            return photo;
        } catch (IOException e) {
            throw new StorageException("Error saving file", e);
        }
    }

    @PostMapping("/delete/{id}")
    public String deletePhoto(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            photoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("message", "Photo deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting photo: " + e.getMessage());
        }
        return "redirect:/";
    }
}