package com.ecommerce.service;

import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public CartService(CartRepository cartRepository,
                       org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.cartRepository = cartRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<CartItemResponse> getCart(int userId) {
        return cartRepository.findByUserId(userId);
    }

    public List<CartItemResponse> setQuantity(int userId, int productId, int quantity) {
        return transactionTemplate.execute(status -> {
            cartRepository.setQuantity(userId, productId, quantity);
            return cartRepository.findByUserId(userId);
        });
    }

    public List<CartItemResponse> remove(int userId, int productId) {
        return transactionTemplate.execute(status -> {
            cartRepository.remove(userId, productId);
            return cartRepository.findByUserId(userId);
        });
    }
}