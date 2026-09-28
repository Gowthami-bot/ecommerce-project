package com.ecommerce.controller;

import com.ecommerce.dto.ApiMessage;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import com.ecommerce.web.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.ecommerce.web.AuthenticatedUser authenticatedUser;

    @GetMapping("/me")
    public UserResponse currentUser(HttpSession session) {
        int id = authenticatedUser.requireId(session);
        User user = userService.getUserById(id);
        if (user == null) {
            session.invalidate();
            throw new UnauthorizedException("Sign in to continue.");
        }
        return toResponse(user);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        if (request.getEmail() == null || request.getPassword() == null
                || request.getEmail().isBlank() || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        if (user == null) {
            throw new UnauthorizedException("Email or password is incorrect.");
        }
        HttpSession existingSession = servletRequest.getSession(false);
        if (existingSession != null) {
            servletRequest.changeSessionId();
        }
        HttpSession session = servletRequest.getSession(true);
        session.setMaxInactiveInterval(8 * 60 * 60);
        session.setAttribute("authenticatedUserId", user.getId());
        return toResponse(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiMessage> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiMessage("Signed out successfully."));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}