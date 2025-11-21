package com.abhishek.candidateService.servcie;

import com.abhishek.candidateService.model.Answer;
import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.model.Status;
import com.abhishek.candidateService.repository.AttemptRepository;
import com.abhishek.candidateService.util.AdminClientRest;
import com.abhishek.candidateService.util.ResultsClientRest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AttemptService {
    private final AttemptRepository attemptRepository;
    private final AdminClientRest adminClient; // Rest client
    private final ResultsClientRest resultsClient; // Rest client

    // startAttempt now accepts bearer token forwarded from controller
    public Attempt startAttempt(String candidateId, String testId, String bearerToken) {
        var test = adminClient.fetchTest(testId, bearerToken);
        if (test == null) throw new NoSuchElementException("Test not found");

        if (test.assignedCandidates == null || !test.assignedCandidates.contains(candidateId)) {
            throw new IllegalArgumentException("Candidate not assigned to this test");
        }

        if (!test.active) throw new IllegalArgumentException("Test is not active");

        var existing = attemptRepository.findByCandidateIdAndStatus(candidateId, Status.IN_PROGRESS);
        boolean sameTestInProgress = existing.stream().anyMatch(a -> testId.equals(a.getTestId()));
        if (sameTestInProgress) throw new IllegalArgumentException("An attempt is already in progress for this test");

        var questions = adminClient.fetchQuestionsForTest(test, bearerToken);

        Attempt att = new Attempt();
        att.setTestId(testId);
        att.setCandidateId(candidateId);
        att.setStartedAt(Instant.now());
        att.setTimeLimitMinutes(test.durationMinutes);
        att.setStatus(Status.IN_PROGRESS);
        att.setQuestions(questions);
        att.setAnswers(List.of());
        return attemptRepository.save(att);
    }

    public Attempt saveAnswer(String candidateId, String attemptId, String questionId, String optionId) {
        Attempt att = attemptRepository.findByIdAndCandidateId(attemptId, candidateId).orElseThrow(() -> new NoSuchElementException("Attempt not found"));
        if (att.getStatus() != Status.IN_PROGRESS) throw new IllegalArgumentException("Attempt not in progress");

        var ans = new Answer(); ans.setQuestionId(questionId); ans.setOptionId(optionId); ans.setAnsweredAt(Instant.now());
        att.getAnswers().removeIf(a -> a.getQuestionId().equals(questionId));
        att.getAnswers().add(ans);
        return attemptRepository.save(att);
    }

    public Attempt submitAttempt(String candidateId, String attemptId, String bearerToken) {
        Attempt att = attemptRepository.findByIdAndCandidateId(attemptId, candidateId).orElseThrow(() -> new NoSuchElementException("Attempt not found"));
        if (att.getStatus() != Status.IN_PROGRESS) throw new IllegalArgumentException("Attempt not in progress");

        Instant now = Instant.now();
        Instant deadline = att.getStartedAt().plusSeconds(att.getTimeLimitMinutes() * 60L);
        if (now.isAfter(deadline)) {
            att.setStatus(Status.TIMED_OUT);
            attemptRepository.save(att);
            resultsClient.evaluateAttempt(att, bearerToken);
            return att;
        }

        att.setSubmittedAt(now);
        att.setStatus(Status.SUBMITTED);
        attemptRepository.save(att);

        resultsClient.evaluateAttempt(att, bearerToken);
        return att;
    }

    @Scheduled(fixedDelay = 30000)
    public void sweepTimedOutAttempts() {
        List<Attempt> inProgress = attemptRepository.findByStatus(Status.IN_PROGRESS);
        Instant now = Instant.now();
        for (Attempt a : inProgress) {
            Instant deadline = a.getStartedAt().plusSeconds(a.getTimeLimitMinutes() * 60L);
            if (now.isAfter(deadline)) {
                a.setStatus(Status.TIMED_OUT);
                attemptRepository.save(a);
                resultsClient.evaluateAttempt(a, null);
            }
        }
    }
}
