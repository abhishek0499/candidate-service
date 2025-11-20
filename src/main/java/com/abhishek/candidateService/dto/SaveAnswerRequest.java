package com.abhishek.candidateService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveAnswerRequest {
    @NotBlank
    private String questionId;
    @NotBlank
    private String optionId;
}