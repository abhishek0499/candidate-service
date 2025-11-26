package com.abhishek.candidateService.dto;

import java.util.List;

public class TestDTO {
    public String id;
    public String name;
    public String description;
    public boolean active;
    public boolean scheduled;
    public List<String> assignedCandidates;
    public List<String> questionIds;
    public List<String> categoryIds;
    public int durationMinutes;
}