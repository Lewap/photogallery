package org.lewap.photogallery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class PhotoGalleryApplicationTests {

    private MockMvc mockMvc;

    @TempDir
    private static File tmpUploadDir;

    @TempDir
    private static File tmpThumbnailDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("upload.dir", () -> tmpUploadDir.getAbsolutePath());
        registry.add("thumbnail.dir", () -> tmpThumbnailDir.getAbsolutePath());
    }


    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void homePage_shouldLoad() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("gallery"));
    }

    @Test
    public void uploadPhoto_endpointResponding() throws Exception {
        String fileName = "test_upload.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                        .file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

    }

    @Test
    public void uploadPhotoApi_fileShouldGetCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_upload.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String photoJSON = uploadResult.getResponse().getContentAsString().trim();
        ObjectMapper mapper = new ObjectMapper();
        String photoPath = mapper.readTree(photoJSON).get("path").asString();

        File uploadedFile = new File(photoPath);
        assertTrue(uploadedFile.exists(), "Uploaded file should exist");

    }

    @Test
    public void deletePhotoApi_fileShouldBeUploadedAndThenDeleted() throws Exception {
        // First upload a photo
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_del.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String photoJSON = uploadResult.getResponse().getContentAsString().trim();
        ObjectMapper mapper = new ObjectMapper();
        String photoId = mapper.readTree(photoJSON).get("id").asString();
        String photoPath = mapper.readTree(photoJSON).get("path").asString();

        // Then delete it
        mockMvc.perform(post("/delete/" + photoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertFalse(new File(photoPath).exists(), "Uploaded file should NOT exist after deletion");

    }

}