package com.abhishek.candidateService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${admin.service.base-url:http://localhost:8082}")
    private String adminBaseUrl;


    @Value("${results.service.base-url:http://localhost:8084}")
    private String resultsBaseUrl;

    @Bean(name = "adminRestClient")
    public RestClient adminRestClient(RestClient.Builder builder) {
        return builder.baseUrl(adminBaseUrl).build();
    }

    @Bean(name = "resultsRestClient")
    public RestClient resultsRestClient(RestClient.Builder builder) {
        return builder.baseUrl(resultsBaseUrl).build();
    }
}