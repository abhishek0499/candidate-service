package com.abhishek.candidateService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitAttemptRequest {
    @NotBlank
    private String attemptId;
}