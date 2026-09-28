package com.ecommerce.repository;

import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CartItemResponse> findByUserId(int userId) {
        return jdbcTemplate.query("""
                SELECT c.product_id, c.quantity, p.name, p.description, p.category, p.color,
                       p.price, p.stock, p.rating, p.image_url
                FROM cart c
                JOIN products p ON p.id = c.product_id
                WHERE c.user_id = ?
                ORDER BY c.id
                """, (rs, rowNum) -> new CartItemResponse(
                rs.getInt("product_id"), rs.getInt("quantity"),
                new ProductResponse(rs.getInt("product_id"), rs.getString("name"),
                        rs.getString("description"), rs.getString("category"),
                        rs.getString("color"), rs.getBigDecimal("price"),
                        rs.getInt("stock"), rs.getBigDecimal("rating"),
                        rs.getString("image_url"))), userId);
    }

    public void setQuantity(int userId, int productId, int quantity) {
        List<Integer> stocks = jdbcTemplate.query("SELECT stock FROM products WHERE id = ? FOR UPDATE",
                (rs, rowNum) -> rs.getInt("stock"), productId);
        if (stocks.isEmpty()) {
            throw new IllegalArgumentException("Product " + productId + " is no longer available.");
        }
        if (quantity < 1 || quantity > stocks.get(0)) {
            throw new IllegalArgumentException("Choose a quantity between 1 and the available stock.");
        }

        int updated = jdbcTemplate.update("""
                UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?
                """, quantity, userId, productId);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)
                    """, userId, productId, quantity);
        }
    }

    public void remove(int userId, int productId) {
        jdbcTemplate.update("DELETE FROM cart WHERE user_id = ? AND product_id = ?", userId, productId);
    }

    public void clear(int userId) {
        jdbcTemplate.update("DELETE FROM cart WHERE user_id = ?", userId);
    }
}