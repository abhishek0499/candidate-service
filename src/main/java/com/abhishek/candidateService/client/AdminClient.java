package com.abhishek.candidateService.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminClient {

    private final RestTemplate restTemplate;
    private final String ADMIN_SERVICE_URL = "http://localhost:8082/admin";

    public List<?> getAssignedTests(String candidateId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                ADMIN_SERVICE_URL + "/tests/candidate/" + candidateId,
                HttpMethod.GET,
                entity,
                List.class);
        return response.getBody();
    }
}
