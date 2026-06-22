package com.example.orders.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration data for a new customer account")
public record RegisterRequest(
        @Schema(description = "User email address", example = "user@example.com")
        @Email @NotBlank String email,
        @Schema(description = "Password, minimum 8 characters", example = "password123")
        @NotBlank @Size(min = 8) String password
) {}
