package com.micro.orchestrator_service.controller;

import com.micro.orchestrator_service.dto.OrderRequest;
import com.micro.orchestrator_service.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orchestrator")
public class OrchestratorController {

    private static final String ADMIN_TOKEN ="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzQ3NjYyMDUxLCJleHAiOjE3NDc2NjU2NTF9.33WFEucHmuBQm_lyqSagvISL1r_WpbBBd3SkZjgX2Xs";
    private static final String Auth_TOKEN ="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ2YkRUYmR5c1FObHB2ZlZ5WEFFU3lKTkd1WEFXWk12ZnlmblRBVVVaODFBIn0.eyJleHAiOjE3NDc2NjMxMDUsImlhdCI6MTc0NzY2MjgwNSwianRpIjoiNTEwODZkYzMtNmRhYi00MTliLTgyMWYtNmUyNjBiNjljNjg4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9taWNyb3NlcnZpY2VzIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjBlOTAyNTUwLTlhNjAtNGJmMy1hMzZlLTA4Y2FkYTIyYTZkYiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImludmVudG9yeS1zZXJ2aWNlIiwic2Vzc2lvbl9zdGF0ZSI6ImRlNGQ1YjI2LTcxYmMtNGU3MC04ZTlmLWFhYjU3MDU4YmFjYiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDg1Il0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW1pY3Jvc2VydmljZXMiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiZGU0ZDViMjYtNzFiYy00ZTcwLThlOWYtYWFiNTcwNThiYWNiIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiYWxhYSBhbGJhZHJ5IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWxhYSIsImdpdmVuX25hbWUiOiJhbGFhIiwiZmFtaWx5X25hbWUiOiJhbGJhZHJ5IiwiZW1haWwiOiJhbGJhZHJ5YWxhYWFAZ21haWwuY29tIn0.fJOfY_GKyxwwBIiiStXbYD6vbO0zSSQBnsAEey1LhCANYNwGqS9gg8JUpcLMa8DQpVAFsa9csYVXh8mOKAJ3iBcRcoSAEBlj3X1eyvXQATc-0RwhAQg4xnBSzD81HPXrFnDLYlBSy7uoHCIPmRi7-ATA8kP_TcZ5BqI4iDz1hy34oMunnOfb0DNrUZnK4QEt93ltbKUHAUxs682a8GxAr5g5iDtVUIslQERSf23kcP237x77Us9XnQ7RfusW9h-5vX2B7THxwljFIcGQscpB3MexGIJY1fH-snQvhscqNQAfElGdmfrHDe5JS1aijJk1CYlzZHfbvmfehvWtRRQFQg";
    private final RestTemplate restTemplate;
    @Autowired
    private OrchestratorService orchestratorService;

    public OrchestratorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
        // 1. Call Inventory Service
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Auth_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<String> codes = new ArrayList<>();
        codes.add("code123");
        codes.add("code456");
        request.setSkuCodes(codes);
        String inventoryUrl = "http://INVENTORY-SERVICE/inventory/check";
        HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);
        headers.setBearerAuth(Auth_TOKEN);
        ResponseEntity<Boolean> inventoryResponse = restTemplate.exchange(
                inventoryUrl,
                HttpMethod.POST,
                entity,
                Boolean.class
        );
        if (Boolean.TRUE.equals(inventoryResponse.getBody())) {
            // 2. Call Payment Service
            request.setOrderId("orderId123");
            String paymentUrl = "http://DEMO-PAYMENT/payments/process";
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

    @GetMapping("/load-balanced")
    public ResponseEntity<String> getPort(){
        // 1. Call Inventory Service
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Auth_TOKEN);
        String inventoryUrl = "http://INVENTORY-SERVICE/inventory/port";
        HttpEntity<OrderRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<String> inventoryResponse = restTemplate.exchange(
                inventoryUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
        return ResponseEntity.ok(inventoryResponse.getBody());
    }
}
