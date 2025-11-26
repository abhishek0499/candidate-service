package com.abhishek.candidateService.constant;

/**
 * Centralized constants for the Candidate Service
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final String MSG_TESTS_FETCHED = "Assigned tests fetched successfully";
    public static final String MSG_ATTEMPT_STARTED = "Attempt started successfully";
    public static final String MSG_ANSWER_SAVED = "Answer saved successfully";
    public static final String MSG_ATTEMPT_SUBMITTED = "Attempt submitted successfully";

    public static final String ERROR_VALIDATION_FAILED = "Validation failed";

    public static final String ENDPOINT_CANDIDATE = "/candidate";
    public static final String ENDPOINT_TESTS = "/tests";
    public static final String ENDPOINT_START = "/start";
    public static final String ENDPOINT_ATTEMPTS = "/attempts";
    public static final String ENDPOINT_ANSWER = "/answer";
    public static final String ENDPOINT_SUBMIT = "/submit";
}
