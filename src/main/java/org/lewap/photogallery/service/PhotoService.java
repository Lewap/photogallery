// src/main/java/org/lewap/photogallery/service/PhotoService.java
package org.lewap.photogallery.service;

import jakarta.annotation.PostConstruct;
import org.lewap.photogallery.model.Photo;
import org.lewap.photogallery.model.PhotoEntity;
import org.lewap.photogallery.repository.PhotoRepository;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${thumbnail.dir}")
    private String thumbnailDir;

    @Value("${thumbnail.maxwidth}")
    private int thumbnailMaxWidth;

    @Value("${thumbnail.maxheight}")
    private int thumbnailMaxHeight;

    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    @PostConstruct
    public void init() {
        createUploadDirectory();
        syncWithFilesystem();
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

    public void syncWithFilesystem() {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath) || !Files.isDirectory(uploadPath)) {
            System.err.println("Upload directory does not exist: " + uploadDir);
            return;
        }

        try {
            // 1. Scan filesystem
            List<File> filesOnDisk = Files.list(uploadPath)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .toList();

            // 2. Load DB state
            List<PhotoEntity> dbPhotos = photoRepository.findAll();

            Map<String, PhotoEntity> dbByFilename = dbPhotos.stream()
                    .collect(Collectors.toMap(PhotoEntity::getName, p -> p));

            Set<String> diskFilenames = filesOnDisk.stream()
                    .map(File::getName)
                    .collect(Collectors.toSet());

            // 3. Add missing DB entries
            for (File file : filesOnDisk) {
                String fileName = file.getName();
                if (!dbByFilename.containsKey(fileName)) {

                    String id = UUID.randomUUID().toString();
                    LocalDateTime uploadTime = Instant.ofEpochMilli(file.lastModified())
                            .atZone(ZoneId.systemDefault())  // or UTC
                            .toLocalDateTime();

                    PhotoEntity entity = new PhotoEntity(
                            id,
                            fileName,
                            file.getPath(),
                            uploadTime,
                            file.length(),
                            Paths.get(thumbnailDir, fileName).toString()
                    );

                    photoRepository.save(entity);

                    log.info("Added missing DB entry for file: {}", fileName);
                } else {
                    PhotoEntity entity = dbByFilename.get(fileName);
                    if ( Boolean.TRUE.equals(entity.getIsMissing()) ) {
                        //the file has been found on disk but is flagged as missing
                        entity.setIsMissing(null);
                        photoRepository.save(entity);
                        log.info("isMissing flag has been cleared in the DB for the photo found on disk: {}", fileName);
                    }
                }
            }

            // 4. Detect DB entries missing on disk
            for (PhotoEntity dbPhoto : dbPhotos) {
                if (!diskFilenames.contains(dbPhoto.getName())) {
                    log.warn("File missing on disk for DB entry: {}", dbPhoto.getName());
                    dbPhoto.setIsMissing(true);
                    photoRepository.save(dbPhoto);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to sync filesystem with database", e);
        }
    }

    public List<Photo> getAllPhotos() {

        syncWithFilesystem();
        return photoRepository.findByIsMissingFalseOrIsMissingNull()
                .stream()
                .map(PhotoEntity::toPhoto)
                .toList();
    }

    public Photo uploadPhoto(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String rand = UUID.randomUUID().toString();
        String fileName = rand + ".jpg";

        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());
        File thumbnailFile = generateThumbnail(filePath);

        PhotoEntity photoEntity = new PhotoEntity(
                rand,
                fileName,
                filePath.toString(),
                LocalDateTime.now(),
                file.getSize(),
                thumbnailFile.getPath()
        );

        photoRepository.save(photoEntity);

        return photoEntity.toPhoto();
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

        PhotoEntity entity = photoRepository.findById(id).orElse(null);

        if (entity == null) {
            return;
        }

        boolean fileDeleted = true;

        File originalFile = new File(entity.getPath());
        if (originalFile.exists()) {
            fileDeleted = originalFile.delete();
        }

        String thumbnailPath = entity.getThumbnailPath();
        if (thumbnailPath != null) {
            File thumbnailFile = new File(thumbnailPath);
            if (thumbnailFile.exists()) {
                thumbnailFile.delete();
            }
        }

        if (fileDeleted) {
            photoRepository.deleteById(id);
        } else {
            // fallback: mark as missing
            entity.setIsMissing(true);
            photoRepository.save(entity);
        }
    }
}