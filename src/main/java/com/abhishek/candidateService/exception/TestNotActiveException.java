package com.abhishek.candidateService.exception;

/**
 * Custom exception for test not active errors
 */
public class TestNotActiveException extends RuntimeException {

    private final String testId;

    public TestNotActiveException(String testId) {
        super(String.format("Test is not currently active: %s", testId));
        this.testId = testId;
    }

    public String getTestId() {
        return testId;
    }
}
