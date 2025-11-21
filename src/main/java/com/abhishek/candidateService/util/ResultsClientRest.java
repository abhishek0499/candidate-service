package com.abhishek.candidateService.util;

import com.abhishek.candidateService.model.Attempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ResultsClientRest {

    private final RestClient resultsRestClient;

    /**
     * Send attempt to Result Service for evaluation.
     * bearerToken forwarded if provided.
     */
    public void evaluateAttempt(Attempt attempt, String bearerToken) {
        try {
            var req = resultsRestClient.post().uri("/results/evaluate");
            if (bearerToken != null) req = req.headers(h -> h.setBearerAuth(bearerToken));
            req.body(attempt).retrieve().toBodilessEntity();
        } catch (Exception ex) {
            // POC: log and swallow; implement retry in production
            ex.printStackTrace();
        }
    }
}