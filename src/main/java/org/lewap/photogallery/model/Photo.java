package org.lewap.photogallery.model;

import java.time.LocalDateTime;

public class Photo {
    private String id;
    private String name;
    private String path;
    private LocalDateTime uploadTime;
    private long size;
    private String thumbnailPath;
    private String tags;

    public Photo() {}

    public Photo(String id, String name, String path, LocalDateTime uploadTime, long size, String thumbnailPath) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.uploadTime = uploadTime;
        this.size = size;
        this.thumbnailPath = thumbnailPath;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}