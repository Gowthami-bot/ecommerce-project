package com.ecommerce.repository;

import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public OrderResponse create(OrderRequest request) {
        if (request.getUserId() <= 0 || jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, request.getUserId()) == 0) {
            throw new IllegalArgumentException("Choose a valid customer account.");
        }

        Map<Integer, BigDecimal> prices = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            ProductInventory product = findAvailableProduct(item);
            prices.put(item.getProductId(), product.price());
            total = total.add(product.price().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal orderTotal = total;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO orders (user_id, total_amount, order_status, payment_status,
                                        fulfillment_status, shipping_address, contact_phone)
                    VALUES (?, ?, 'PLACED', 'SIMULATED_PAID', 'PENDING', ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, request.getUserId());
            statement.setBigDecimal(2, orderTotal);
            statement.setString(3, request.getShippingAddress().trim());
            statement.setString(4, request.getContactPhone().trim());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("The order could not be created.");
        }
        int orderId = generatedId.intValue();

        for (OrderItemRequest item : request.getItems()) {
            jdbcTemplate.update("""
                    INSERT INTO order_items (order_id, product_id, quantity, price)
                    VALUES (?, ?, ?, ?)
                    """, orderId, item.getProductId(), item.getQuantity(), prices.get(item.getProductId()));
            jdbcTemplate.update("UPDATE products SET stock = stock - ? WHERE id = ?",
                    item.getQuantity(), item.getProductId());
        }

        jdbcTemplate.update("DELETE FROM cart WHERE user_id = ?", request.getUserId());

        return new OrderResponse(orderId, total, "PLACED", "SIMULATED_PAID", "PENDING");
    }

    private ProductInventory findAvailableProduct(OrderItemRequest item) {
        if (item.getProductId() <= 0 || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Each order item needs a valid product and quantity.");
        }
        var products = jdbcTemplate.query("""
                SELECT price, stock FROM products WHERE id = ? FOR UPDATE
                """, (rs, rowNum) -> new ProductInventory(
                rs.getBigDecimal("price"), rs.getInt("stock")), item.getProductId());
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Product " + item.getProductId() + " is no longer available.");
        }
        ProductInventory product = products.get(0);
        if (product.stock() < item.getQuantity()) {
            throw new IllegalArgumentException("There is not enough stock for product " + item.getProductId() + ".");
        }
        return product;
    }

    private record ProductInventory(BigDecimal price, int stock) { }
}