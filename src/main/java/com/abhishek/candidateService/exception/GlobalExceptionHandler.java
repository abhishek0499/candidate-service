package com.abhishek.candidateService.exception;

import com.abhishek.candidateService.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static com.abhishek.candidateService.constant.Constants.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception) {

        log.debug("Validation exception details:", exception);

        Map<String, String> validationErrors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
            log.debug("Validation error - Field: {}, Message: {}", fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .message(ERROR_VALIDATION_FAILED)
                        .data(validationErrors)
                        .build());
    }

    /**
     * Handle AttemptNotFoundException
     */
    @ExceptionHandler(AttemptNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttemptNotFound(AttemptNotFoundException exception) {
        log.error("Attempt not found - ID: {}", exception.getAttemptId());
        log.debug("AttemptNotFoundException details:", exception);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .message(exception.getMessage())
                        .build());
    }

    /**
     * Handle TestNotActiveException
     */
    @ExceptionHandler(TestNotActiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleTestNotActive(TestNotActiveException exception) {
        log.warn("Test not active - ID: {}", exception.getTestId());
        log.debug("TestNotActiveException details:", exception);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .message(exception.getMessage())
                        .build());
    }

    /**
     * Handle AttemptAlreadySubmittedException
     */
    @ExceptionHandler(AttemptAlreadySubmittedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttemptAlreadySubmitted(AttemptAlreadySubmittedException exception) {
        log.warn("Attempt already submitted - ID: {}", exception.getAttemptId());
        log.debug("AttemptAlreadySubmittedException details:", exception);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .message(exception.getMessage())
                        .build());
    }

    /**
     * Handle UnauthorizedAttemptAccessException
     */
    @ExceptionHandler(UnauthorizedAttemptAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAttemptAccess(
            UnauthorizedAttemptAccessException exception) {
        log.warn("Unauthorized attempt access - Attempt: {}, Candidate: {}",
                exception.getAttemptId(), exception.getCandidateId());
        log.debug("UnauthorizedAttemptAccessException details:", exception);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .message(exception.getMessage())
                        .build());
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        log.error("Illegal argument: {}", exception.getMessage());
        log.debug("IllegalArgumentException stack trace:", exception);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .message(exception.getMessage())
                        .build());
    }

    /**
     * Handle all other uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception exception) {
        log.error("Unhandled exception occurred: {}", exception.getMessage());
        log.error("Exception stack trace:", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .message("An unexpected error occurred. Please try again later.")
                        .build());
    }
}