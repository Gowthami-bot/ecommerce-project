package com.ecommerce.dto;

public class CartItemResponse {

    private final int productId;
    private final int quantity;
    private final ProductResponse product;

    public CartItemResponse(int productId, int quantity, ProductResponse product) {
        this.productId = productId;
        this.quantity = quantity;
        this.product = product;
    }

    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public ProductResponse getProduct() { return product; }
}