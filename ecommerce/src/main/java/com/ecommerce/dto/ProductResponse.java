package com.ecommerce.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final String color;
    private final BigDecimal price;
    private final int stock;
    private final BigDecimal rating;
    private final String imageUrl;

    public ProductResponse(int id, String name, String description, String category,
                           String color, BigDecimal price, int stock,
                           BigDecimal rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.color = color;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getColor() { return color; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public BigDecimal getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
}