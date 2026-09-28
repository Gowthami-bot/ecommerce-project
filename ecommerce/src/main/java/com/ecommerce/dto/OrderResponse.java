package com.ecommerce.dto;

import java.math.BigDecimal;

public class OrderResponse {

    private final int id;
    private final BigDecimal totalAmount;
    private final String status;
    private final String paymentStatus;
    private final String fulfillmentStatus;

    public OrderResponse(int id, BigDecimal totalAmount, String status,
                         String paymentStatus, String fulfillmentStatus) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public int getId() { return id; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getFulfillmentStatus() { return fulfillmentStatus; }
}