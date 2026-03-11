package org.lewap.photogallery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lewap.photogallery.model.Photo;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhotoServiceTest {

    private PhotoService photoService;

    @TempDir
    Path tempUploadDir;

    @TempDir
    Path tempThumbnailDir;

    @BeforeEach
    void setUp() {
        photoService = new PhotoService();

        // Inject @Value fields manually
        ReflectionTestUtils.setField(photoService, "uploadDir", tempUploadDir.toString());
        ReflectionTestUtils.setField(photoService, "thumbnailDir", tempThumbnailDir.toString());
        ReflectionTestUtils.setField(photoService, "thumbnailWidth", 100);
        ReflectionTestUtils.setField(photoService, "thumbnailHeight", 100);

        // Manually trigger @PostConstruct
        photoService.init();
    }

    // ---------- INIT / LOAD TEST ----------

    @Test
    void init_shouldLoadExistingPhotosFromUploadDirectory() throws IOException {
        // Create a fake image file in upload dir BEFORE service loads
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        File existingFile = tempUploadDir.resolve("existing.jpg").toFile();
        ImageIO.write(image, "jpg", existingFile);

        // Re-run init to force reload
        photoService.init();

        List<Photo> photos = photoService.getAllPhotos();

        assertEquals(1, photos.size());
        assertEquals("existing.jpg", photos.getFirst().getName());
        assertTrue(new File(photos.getFirst().getPath()).exists());
    }

    // ---------- UPLOAD TESTS ----------

    @Test
    void uploadPhoto_shouldSaveFileAndCreateThumbnail() throws IOException {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Path tempImagePath = Files.createTempFile("test", ".jpg");
        ImageIO.write(image, "jpg", tempImagePath.toFile());

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                Files.readAllBytes(tempImagePath)
        );

        Photo photo = photoService.uploadPhoto(multipartFile);

        assertNotNull(photo);
        //assertEquals("photo.jpg", photo.getName());

        File savedFile = new File(photo.getPath());
        File thumbnailFile = new File(photo.getThumbnailPath());

        assertTrue(savedFile.exists(), "Uploaded file should exist");
        assertTrue(thumbnailFile.exists(), "Thumbnail should exist");
        assertEquals(100, ImageIO.read(thumbnailFile).getWidth());
        assertEquals(100, ImageIO.read(thumbnailFile).getHeight());
    }

    @Test
    void uploadPhoto_withEmptyFile_shouldReturnNull() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        Photo result = photoService.uploadPhoto(emptyFile);

        assertNull(result);
        assertTrue(photoService.getAllPhotos().isEmpty());
    }

    // ---------- GET ALL PHOTOS TEST ----------

    @Test
    void getAllPhotos_shouldReturnDefensiveCopy() throws IOException {
        MockMultipartFile file = createTestImage("copy.jpg");

        photoService.uploadPhoto(file);
        List<Photo> photos = photoService.getAllPhotos();

        assertEquals(1, photos.size());

        photos.clear(); // Modify returned list

        // Internal list must remain unchanged
        assertEquals(1, photoService.getAllPhotos().size());
    }

    // ---------- DELETE TEST ----------

    @Test
    void deletePhoto_shouldRemovePhotoAndDeleteFiles() throws IOException {
        MockMultipartFile file = createTestImage("delete.jpg");

        Photo photo = photoService.uploadPhoto(file);
        File original = new File(photo.getPath());
        File thumbnail = new File(photo.getThumbnailPath());

        assertTrue(original.exists());
        assertTrue(thumbnail.exists());

        photoService.deletePhoto(photo.getId());

        assertFalse(original.exists(), "Original file should be deleted");
        assertFalse(thumbnail.exists(), "Thumbnail should be deleted");
        assertTrue(photoService.getAllPhotos().isEmpty());
    }

    // ---------- HELPER ----------

    private MockMultipartFile createTestImage(String filename) throws IOException {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Path tempImagePath = Files.createTempFile("upload", ".jpg");
        ImageIO.write(image, "jpg", tempImagePath.toFile());

        return new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                Files.readAllBytes(tempImagePath)
        );
    }
}