package com.abhishek.candidateService.dto;

import lombok.Data;

@Data
public class ResultDTO {
    private String id;
    private String attemptId;
    private String candidateId;
    private String testId;
    private int score;
    private int correctCount;
    private int totalQuestions;
}
