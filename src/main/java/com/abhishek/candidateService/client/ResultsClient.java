package com.abhishek.candidateService.client;

import com.abhishek.candidateService.dto.ResultDTO;
import com.abhishek.candidateService.model.Attempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ResultsClient {
    private final RestClient resultsRestClient;

    /**
     * Send attempt to Result Service for evaluation.
     */
    public ResultDTO evaluateAttempt(Attempt attempt, String bearerToken) {
        try {
            var req = resultsRestClient.post().uri("/results/evaluate");
            if (bearerToken != null)
                req = req.headers(h -> h.setBearerAuth(bearerToken));

            var response = req.body(attempt).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<com.abhishek.candidateService.dto.ApiResponse<com.abhishek.candidateService.dto.ResultDTO>>() {
                    });

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            return null;
        } catch (Exception ex) {
            // POC: log and swallow; implement retry in production
            ex.printStackTrace();
            return null;
        }
    }
}