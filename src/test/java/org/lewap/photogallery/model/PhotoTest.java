package org.lewap.photogallery.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        Photo photo = new Photo();

        assertNull(photo.getId());
        assertNull(photo.getName());
        assertNull(photo.getPath());
        assertNull(photo.getUploadTime());
        assertEquals(0, photo.getSize());
        assertNull(photo.getThumbnailPath());
    }

    @Test
    void allArgsConstructor_shouldSetFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Photo photo = new Photo(
                "1",
                "image.jpg",
                "/uploads/image.jpg",
                now,
                2048L
        );

        assertEquals("1", photo.getId());
        assertEquals("image.jpg", photo.getName());
        assertEquals("/uploads/image.jpg", photo.getPath());
        assertEquals(now, photo.getUploadTime());
        assertEquals(2048L, photo.getSize());

        // Not part of constructor
        assertNull(photo.getThumbnailPath());
    }

    @Test
    void settersAndGetters_shouldWorkForAllFields() {
        Photo photo = new Photo();
        LocalDateTime now = LocalDateTime.now();

        photo.setId("42");
        photo.setName("pic.png");
        photo.setPath("/data/pic.png");
        photo.setUploadTime(now);
        photo.setSize(999L);
        photo.setThumbnailPath("/thumbs/pic.png");

        assertAll(
                () -> assertEquals("42", photo.getId()),
                () -> assertEquals("pic.png", photo.getName()),
                () -> assertEquals("/data/pic.png", photo.getPath()),
                () -> assertEquals(now, photo.getUploadTime()),
                () -> assertEquals(999L, photo.getSize()),
                () -> assertEquals("/thumbs/pic.png", photo.getThumbnailPath())
        );
    }

    @Test
    void fields_shouldAllowBeingOverwritten() {
        Photo photo = new Photo();

        photo.setName("first.jpg");
        assertEquals("first.jpg", photo.getName());

        photo.setName("second.jpg");
        assertEquals("second.jpg", photo.getName());
    }

    @Test
    void size_shouldHandleZeroAndLargeValues() {
        Photo photo = new Photo();

        photo.setSize(0);
        assertEquals(0, photo.getSize());

        photo.setSize(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, photo.getSize());
    }
}