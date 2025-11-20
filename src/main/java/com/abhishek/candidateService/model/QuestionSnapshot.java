package com.abhishek.candidateService.model;

import lombok.Data;

import java.util.List;

@Data
public class QuestionSnapshot {
    private String questionId;
    private List<Option> options; // randomized order
    private String text;
}