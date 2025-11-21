package com.abhishek.candidateService.util;

import com.abhishek.candidateService.dto.OptionDTO;
import com.abhishek.candidateService.dto.QuestionDTO;
import com.abhishek.candidateService.dto.TestDTO;
import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.model.Option;
import com.abhishek.candidateService.model.QuestionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminClientRest {


    private final RestClient adminRestClient;
    private final Random random = new Random();

/*    public TestDTO fetchTest(String testId) {
        try {
            return adminRestClient.get()
                    .uri("/admin/tests/{id}", testId)
                    .retrieve()
                    .body(TestDTO.class);
        } catch (Exception e) {
            return null;
        }
    }


    public List<QuestionSnapshot> fetchQuestionsForTest(TestDTO test) {
        if (test == null) return List.of();

        List<QuestionDTO> questions = new ArrayList<>();

        // 1. If explicit question IDs exist → fetch questions in bulk
        if (test.questionIds != null && !test.questionIds.isEmpty()) {
            try {
                QuestionDTO[] arr = adminRestClient.post()
                        .uri("/admin/questions/bulk")
                        .body(test.questionIds)
                        .retrieve()
                        .body(QuestionDTO[].class);

                if (arr != null) {
                    questions.addAll(Arrays.asList(arr));
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch questions by IDs", ex);
            }
        }
        // 2. Else fetch questions by categories
        else if (test.categoryIds != null && !test.categoryIds.isEmpty()) {

            for (String categoryId : test.categoryIds) {
                try {
                    QuestionDTO[] byCat = adminRestClient.get()
                            .uri(uri -> uri.path("/admin/questions")
                                    .queryParam("categoryId", categoryId)
                                    .build())
                            .retrieve()
                            .body(QuestionDTO[].class);

                    if (byCat != null) {
                        questions.addAll(Arrays.asList(byCat));
                    }

                } catch (Exception ignored) {
                    // ignore category fetch failure (POC behavior)
                }
            }
        }

        // 3. Map QuestionDTO → QuestionSnapshot with randomized options
        return questions.stream().map(q -> {
            QuestionSnapshot snap = new QuestionSnapshot();
            snap.setQuestionId(q.id);
            snap.setText(q.text);

            // null-safe options list
            List<OptionDTO> optList =
                    (q.options == null ? List.of() : q.options);

            List<Option> opts = optList.stream()
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
    }*/

    /**
     * Fetch test from Admin Service. If not found returns null.
     * bearerToken may be null.
     */
    public TestDTO fetchTest(String testId, String bearerToken) {
        try {
            var req = adminRestClient.get().uri("/admin/tests/{id}", testId);
            if (bearerToken != null) req = req.headers(h -> h.setBearerAuth(bearerToken));
            return req.retrieve().body(TestDTO.class);
        } catch (RestClientException ex) {
            return null;
        }
    }

    /**
     * Fetch questions for a test and return a randomized snapshot per question suitable for Attempt.QuestionSnapshot.
     * Accepts bearer token to forward.
     */
    public List<QuestionSnapshot> fetchQuestionsForTest(TestDTO test, String bearerToken) {
        if (test == null) return List.of();
        List<QuestionDTO> questions = new ArrayList<>();

        // Bulk fetch by IDs (preferred)
        if (test.questionIds != null && !test.questionIds.isEmpty()) {
            try {
                var req = adminRestClient.post().uri("/admin/questions/bulk");
                if (bearerToken != null) req = req.headers(h -> h.setBearerAuth(bearerToken));
                QuestionDTO[] arr = req.body(test.questionIds).retrieve().body(QuestionDTO[].class);
                if (arr != null) questions.addAll(Arrays.asList(arr));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch questions by IDs", ex);
            }
        }
        // Else fetch by category
        else if (test.categoryIds != null && !test.categoryIds.isEmpty()) {
            for (String categoryId : test.categoryIds) {
                try {
                    var req = adminRestClient.get().uri(uri -> uri.path("/admin/questions").queryParam("categoryId", categoryId).build());
                    if (bearerToken != null) req = req.headers(h -> h.setBearerAuth(bearerToken));
                    QuestionDTO[] byCat = req.retrieve().body(QuestionDTO[].class);
                    if (byCat != null) questions.addAll(Arrays.asList(byCat));
                } catch (Exception ignored) {
                    // ignore category fetch failures for now (POC)
                }
            }
        }

        // Map to snapshots and randomize options
        return questions.stream().map(q -> {
            QuestionSnapshot snap = new QuestionSnapshot();
            snap.setQuestionId(q.id);
            snap.setText(q.text);

            List<OptionDTO> optList = (q.options == null ? List.of() : q.options);
            List<Option> opts = optList.stream().map(o -> {
                Option op = new Option();
                op.setId(o.id);
                op.setText(o.text);
                return op;
            }).collect(Collectors.toList());

            Collections.shuffle(opts, random);
            snap.setOptions(opts);
            return snap;
        }).collect(Collectors.toList());
    }
}
