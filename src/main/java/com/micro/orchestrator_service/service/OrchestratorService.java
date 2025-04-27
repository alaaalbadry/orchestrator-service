package com.micro.orchestrator_service.service;

import com.micro.orchestrator_service.dto.OrderRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
@Service
public class OrchestratorService{
    private final RestTemplate restTemplate;

    public OrchestratorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> createOrder(OrderRequest orderRequest, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token); // forward token

            HttpEntity<OrderRequest> request = new HttpEntity<>(orderRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8083/orders/place", request, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException.Unauthorized ex) {
            // Handle token expiration
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token expired or unauthorized access");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Something went wrong while creating the order");
        }
    }

}
