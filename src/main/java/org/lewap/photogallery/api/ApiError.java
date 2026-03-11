package org.lewap.photogallery.api;

import java.time.LocalDateTime;

public record ApiError(
        String message,
        int status,
        LocalDateTime timestamp
) {
    public ApiError(String message, int status, LocalDateTime timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }
}
