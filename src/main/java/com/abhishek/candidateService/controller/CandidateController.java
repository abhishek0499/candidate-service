package com.abhishek.candidateService.controller;

import com.abhishek.candidateService.dto.SaveAnswerRequest;
import com.abhishek.candidateService.dto.StartAttemptRequest;
import com.abhishek.candidateService.dto.SubmitAttemptRequest;
import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.servcie.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateController {
    private final AttemptService attemptService;

    @GetMapping("/tests")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> listAssignedTests(HttpServletRequest request) {
        String userId = (String) request.getAttribute("principalId");
        // TODO: call AdminClient to fetch tests assigned to this user
        return ResponseEntity.ok("[]");
    }

    @PostMapping("/tests/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Attempt> startAttempt(@Valid @RequestBody StartAttemptRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("principalId");
        String bearer = extractBearer(request);
        Attempt a = attemptService.startAttempt(userId, req.getTestId(), bearer);
        return ResponseEntity.ok(a);
    }

    @PostMapping("/attempts/{attemptId}/answer")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Attempt> saveAnswer(@PathVariable String attemptId, @Valid @RequestBody SaveAnswerRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("principalId");
        Attempt a = attemptService.saveAnswer(userId, attemptId, req.getQuestionId(), req.getOptionId());
        return ResponseEntity.ok(a);
    }

    @PostMapping("/attempts/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Attempt> submit(@Valid @RequestBody SubmitAttemptRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("principalId");
        String bearer = extractBearer(request);
        Attempt a = attemptService.submitAttempt(userId, req.getAttemptId(), bearer);
        return ResponseEntity.ok(a);
    }

    private String extractBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) return null;
        if (header.startsWith("Bearer ")) return header.substring(7);
        return header;
    }
}
