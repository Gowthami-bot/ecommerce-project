package com.ecommerce.dto;

public class ApiMessage {

    private final String message;

    public ApiMessage(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
}