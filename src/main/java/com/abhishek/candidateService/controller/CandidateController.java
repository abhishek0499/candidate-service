package com.abhishek.candidateService.controller;

import com.abhishek.candidateService.dto.ApiResponse;
import com.abhishek.candidateService.dto.SaveAnswerRequest;
import com.abhishek.candidateService.dto.StartAttemptRequest;
import com.abhishek.candidateService.dto.SubmitAttemptRequest;
import com.abhishek.candidateService.dto.TestWithStatusDTO;
import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.servcie.AttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.abhishek.candidateService.constant.Constants.*;

@Slf4j
@RestController
@RequestMapping(ENDPOINT_CANDIDATE)
@RequiredArgsConstructor
public class CandidateController {
    private final AttemptService attemptService;

    @GetMapping(ENDPOINT_TESTS)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<List<TestWithStatusDTO>>> listAssignedTests(HttpServletRequest request) {
        String candidateId = (String) request.getAttribute("principalId");
        String bearerToken = extractBearer(request);

        log.info("GET {}{} - Candidate: {}", ENDPOINT_CANDIDATE, ENDPOINT_TESTS, candidateId);

        List<TestWithStatusDTO> tests = attemptService.getTestsWithStatus(candidateId, bearerToken);

        log.debug("Returning {} tests for candidate: {}", tests.size(), candidateId);
        return ResponseEntity.ok(ApiResponse.<List<TestWithStatusDTO>>builder()
                .message(MSG_TESTS_FETCHED)
                .data(tests)
                .build());
    }

    @PostMapping(ENDPOINT_TESTS + ENDPOINT_START)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Attempt>> startAttempt(@Valid @RequestBody StartAttemptRequest request,
            HttpServletRequest servletRequest) {
        String candidateId = (String) servletRequest.getAttribute("principalId");
        String bearerToken = extractBearer(servletRequest);

        log.info("POST {}{}{} - Candidate: {}, Test: {}",
                ENDPOINT_CANDIDATE, ENDPOINT_TESTS, ENDPOINT_START, candidateId, request.getTestId());

        Attempt attempt = attemptService.startAttempt(candidateId, request.getTestId(), bearerToken);

        log.info("Attempt started successfully - ID: {}, Candidate: {}", attempt.getId(), candidateId);
        return ResponseEntity.ok(ApiResponse.<Attempt>builder()
                .message(MSG_ATTEMPT_STARTED)
                .data(attempt)
                .build());
    }

    @PostMapping(ENDPOINT_ATTEMPTS + "/{attemptId}" + ENDPOINT_ANSWER)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Attempt>> saveAnswer(@PathVariable String attemptId,
            @Valid @RequestBody SaveAnswerRequest request,
            HttpServletRequest servletRequest) {
        String candidateId = (String) servletRequest.getAttribute("principalId");

        log.debug("POST {}{}/{}{} - Candidate: {}, Question: {}",
                ENDPOINT_CANDIDATE, ENDPOINT_ATTEMPTS, attemptId, ENDPOINT_ANSWER,
                candidateId, request.getQuestionId());

        Attempt attempt = attemptService.saveAnswer(candidateId, attemptId, request.getQuestionId(),
                request.getOptionId());

        return ResponseEntity.ok(ApiResponse.<Attempt>builder()
                .message(MSG_ANSWER_SAVED)
                .data(attempt)
                .build());
    }

    @PostMapping(ENDPOINT_ATTEMPTS + ENDPOINT_SUBMIT)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Attempt>> submitAttempt(@Valid @RequestBody SubmitAttemptRequest request,
            HttpServletRequest servletRequest) {
        String candidateId = (String) servletRequest.getAttribute("principalId");
        String bearerToken = extractBearer(servletRequest);

        log.info("POST {}{}{} - Candidate: {}, Attempt: {}",
                ENDPOINT_CANDIDATE, ENDPOINT_ATTEMPTS, ENDPOINT_SUBMIT, candidateId, request.getAttemptId());

        Attempt attempt = attemptService.submitAttempt(candidateId, request.getAttemptId(), bearerToken);

        log.info("Attempt submitted successfully - ID: {}, Candidate: {}, Score: {}",
                attempt.getId(), candidateId, attempt.getScore());
        return ResponseEntity.ok(ApiResponse.<Attempt>builder()
                .message(MSG_ATTEMPT_SUBMITTED)
                .data(attempt)
                .build());
    }

    private String extractBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
