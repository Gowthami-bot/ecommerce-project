package com.ecommerce.service;

import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.orderRepository = orderRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public OrderResponse placeOrder(OrderRequest request) {
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()
                || request.getContactPhone() == null || request.getContactPhone().isBlank()) {
            throw new IllegalArgumentException("Shipping address and phone are required.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }
        if (request.getItems().stream().map(OrderItemRequest::getProductId).distinct().count()
                != request.getItems().size()) {
            throw new IllegalArgumentException("A product can only appear once in an order.");
        }
        if (request.getShippingAddress().length() > 500 || request.getContactPhone().length() > 30) {
            throw new IllegalArgumentException("Shipping details are too long.");
        }
        return transactionTemplate.execute(status -> orderRepository.create(request));
    }
}