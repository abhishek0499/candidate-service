package com.abhishek.candidateService.exception;

/**
 * Custom exception for unauthorized attempt access
 */
public class UnauthorizedAttemptAccessException extends RuntimeException {

    private final String attemptId;
    private final String candidateId;

    public UnauthorizedAttemptAccessException(String attemptId, String candidateId) {
        super(String.format("Candidate %s is not authorized to access attempt %s", candidateId, attemptId));
        this.attemptId = attemptId;
        this.candidateId = candidateId;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public String getCandidateId() {
        return candidateId;
    }
}
