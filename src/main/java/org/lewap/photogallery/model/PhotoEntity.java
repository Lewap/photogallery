package org.lewap.photogallery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
public class PhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String path;
    private LocalDateTime uploadTime;
    private long size;
    private String thumbnailPath;

    // Constructors, getters, and setters
    public PhotoEntity() {}

    public PhotoEntity(String id, String name, String path, LocalDateTime uploadTime, long size, String thumbnailPath) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.uploadTime = uploadTime;
        this.size = size;
        this.thumbnailPath = thumbnailPath;
    }

    public Photo toPhoto () {

        return new Photo(
                this.id,
                this.name,
                this.path,
                this.uploadTime,
                this.size,
                this.thumbnailPath
        );
    }

    // Getters and setters...
}