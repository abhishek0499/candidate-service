package com.abhishek.candidateService.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Answer {
    private String questionId;
    private String optionId;
    private Instant answeredAt;
}