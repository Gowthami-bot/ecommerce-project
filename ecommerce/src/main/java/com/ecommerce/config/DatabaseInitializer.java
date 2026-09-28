package com.ecommerce.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.ecommerce.service.UserService;

@Component
public class DatabaseInitializer implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Override
    public void afterPropertiesSet() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    description TEXT,
                    category VARCHAR(50) NOT NULL,
                    color VARCHAR(30),
                    price DECIMAL(10,2) NOT NULL,
                    stock INT NOT NULL DEFAULT 0,
                    rating DECIMAL(2,1) DEFAULT 0.0,
                    image_url VARCHAR(500),
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    total_amount DECIMAL(10,2) NOT NULL,
                    order_status VARCHAR(30) NOT NULL DEFAULT 'PLACED',
                    shipping_address VARCHAR(500),
                    contact_phone VARCHAR(30),
                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY user_id (user_id),
                    CONSTRAINT orders_user_fk FOREIGN KEY (user_id)
                        REFERENCES users (id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    KEY order_id (order_id),
                    KEY product_id (product_id),
                    CONSTRAINT order_items_order_fk FOREIGN KEY (order_id)
                        REFERENCES orders (id) ON DELETE CASCADE,
                    CONSTRAINT order_items_product_fk FOREIGN KEY (product_id)
                        REFERENCES products (id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        ensureOrderColumn("shipping_address", "VARCHAR(500)");
        ensureOrderColumn("contact_phone", "VARCHAR(30)");
        ensureOrderColumn("payment_status", "VARCHAR(30) NOT NULL DEFAULT 'NOT_RECORDED'");
        ensureOrderColumn("fulfillment_status", "VARCHAR(30) NOT NULL DEFAULT 'PENDING'");
        seedProductsIfEmpty();
        userService.upgradeLegacyPasswords();
    }

    private void ensureOrderColumn(String column, String type) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = ?
                """, Integer.class, column);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN " + column + " " + type);
        }
    }

    private void seedProductsIfEmpty() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.batchUpdate("""
                    INSERT INTO products (name, description, category, color, price, stock, rating, image_url)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, java.util.List.of(
                    new Object[]{"Canvas Weekender", "A roomy everyday carryall in sturdy washed canvas.", "Bags", "Sage", 68.00, 24, 4.8, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=85"},
                    new Object[]{"Studio Headphones", "Balanced wireless sound with a soft, over-ear fit.", "Audio", "Black", 129.00, 18, 4.9, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=85"},
                    new Object[]{"Everyday Runner", "Lightweight movement shoes built for daily miles.", "Footwear", "Coral", 94.00, 32, 4.7, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=85"},
                    new Object[]{"Ceramic Pour-over Set", "A considered two-piece set for a slower morning ritual.", "Home", "Stone", 56.00, 15, 4.6, "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=85"},
                    new Object[]{"Field Watch", "A clean, durable timepiece with a brushed steel case.", "Accessories", "Steel", 156.00, 11, 4.8, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=85"},
                    new Object[]{"Soft-knit Throw", "A relaxed woven layer for the sofa or the foot of the bed.", "Home", "Ochre", 82.00, 20, 4.5, "https://images.unsplash.com/photo-1600369671236-e74521d4b6ad?auto=format&fit=crop&w=900&q=85"}
            ));
        }
    }
}