// src/main/java/org/lewap/photogallery/service/PhotoService.java
package org.lewap.photogallery.service;

import jakarta.annotation.PostConstruct;
import org.lewap.photogallery.model.Photo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${thumbnail.dir}")
    private String thumbnailDir;

    @Value("${thumbnail.maxwidth}")
    private int thumbnailMaxWidth;

    @Value("${thumbnail.maxheight}")
    private int thumbnailMaxHeight;

    @PostConstruct
    public void init() {
        createUploadDirectory();
        loadPhotos();
    }

    private final List<Photo> photos = new ArrayList<>();

    public String getUploadDir() {
        return uploadDir;
    }

    public String getThumbnailDir() {
        return thumbnailDir;
    }

    private void createUploadDirectory() {
        File uploadFileDir = new File(uploadDir);
        if (!uploadFileDir.exists()) {
            uploadFileDir.mkdirs();
        }
    }

    private void loadPhotos() {
        File uploadFileDir = new File(uploadDir);
        if (uploadFileDir.exists() && uploadFileDir.isDirectory()) {
            File[] files = uploadFileDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        Photo photo = new Photo();
                        photo.setId(UUID.randomUUID().toString());
                        photo.setName(fileName);
                        photo.setPath(file.getAbsolutePath());
                        photo.setUploadTime(LocalDateTime.now());
                        photo.setSize(file.length());
                        photo.setThumbnailPath(Paths.get(thumbnailDir, fileName).toString());
                        photos.add(photo);
                    }
                }
            }
        }
    }

    public List<Photo> getAllPhotos() {
        return new ArrayList<>(photos);
    }

    public Photo uploadPhoto(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String rand = UUID.randomUUID().toString() + ".jpg";

        Path filePath = Paths.get(uploadDir, rand);
        Files.write(filePath, file.getBytes());
        File thumbnailFile = generateThumbnail(filePath);

        Photo photo = new Photo();
        photo.setId(UUID.randomUUID().toString());
        photo.setName(rand);
        photo.setPath(filePath.toString());
        photo.setUploadTime(LocalDateTime.now());
        photo.setSize(file.getSize());
        photo.setThumbnailPath(thumbnailFile.getPath());

        photos.add(photo);
        return photo;
    }

    private File generateThumbnail(Path originalPhotoPath) throws IOException {
        Path thumbnailPath = Paths.get(thumbnailDir, originalPhotoPath.getFileName().toString());
        Files.createDirectories(thumbnailPath.getParent());

        BufferedImage originalImage = ImageIO.read(originalPhotoPath.toFile());

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate the scaling factor to fit within max dimensions
        double scale = Math.min((double) thumbnailMaxWidth / originalWidth, (double) thumbnailMaxHeight / originalHeight);

        // Calculate new dimensions preserving aspect ratio
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        //BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, originalImage.getType());

        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        File thumbnailFile = thumbnailPath.toFile();
        ImageIO.write(thumbnail, "jpg", thumbnailFile);
        return thumbnailFile;
    }

    public void deletePhoto(String id) {
        //photos.removeIf(photo -> photo.getId().equals(id));
        // Delete file from disk
        Photo photoToDelete = photos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (photoToDelete != null) {
            photos.remove(photoToDelete);
            new File(photoToDelete.getPath()).delete();
            String thumbnailPath = photoToDelete.getThumbnailPath();
            if ( thumbnailPath != null ) {
                new File(thumbnailPath).delete();
            }
        }
    }
}