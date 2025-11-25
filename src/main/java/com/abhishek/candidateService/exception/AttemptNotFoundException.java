package com.abhishek.candidateService.exception;

/**
 * Custom exception for attempt not found errors
 */
public class AttemptNotFoundException extends RuntimeException {

    private final String attemptId;

    public AttemptNotFoundException(String attemptId) {
        super(String.format("Attempt not found with ID: %s", attemptId));
        this.attemptId = attemptId;
    }

    public String getAttemptId() {
        return attemptId;
    }
}
