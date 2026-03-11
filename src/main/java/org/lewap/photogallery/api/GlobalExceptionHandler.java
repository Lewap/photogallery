package org.lewap.photogallery.api;

import org.lewap.photogallery.api.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiError> handleStorage(StorageException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Fallback for anything we didn't explicitly handle
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return buildResponse("Unexpected server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildResponse(String message, HttpStatus status) {
        ApiError error = new ApiError(
                message,
                status.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, status);
    }
}