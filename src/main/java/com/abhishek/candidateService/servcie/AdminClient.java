package com.abhishek.candidateService.servcie;

import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.model.QuestionSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminClient {
// In production implement with WebClient/RestTemplate or Feign to call Admin Service.


    public TestDTO fetchTest(String testId) {
// TODO: call Admin Service /admin/tests/{id}
        return null;
    }


    public List<QuestionSnapshot> fetchQuestionsForTest(TestDTO test) {
// TODO: call Admin Service to fetch questions by IDs or by categories and return randomized snapshots per attempt
        return List.of();
    }

    public static class TestDTO {
        private String id;
        private boolean active;
        private List<String> assignedCandidates;
        private List<String> questionIds;
        private List<String> categoryIds;
        private int durationMinutes;


        public boolean isActive() {
            return active;
        }

        public List<String> getAssignedCandidates() {
            return assignedCandidates;
        }

        public List<String> getQuestionIds() {
            return questionIds;
        }

        public List<String> getCategoryIds() {
            return categoryIds;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }
    }
}