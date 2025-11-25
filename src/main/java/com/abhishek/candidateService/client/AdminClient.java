package com.abhishek.candidateService.client;

import com.abhishek.candidateService.dto.ApiResponse;
import com.abhishek.candidateService.dto.OptionDTO;
import com.abhishek.candidateService.dto.QuestionDTO;
import com.abhishek.candidateService.dto.TestDTO;
import com.abhishek.candidateService.model.Option;
import com.abhishek.candidateService.model.QuestionSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminClient {

    private final RestClient adminRestClient;
    private final Random random = new Random();

    /**
     * Fetch assigned tests for a candidate from Admin Service.
     * Returns empty list if not found or error occurs.
     */
    public List<?> getAssignedTests(String candidateId, String bearerToken) {
        log.debug("Fetching assigned tests for candidate: {}", candidateId);

        try {
            var request = adminRestClient.get()
                    .uri("/admin/tests/candidate/{candidateId}", candidateId);

            if (bearerToken != null) {
                request = request.headers(headers -> headers.setBearerAuth(bearerToken));
            }

            var response = request.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List>>() {
                    });

            if (response != null && response.getData() != null) {
                log.debug("Retrieved {} tests for candidate: {}", response.getData().size(), candidateId);
                return response.getData();
            }

            log.warn("No tests found for candidate: {}", candidateId);
            return List.of();
        } catch (RestClientException exception) {
            log.error("Failed to fetch assigned tests for candidate: {}", candidateId, exception);
            return List.of();
        }
    }

    /**
     * Fetch test by ID from Admin Service.
     * Returns null if not found.
     */
    public TestDTO fetchTest(String testId, String bearerToken) {
        log.debug("Fetching test: {}", testId);

        try {
            var request = adminRestClient.get().uri("/admin/tests/{testId}", testId);

            if (bearerToken != null) {
                request = request.headers(headers -> headers.setBearerAuth(bearerToken));
            }

            var response = request.retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<TestDTO>>() {
                    });

            if (response != null && response.getData() != null) {
                log.debug("Test fetched successfully: {}", testId);
                return response.getData();
            }

            log.warn("Test not found: {}", testId);
            return null;
        } catch (RestClientException exception) {
            log.error("Failed to fetch test: {}", testId, exception);
            return null;
        }
    }

    /**
     * Fetch questions for a test and return a randomized snapshot per question
     * suitable for Attempt.QuestionSnapshot.
     * Accepts bearer token to forward.
     */
    public List<QuestionSnapshot> fetchQuestionsForTest(TestDTO test, String bearerToken) {
        if (test == null) {
            log.warn("Cannot fetch questions for null test");
            return List.of();
        }

        log.debug("Fetching questions for test: {}", test.id);
        List<QuestionDTO> questions = new ArrayList<>();

        // Bulk fetch by IDs (preferred)
        if (test.questionIds != null && !test.questionIds.isEmpty()) {
            try {
                var request = adminRestClient.post().uri("/admin/questions/bulk");

                if (bearerToken != null) {
                    request = request.headers(headers -> headers.setBearerAuth(bearerToken));
                }

                var response = request.body(test.questionIds).retrieve()
                        .body(new ParameterizedTypeReference<ApiResponse<QuestionDTO[]>>() {
                        });

                if (response != null && response.getData() != null) {
                    questions.addAll(Arrays.asList(response.getData()));
                    log.debug("Fetched {} questions by IDs for test: {}",
                            response.getData().length, test.id);
                }
            } catch (Exception exception) {
                log.error("Failed to fetch questions by IDs for test: {}", test.id, exception);
                throw new RuntimeException("Failed to fetch questions by IDs", exception);
            }
        }
        // Else fetch by category
        else if (test.categoryIds != null && !test.categoryIds.isEmpty()) {
            log.debug("Fetching questions by {} categories for test: {}",
                    test.categoryIds.size(), test.id);

            for (String categoryId : test.categoryIds) {
                try {
                    var request = adminRestClient.get()
                            .uri(uriBuilder -> uriBuilder.path("/admin/questions")
                                    .queryParam("categoryId", categoryId)
                                    .build());

                    if (bearerToken != null) {
                        request = request.headers(headers -> headers.setBearerAuth(bearerToken));
                    }

                    var response = request.retrieve()
                            .body(new ParameterizedTypeReference<ApiResponse<QuestionDTO[]>>() {
                            });

                    if (response != null && response.getData() != null) {
                        questions.addAll(Arrays.asList(response.getData()));
                    }
                } catch (Exception exception) {
                    log.warn("Failed to fetch questions for category: {}, continuing...",
                            categoryId, exception);
                }
            }
            log.debug("Fetched total {} questions by categories for test: {}",
                    questions.size(), test.id);
        }

        // Map to snapshots and randomize options
        List<QuestionSnapshot> snapshots = questions.stream().map(question -> {
            QuestionSnapshot snapshot = new QuestionSnapshot();
            snapshot.setQuestionId(question.id);
            snapshot.setText(question.text);

            List<OptionDTO> optionDTOs = (question.options == null ? List.of() : question.options);
            List<Option> options = optionDTOs.stream().map(optionDTO -> {
                Option option = new Option();
                option.setId(optionDTO.id);
                option.setText(optionDTO.text);
                return option;
            }).collect(Collectors.toList());

            Collections.shuffle(options, random);
            snapshot.setOptions(options);
            return snapshot;
        }).collect(Collectors.toList());

        log.debug("Created {} question snapshots with randomized options for test: {}",
                snapshots.size(), test.id);
        return snapshots;
    }
}
