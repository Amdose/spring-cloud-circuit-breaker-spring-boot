package com.amdose.spring.cloud.circuit.breaker.middleware.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amdose Team
 */
@RestController
class CloudController {

    private static final String BACKEND_SERVICE = "thirdPartyService";

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${third-party.service.url}")
    private String thirdPartyServiceUrl;

    @GetMapping("/api/cloud-data")
    @CircuitBreaker(name = BACKEND_SERVICE, fallbackMethod = "fallbackData")
//    @Retry(name = BACKEND_SERVICE)
    public Map<String, Object> getCloudData() {
        // Call third-party service
        String url = thirdPartyServiceUrl + "/api/data";
        ResponseEntity<Map> thirdPartyResponse = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> thirdPartyData = thirdPartyResponse.getBody();

        // Create and return response
        Map<String, Object> response = new HashMap<>();
        response.put("source", "Spring Cloud Component");
        response.put("thirdPartyData", thirdPartyData);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    // Fallback method for circuit breaker
    public Map<String, Object> fallbackData(Exception e) {
        Map<String, Object> fallbackThirdPartyData = new HashMap<>();
        fallbackThirdPartyData.put("message", "Fallback response from Cloud Service");
        fallbackThirdPartyData.put("reason", e.getMessage());
        fallbackThirdPartyData.put("status", "Circuit is OPEN");

        Map<String, Object> response = new HashMap<>();
        response.put("source", "Spring Cloud Component");
        response.put("thirdPartyData", fallbackThirdPartyData);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }
}