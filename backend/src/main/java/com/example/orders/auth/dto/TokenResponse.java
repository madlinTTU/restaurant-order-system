package com.example.orders.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token returned after successful authentication")
public record TokenResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType
) {}
