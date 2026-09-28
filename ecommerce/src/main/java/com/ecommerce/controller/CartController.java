package com.ecommerce.controller;

import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.service.CartService;
import com.ecommerce.web.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthenticatedUser authenticatedUser;

    @GetMapping
    public List<CartItemResponse> getCart(HttpSession session) {
        return cartService.getCart(authenticatedUser.requireId(session));
    }

    @PutMapping("/items/{productId}")
    public List<CartItemResponse> setQuantity(@PathVariable("productId") int productId,
                                              @RequestBody QuantityRequest request,
                                              HttpSession session) {
        return cartService.setQuantity(authenticatedUser.requireId(session), productId, request.getQuantity());
    }

    @DeleteMapping("/items/{productId}")
    public List<CartItemResponse> remove(@PathVariable("productId") int productId, HttpSession session) {
        return cartService.remove(authenticatedUser.requireId(session), productId);
    }

    public static class QuantityRequest {
        private int quantity;

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}