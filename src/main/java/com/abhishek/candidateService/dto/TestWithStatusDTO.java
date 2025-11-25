package com.abhishek.candidateService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestWithStatusDTO {
    private String id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean active;
    private Boolean scheduled;
    private List<String> categoryIds;
    private List<String> questionIds;
    private List<String> assignedCandidates;

    // Status fields
    private Boolean completed;
    private Boolean inProgress;
}
