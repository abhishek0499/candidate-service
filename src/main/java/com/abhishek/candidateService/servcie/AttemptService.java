package com.abhishek.candidateService.servcie;

import com.abhishek.candidateService.client.AdminClient;
import com.abhishek.candidateService.dto.TestWithStatusDTO;
import com.abhishek.candidateService.model.Answer;
import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.model.Status;
import com.abhishek.candidateService.repository.AttemptRepository;
import com.abhishek.candidateService.client.ResultsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttemptService {
    private final AttemptRepository attemptRepository;
    private final AdminClient adminClient;
    private final ResultsClient resultsClient;

    /**
     * Optimized version: Fetches all attempts once instead of N+2 queries
     * Old: 1 query for tests + N queries for completed + N queries for in-progress
     * = O(2N+1)
     * New: 1 query for tests + 3 queries for attempts = O(4) constant time
     * Uses Set instead of Map for better memory efficiency and semantic clarity
     */
    public List<TestWithStatusDTO> getTestsWithStatus(String candidateId, String bearerToken) {
        log.info("Fetching tests with status for candidate: {}", candidateId);

        var tests = adminClient.getAssignedTests(candidateId, bearerToken);
        log.debug("Retrieved {} assigned tests", tests.size());

        // Optimization: Fetch all attempts for this candidate once
        List<Attempt> inProgressAttempts = attemptRepository.findByCandidateIdAndStatus(candidateId,
                Status.IN_PROGRESS);
        List<Attempt> submittedAttempts = attemptRepository.findByCandidateIdAndStatus(candidateId, Status.SUBMITTED);
        List<Attempt> timedOutAttempts = attemptRepository.findByCandidateIdAndStatus(candidateId, Status.TIMED_OUT);

        log.debug("Retrieved {} in-progress, {} submitted, and {} timed-out attempts for candidate: {}",
                inProgressAttempts.size(), submittedAttempts.size(), timedOutAttempts.size(), candidateId);

        // Build sets for O(1) lookup - more efficient than Map<String, Boolean>
        Set<String> inProgressTests = inProgressAttempts.stream()
                .map(Attempt::getTestId)
                .collect(Collectors.toSet());

        Set<String> completedTests = new HashSet<>();
        submittedAttempts.forEach(attempt -> completedTests.add(attempt.getTestId()));
        timedOutAttempts.forEach(attempt -> completedTests.add(attempt.getTestId()));

        List<TestWithStatusDTO> testsWithStatus = tests.stream().map(testObj -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> testData = (Map<String, Object>) testObj;

            TestWithStatusDTO testWithStatus = new TestWithStatusDTO();
            testWithStatus.setId((String) testData.get("id"));
            testWithStatus.setName((String) testData.get("name"));
            testWithStatus.setDescription((String) testData.get("description"));
            testWithStatus.setDurationMinutes((Integer) testData.get("durationMinutes"));
            testWithStatus.setActive((Boolean) testData.get("active"));
            testWithStatus.setScheduled((Boolean) testData.get("scheduled"));

            String testId = (String) testData.get("id");
            // O(1) lookup using Set.contains() - cleaner and more efficient than Map
            testWithStatus.setCompleted(completedTests.contains(testId));
            testWithStatus.setInProgress(inProgressTests.contains(testId));
            return testWithStatus;
        }).collect(Collectors.toList());

        log.info("Added status information to {} tests for candidate: {}", testsWithStatus.size(), candidateId);
        return testsWithStatus;
    }

    @Transactional
    public Attempt startAttempt(String candidateId, String testId, String bearerToken) {
        log.info("Starting attempt for candidate: {}, test: {}", candidateId, testId);

        var test = adminClient.fetchTest(testId, bearerToken);
        if (test == null) {
            log.error("Test not found: {}", testId);
            throw new NoSuchElementException("Test not found");
        }
        log.debug("Test fetched successfully with {} questions",
                test.questionIds != null ? test.questionIds.size() : 0);

        if (test.assignedCandidates == null || !test.assignedCandidates.contains(candidateId)) {
            log.warn("Candidate {} not assigned to test {}", candidateId, testId);
            throw new IllegalArgumentException("Candidate not assigned to this test");
        }

        if (!test.active) {
            log.warn("Test {} is not active", testId);
            throw new IllegalArgumentException("Test is not active");
        }

        var existingAttempts = attemptRepository.findByCandidateIdAndStatus(candidateId, Status.IN_PROGRESS);
        boolean sameTestInProgress = existingAttempts.stream()
                .anyMatch(attempt -> testId.equals(attempt.getTestId()));
        if (sameTestInProgress) {
            log.warn("Candidate {} already has an in-progress attempt for test {}", candidateId, testId);
            throw new IllegalArgumentException("An attempt is already in progress for this test");
        }

        if (hasCompletedTest(candidateId, testId)) {
            log.warn("Candidate {} has already completed test {}", candidateId, testId);
            throw new IllegalArgumentException("You have already completed this test");
        }

        var questions = adminClient.fetchQuestionsForTest(test, bearerToken);
        log.debug("Fetched {} questions for test {}", questions.size(), testId);

        Attempt newAttempt = new Attempt();
        newAttempt.setTestId(testId);
        newAttempt.setCandidateId(candidateId);
        newAttempt.setStartedAt(Instant.now());
        newAttempt.setTimeLimitMinutes(test.durationMinutes);
        newAttempt.setStatus(Status.IN_PROGRESS);
        newAttempt.setQuestions(questions);
        newAttempt.setAnswers(List.of());

        Attempt savedAttempt = attemptRepository.save(newAttempt);
        log.info("Attempt created successfully: {} for candidate: {}, test: {}",
                savedAttempt.getId(), candidateId, testId);
        return savedAttempt;
    }

    public boolean hasCompletedTest(String candidateId, String testId) {
        List<Attempt> attempts = attemptRepository.findByCandidateIdAndTestId(candidateId, testId);
        return attempts.stream()
                .anyMatch(attempt -> attempt.getStatus() == Status.SUBMITTED ||
                        attempt.getStatus() == Status.TIMED_OUT);
    }

    public boolean hasInProgressAttempt(String candidateId, String testId) {
        List<Attempt> attempts = attemptRepository.findByCandidateIdAndTestId(candidateId, testId);
        return attempts.stream()
                .anyMatch(attempt -> attempt.getStatus() == Status.IN_PROGRESS);
    }

    @Transactional
    public Attempt saveAnswer(String candidateId, String attemptId, String questionId, String optionId) {
        log.debug("Saving answer - Attempt: {}, Question: {}", attemptId, questionId);

        Attempt attempt = getInProgressAttempt(attemptId, candidateId);

        Answer newAnswer = new Answer();
        newAnswer.setQuestionId(questionId);
        newAnswer.setOptionId(optionId);
        newAnswer.setAnsweredAt(Instant.now());

        attempt.getAnswers().removeIf(existingAnswer -> existingAnswer.getQuestionId().equals(questionId));
        attempt.getAnswers().add(newAnswer);

        Attempt savedAttempt = attemptRepository.save(attempt);
        log.debug("Answer saved successfully");
        return savedAttempt;
    }

    @Transactional
    public Attempt submitAttempt(String candidateId, String attemptId, String bearerToken) {
        log.info("Submitting attempt: {}", attemptId);

        Attempt attempt = getInProgressAttempt(attemptId, candidateId);

        Instant currentTime = Instant.now();
        Instant attemptDeadline = attempt.getStartedAt().plusSeconds(attempt.getTimeLimitMinutes() * 60L);

        if (currentTime.isAfter(attemptDeadline)) {
            log.warn("Attempt timed out - Deadline: {}, Current: {}", attemptDeadline, currentTime);
            attempt.setStatus(Status.TIMED_OUT);
            attemptRepository.save(attempt);
            evaluateAndUpdateAttempt(attempt, bearerToken, "Timed out");
            return attempt;
        }

        attempt.setSubmittedAt(currentTime);
        attempt.setStatus(Status.SUBMITTED);
        attemptRepository.save(attempt);
        log.info("Attempt submitted successfully");

        evaluateAndUpdateAttempt(attempt, bearerToken, "Submitted");
        return attempt;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void sweepTimedOutAttempts() {
        log.debug("Running scheduled sweep for timed out attempts");
        List<Attempt> inProgressAttempts = attemptRepository.findByStatus(Status.IN_PROGRESS);
        Instant currentTime = Instant.now();
        int timedOutCount = 0;

        for (Attempt attempt : inProgressAttempts) {
            Instant attemptDeadline = attempt.getStartedAt()
                    .plusSeconds(attempt.getTimeLimitMinutes() * 60L);

            if (currentTime.isAfter(attemptDeadline)) {
                log.info("Auto-timing out attempt: {}", attempt.getId());
                attempt.setStatus(Status.TIMED_OUT);
                attemptRepository.save(attempt);

                try {
                    resultsClient.evaluateAttempt(attempt, null);
                    timedOutCount++;
                } catch (Exception evaluationException) {
                    log.error("Failed to evaluate auto-timed-out attempt: {}",
                            attempt.getId(), evaluationException);
                }
            }
        }

        if (timedOutCount > 0) {
            log.info("Swept {} timed out attempts", timedOutCount);
        }
    }

    private Attempt getInProgressAttempt(String attemptId, String candidateId) {
        Attempt attempt = attemptRepository.findByIdAndCandidateId(attemptId, candidateId)
                .orElseThrow(() -> {
                    log.error("Attempt not found - ID: {}, Candidate: {}", attemptId, candidateId);
                    return new NoSuchElementException("Attempt not found");
                });

        if (attempt.getStatus() != Status.IN_PROGRESS) {
            log.warn("Attempt not in progress - Status: {}", attempt.getStatus());
            throw new IllegalArgumentException("Attempt not in progress");
        }

        return attempt;
    }

    private void evaluateAndUpdateAttempt(Attempt attempt, String bearerToken, String attemptType) {
        try {
            var evaluationResult = resultsClient.evaluateAttempt(attempt, bearerToken);
            if (evaluationResult != null) {
                attempt.setScore(evaluationResult.getScore());
                attemptRepository.save(attempt);
                log.info("{} attempt evaluated - Score: {}", attemptType, evaluationResult.getScore());
            }
        } catch (Exception evaluationException) {
            log.error("Failed to evaluate {} attempt", attemptType.toLowerCase(), evaluationException);
        }
    }
}
