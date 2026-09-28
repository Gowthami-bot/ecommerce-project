package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.dto.ApiMessage;
import com.ecommerce.web.ResourceNotFoundException;
import com.ecommerce.web.DuplicateAccountException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("id") int id) {

        User user = userService.getUserById(id);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        return ResponseEntity.ok(
                new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreatedAt()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiMessage> createUser(@RequestBody User user) {

        if (user.getUsername() == null || user.getUsername().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().length() < 8) {
            throw new IllegalArgumentException("Username, email, and a password of at least 8 characters are required.");
        }

        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        if (userService.accountExists(user.getUsername(), user.getEmail())) {
            throw new DuplicateAccountException(
                "That username or email is already registered. Sign in or use different details.");
        }
        int rows = userService.createUser(user);

        if (rows == 1) {
            return ResponseEntity.status(201).body(new ApiMessage("User created successfully"));
        }

        throw new IllegalStateException("The user could not be created.");
    }
}