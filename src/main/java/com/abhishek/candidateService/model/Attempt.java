package com.abhishek.candidateService.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "attempts")
@Data
public class Attempt {
    @Id
    private String id;
    private String testId;
    private String candidateId;
    private Instant startedAt;
    private Instant submittedAt;
    private int timeLimitMinutes;
    private Status status; // IN_PROGRESS, SUBMITTED, TIMED_OUT
    private List<QuestionSnapshot> questions; // snapshot of questions & options for this attempt
    private List<Answer> answers; // saved answers
    private Integer score; // optional after evaluation
}