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

@Slf4j
@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateController {
    private final AttemptService attemptService;

    @GetMapping("/tests")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<List<TestWithStatusDTO>>> listAssignedTests(HttpServletRequest request) {
        String candidateId = (String) request.getAttribute("principalId");
        String bearerToken = extractBearer(request);

        log.info("GET /candidate/tests - Candidate: {}", candidateId);

        List<TestWithStatusDTO> tests = attemptService.getTestsWithStatus(candidateId, bearerToken);

        log.debug("Returning {} tests for candidate: {}", tests.size(), candidateId);
        return ResponseEntity.ok(ApiResponse.<List<TestWithStatusDTO>>builder()
                .message("Assigned tests fetched successfully")
                .data(tests)
                .build());
    }

    @PostMapping("/tests/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Attempt>> startAttempt(@Valid @RequestBody StartAttemptRequest req,
            HttpServletRequest request) {
        String candidateId = (String) request.getAttribute("principalId");
        String bearerToken = extractBearer(request);

        log.info("POST /candidate/tests/start - Candidate: {}, Test: {}", candidateId, req.getTestId());

        Attempt attempt = attemptService.startAttempt(candidateId, req.getTestId(), bearerToken);

        log.info("Attempt started successfully: {}", attempt.getId());
        return ResponseEntity.ok(ApiResponse.<Attempt>builder()
                .message("Attempt started successfully")
                .data(attempt)
                .build());
    }

    @PostMapping("/attempts/{attemptId}/answer")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Attempt>> saveAnswer(@PathVariable String attemptId,
            @Valid @RequestBody SaveAnswerRequest req,
            HttpServletRequest request) {
        String candidateId = (String) request.getAttribute("principalId");

        log.debug("POST /candidate/attempts/{}/answer - Candidate: {}, Question: {}",
                attemptId, candidateId, req.getQuestionId());

        Attempt attempt = attemptService.saveAnswer(candidateId, attemptId, req.getQuestionId(), req.getOptionId());

        return ResponseEntity.ok(ApiResponse.<Attempt>builder()
                .message("Answer saved successfully")
                .data(attempt)
                .build());
    }

    @PostMapping("/attempts/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Attempt>> submitAttempt(@Valid @RequestBody SubmitAttemptRequest req,
            HttpServletRequest request) {
        String candidateId = (String) request.getAttribute("principalId");
        String bearerToken = extractBearer(request);

        log.info("POST /candidate/attempts/submit - Candidate: {}, Attempt: {}", candidateId, req.getAttemptId());

        Attempt attempt = attemptService.submitAttempt(candidateId, req.getAttemptId(), bearerToken);

        log.info("Attempt submitted successfully: {}, Score: {}", attempt.getId(), attempt.getScore());
        return ResponseEntity.ok(ApiResponse.<Attempt>builder()
                .message("Attempt submitted successfully")
                .data(attempt)
                .build());
    }

    private String extractBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null)
            return null;
        if (header.startsWith("Bearer "))
            return header.substring(7);
        return header;
    }
}
