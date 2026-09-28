package com.ecommerce.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    public int requireId(HttpSession session) {
        Object userId = session.getAttribute("authenticatedUserId");
        if (userId instanceof Integer id) {
            return id;
        }
        throw new UnauthorizedException("Sign in to continue.");
    }
}