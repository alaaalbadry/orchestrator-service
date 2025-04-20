package com.micro.orchestrator_service.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
@Getter
@Setter
@Component
public class OrderRequest {
    private String productId;
    private int quantity;
    private String userId;
    private String paymentMethod;
    private double amount;
    private List<String> skuCodes;
    private String orderId;

    // Constructors
    public OrderRequest() {}

    public OrderRequest(String productId, int quantity, String userId, String paymentMethod, double amount) {
        this.productId = productId;
        this.quantity = quantity;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

}
