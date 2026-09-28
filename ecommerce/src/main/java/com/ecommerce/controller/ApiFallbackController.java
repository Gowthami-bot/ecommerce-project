package com.ecommerce.controller;

import com.ecommerce.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/**")
public class ApiFallbackController {

    @RequestMapping
    public ResponseEntity<ApiError> unknownApiRoute() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found", "The requested API route was not found."));
    }
}