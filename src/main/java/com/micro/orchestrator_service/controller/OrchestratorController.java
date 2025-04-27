package com.micro.orchestrator_service.controller;

import com.micro.orchestrator_service.dto.OrderRequest;
import com.micro.orchestrator_service.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orchestrator")
public class OrchestratorController {

    private static final String ADMIN_TOKEN ="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzQ1NzgyOTUxLCJleHAiOjE3NDU3ODY1NTF9.y8arPUHLqbnGaeCNycOc5i4JNMWmHFarpqmvSfeMMHg" ;
    private final RestTemplate restTemplate;
    @Autowired
    private OrchestratorService orchestratorService;

    public OrchestratorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzQ1MTQ3NTk2LCJleHAiOjE3NDUxNTExOTZ9.NFJAWfgwjpvxlT13C-1kCrwJ6AGC-0qYkibhq3DMv4w"); // <- Make sure this token has correct role
      //  HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);
        // 1. Call Inventory Service
        List<String> codes = new ArrayList<>();
        codes.add("code123");
        codes.add("code456");
        request.setSkuCodes(codes);
        String inventoryUrl = "http://localhost:8085/inventory/check";
        ResponseEntity<Boolean> inventoryResponse = restTemplate.postForEntity(inventoryUrl, request, Boolean.class);

        if (Boolean.TRUE.equals(inventoryResponse.getBody())) {
            // 2. Call Payment Service
            request.setOrderId("orderId123");
            String paymentUrl = "http://localhost:8082/payments/process";
            ResponseEntity<String> paymentResponse = restTemplate.postForEntity(paymentUrl, request, String.class);

            if (paymentResponse.getStatusCode().is2xxSuccessful()) {
                // 3. Call Order Service
                ResponseEntity<String> orderResponse = orchestratorService.createOrder(request, ADMIN_TOKEN);
                if (orderResponse.getStatusCode().is2xxSuccessful()) {
                    return  ResponseEntity.ok(orderResponse.getBody());
                }else{
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Expired!!");
                }
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Payment failed");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item not available");
    }
}
