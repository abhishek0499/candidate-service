package com.abhishek.candidateService.controller;

import com.abhishek.candidateService.dto.SaveAnswerRequest;
import com.abhishek.candidateService.dto.StartAttemptRequest;
import com.abhishek.candidateService.dto.SubmitAttemptRequest;
import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.servcie.AttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateController {
    private final AttemptService attemptService;


    // List assigned tests (simple stub: delegate to Admin Service in production)
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
        Attempt a = attemptService.startAttempt(userId, req.getTestId());
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
        Attempt a = attemptService.submitAttempt(userId, req.getAttemptId());
        return ResponseEntity.ok(a);
    }
}