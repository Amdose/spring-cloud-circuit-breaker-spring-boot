# Resilience Patterns Demo

This project demonstrates resilience patterns in a distributed system with two components:

1. **Third Party Component** (port 8081) - Simulates an external API that fails after 5 requests
2. **Spring Cloud Component** (port 8082) - Service with Circuit Breaker pattern that communicates with the Third Party Component

## System Architecture

```
┌─────────────┐     ┌─────────────────┐     ┌────────────────┐
│   Postman   │ --> │  Spring Cloud   │ --> │  Third Party   │
│             │     │  Component      │     │  Component     │
│             │     │  (Port 8082)    │     │  (Port 8081)   │
└─────────────┘     └─────────────────┘     └────────────────┘
```

## Resilience Patterns Implemented

1. **Failure Simulation** - Third Party Component fails after 5 requests and recovers after 30 seconds
2. **Circuit Breaker** - Spring Cloud Component uses Resilience4j to implement circuit breaker pattern

## Expected Behavior

In our demonstration, the first 5 API calls execute successfully, with the cloud service seamlessly passing through third-party responses. On the 6th request, the third-party service fails but our middleware still makes the call and returns the error from downstream. The true resilience shines around the 9th request, when our failure rate crosses the 50% threshold - at this point, Resilience4j's circuit breaker trips open and stops calling the third-party service entirely. Instead, requests are immediately routed to our fallback method, providing a graceful degraded experience until the system recovers

## Circuit Breaker Configuration

Our circuit breaker configuration includes:

• **registerHealthIndicator=true**: Adds circuit state to Spring's health endpoint.
• **slidingWindowSize=10**: Uses last 10 calls to calculate failure rates.
• **minimumNumberOfCalls=5**: Requires 5+ calls before circuit can trip.
• **permittedNumberOfCallsInHalfOpenState=3**: Allows 3 test calls when half-open.
• **automaticTransitionFromOpenToHalfOpenEnabled=true**: Auto-shifts to half-open after wait duration.
• **waitDurationInOpenState=10s**: Stays open for 10 seconds before testing recovery.
• **failureRateThreshold=50**: Trips when failure rate exceeds 50%.
• **eventConsumerBufferSize=10**: Stores 10 circuit events for monitoring.

## Monitoring

To monitor your circuit breaker state, add these properties to your application.properties file:

```properties
management.endpoints.web.exposure.include=health,circuitbreakers
management.health.circuitbreakers.enabled=true
management.endpoint.health.show-details=always
```

You can access:
- http://localhost:8082/actuator/health for overall health status
- http://localhost:8082/actuator/circuitbreakers for detailed metrics
- http://localhost:8082/actuator/health/circuitbreakers for circuit-specific health details
![image](https://github.com/user-attachments/assets/81cf0d7a-9163-4542-89e7-80c5b9101e6d)


## Testing with Postman
Create a new Postman collection with the following request:

1. **Cloud Data Request**
    - Method: GET
    - URL: http://localhost:8082/api/cloud-data
    - Description: Get data through the Cloud Component

### Testing Procedure

1. Execute the "Cloud Data Request" 10+ times and observe:
    - First 5 requests: Success
    - 6th-8th requests: Errors returned from third-party
    - 9th+ requests: Circuit breaker trips, fallback responses provided
2. Wait 10 seconds and try again - the circuit enters half-open state
3. After 30 seconds, the Third Party Component should recover

As show below
![image](https://github.com/user-attachments/assets/1e23bfe9-4533-4f03-849f-310e4d4e2325)
![image](https://github.com/user-attachments/assets/10703fd9-812a-4284-a54c-1f5547dce2e8)
![image](https://github.com/user-attachments/assets/c2553040-a52d-42fd-875c-7addba23481a)
