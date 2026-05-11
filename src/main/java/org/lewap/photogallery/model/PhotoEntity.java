package org.lewap.photogallery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
public class PhotoEntity {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String path;
    private LocalDateTime uploadTime;
    private long size;
    private String thumbnailPath;
    private String tags;
    private Boolean isMissing;

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

        Photo photo =  new Photo(
                this.id,
                this.name,
                this.path,
                this.uploadTime,
                this.size,
                this.thumbnailPath
        );
        photo.setTags(this.tags);
        return photo;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsMissing() {
        return isMissing;
    }

    public void setIsMissing(Boolean isMissing) {
        this.isMissing = isMissing;
    }

    public String getPath() {
        return path;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    @Override
    public String toString() {
        return "PhotoEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", uploadTime=" + uploadTime +
                ", size=" + size +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", tags='" + tags + '\'' +
                ", isMissing=" + isMissing +
                '}';
    }
}