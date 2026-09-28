package com.ecommerce.repository;

import com.ecommerce.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> findAll() {

        String sql = """
                SELECT id, username, email, password, created_at
                FROM users
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            User user = new User();

            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setCreatedAt(rs.getTimestamp("created_at"));

            return user;
        });
    }
    public User findById(int id) {

    String sql = """
            SELECT id, username, email, password, created_at
            FROM users
            WHERE id = ?
            """;

    List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {

        User user = new User();

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCreatedAt(rs.getTimestamp("created_at"));

        return user;

    }, id);

    return users.isEmpty() ? null : users.get(0);
}

    public User findByEmail(String email) {
        List<User> users = jdbcTemplate.query("""
                SELECT id, username, email, password, created_at
                FROM users
                WHERE email = ?
                """, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
            return user;
        }, email);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean existsByUsernameOrEmail(String username, String email) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM users WHERE username = ? OR email = ?
                """, Integer.class, username, email);
        return count != null && count > 0;
    }

    public int updatePassword(int id, String passwordHash) {
        return jdbcTemplate.update("UPDATE users SET password = ? WHERE id = ?", passwordHash, id);
    }

    public List<User> findLegacyPasswordUsers() {
        return jdbcTemplate.query("""
                SELECT id, password
                FROM users
                WHERE password NOT LIKE 'pbkdf2-sha256$%'
                """, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setPassword(rs.getString("password"));
            return user;
        });
    }

    public int save(User user) {

    String sql = """
            INSERT INTO users (username, email, password)
            VALUES (?, ?, ?)
            """;

    return jdbcTemplate.update(
            sql,
            user.getUsername(),
            user.getEmail(),
            user.getPassword()
    );
}
}