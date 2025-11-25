package com.abhishek.candidateService.exception;

/**
 * Custom exception for attempt already submitted errors
 */
public class AttemptAlreadySubmittedException extends RuntimeException {

    private final String attemptId;

    public AttemptAlreadySubmittedException(String attemptId) {
        super(String.format("Attempt has already been submitted: %s", attemptId));
        this.attemptId = attemptId;
    }

    public String getAttemptId() {
        return attemptId;
    }
}
