package com.amdose.spring.cloud.circuit.breaker.third.party.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Amdose Team
 */
@RestController
class ThirdPartyController {

    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final int MAX_REQUESTS = 5;
    private final long RECOVERY_TIME_MS = 30000; // 30 seconds
    private volatile long recoveryEndTime = 0;

    @GetMapping("/api/data")
    public ResponseEntity<Object> getData() {
        // Check if in recovery period
        if (System.currentTimeMillis() < recoveryEndTime) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Service temporarily unavailable",
                            "message", "Service is in recovery mode",
                            "recoveryTimeRemaining", (recoveryEndTime - System.currentTimeMillis()) / 1000 + " seconds"
                    ));
        } else if (recoveryEndTime > 0 && System.currentTimeMillis() >= recoveryEndTime) {
            // Recovery period is over
            recoveryEndTime = 0;
            requestCounter.set(0);
        }

        // Count requests
        int currentCount = requestCounter.incrementAndGet();

        // Check if we should fail
        if (currentCount > MAX_REQUESTS) {
            // Start recovery period
            recoveryEndTime = System.currentTimeMillis() + RECOVERY_TIME_MS;

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Service temporarily unavailable",
                            "message", "Too many requests received, service is now in recovery mode",
                            "recoveryTime", RECOVERY_TIME_MS / 1000 + " seconds"
                    ));
        }

        // Normal response
        return ResponseEntity.ok(Map.of(
                "message", "Success from Third Party API",
                "data", "Sample data from the third-party service",
                "requestCount", currentCount,
                "remainingRequests", MAX_REQUESTS - currentCount
        ));
    }
}