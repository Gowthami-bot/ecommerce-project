package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Product> findAll() {
        String sql = """
                SELECT id, name, description, category, color, price, stock, rating, image_url
                FROM products
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getInt("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setCategory(rs.getString("category"));
            product.setColor(rs.getString("color"));
            product.setPrice(rs.getBigDecimal("price"));
            product.setStock(rs.getInt("stock"));
            product.setRating(rs.getBigDecimal("rating"));
            product.setImageUrl(rs.getString("image_url"));
            return product;
        });
    }

    public Product findById(int id) {
        List<Product> products = jdbcTemplate.query("""
                SELECT id, name, description, category, color, price, stock, rating, image_url
                FROM products
                WHERE id = ?
                """, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getInt("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setCategory(rs.getString("category"));
            product.setColor(rs.getString("color"));
            product.setPrice(rs.getBigDecimal("price"));
            product.setStock(rs.getInt("stock"));
            product.setRating(rs.getBigDecimal("rating"));
            product.setImageUrl(rs.getString("image_url"));
            return product;
        }, id);
        return products.isEmpty() ? null : products.get(0);
    }
}