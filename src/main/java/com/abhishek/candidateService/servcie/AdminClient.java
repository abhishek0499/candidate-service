/*
package com.abhishek.candidateService.servcie;

import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.model.Option;
import com.abhishek.candidateService.model.QuestionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminClient {

    private final WebClient adminWebClient;
    private final Random random = new Random();

    // DTO returned from Admin Service
    public static class TestDTO {
        public String id;
        public String name;
        public String description;
        public List<String> categoryIds;
        public List<String> questionIds;
        public int durationMinutes;
        public java.time.Instant startAt;
        public java.time.Instant endAt;
        public List<String> assignedCandidates;
        public boolean scheduled;
        public boolean active;

        public boolean isActive() { return active; }
        public List<String> getAssignedCandidates() { return assignedCandidates; }
        public List<String> getQuestionIds() { return questionIds; }
        public List<String> getCategoryIds() { return categoryIds; }
        public int getDurationMinutes() { return durationMinutes; }
    }

    public static class QuestionDTO {
        public String id;
        public String text;
        public List<OptionDTO> options;
        // correctOptionId intentionally omitted
    }

    public static class OptionDTO {
        public String id;
        public String text;
    }

    */
/**
     * Fetch test by id from Admin Service. Returns null if not found.
     *//*

    public TestDTO fetchTest(String testId) {
        try {
            return adminWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/admin/tests/{id}").build(testId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(TestDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound nf) {
            return null;
        }
    }

    */
/**
     * Fetch questions for a test and return a randomized snapshot per question suitable for Attempt.QuestionSnapshot
     * If questionIds are present in the test it fetches those exact questions; otherwise it fetches by category.
     *//*

    public List<QuestionSnapshot> fetchQuestionsForTest(TestDTO test) {
        if (test == null) return Collections.emptyList();

        List<QuestionDTO> questions = new ArrayList<>();

        if (test.questionIds != null && !test.questionIds.isEmpty()) {
            // bulk fetch by IDs - Admin Service should provide /admin/questions/bulk
            try {
                List<QuestionDTO> fetched = adminWebClient.post()
                        .uri("/admin/questions/bulk")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(test.questionIds)
                        .retrieve()
                        .bodyToFlux(QuestionDTO.class)
                        .collectList()
                        .block();
                if (fetched != null) questions.addAll(fetched);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch questions for test by ids", ex);
            }
        } else if (test.categoryIds != null && !test.categoryIds.isEmpty()) {
            // fetch questions per category
            for (String cat : test.categoryIds) {
                try {
                    List<QuestionDTO> byCat = adminWebClient.get()
                            .uri(uriBuilder -> uriBuilder.path("/admin/questions").queryParam("categoryId", cat).build())
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToFlux(QuestionDTO.class)
                            .collectList()
                            .block();
                    if (byCat != null) questions.addAll(byCat);
                } catch (Exception ex) {
                    // ignore category fetch failures for now (POC)
                }
            }
        }

        // produce snapshots (randomize option order)
        return questions.stream().map(q -> {
            QuestionSnapshot snap = new QuestionSnapshot();
            snap.setQuestionId(q.id);
            snap.setText(q.text);

            List<Option> opts = (q.options == null ? List.<OptionDTO>of() : q.options).stream()
                    .map(o -> {
                        Option op = new Option();
                        op.setId(o.id);
                        op.setText(o.text);
                        return op;
                    })
                    .collect(Collectors.toList());

            Collections.shuffle(opts, random);
            snap.setOptions(opts);
            return snap;
        }).collect(Collectors.toList());
    }
}
*/
