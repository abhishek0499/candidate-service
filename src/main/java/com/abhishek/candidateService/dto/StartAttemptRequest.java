package com.abhishek.candidateService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartAttemptRequest {
    @NotBlank
    private String testId;
}