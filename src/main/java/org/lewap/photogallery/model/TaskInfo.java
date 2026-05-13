package org.lewap.photogallery.model;

public class TaskInfo {

    private TaskStatus status;
    private Integer progress;
    private String message;

    public TaskInfo(TaskStatus status, Integer progress, String message) {
        this.status = status;
        this.progress = progress;
        this.message = message;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}