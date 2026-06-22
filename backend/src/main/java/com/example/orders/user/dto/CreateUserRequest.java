package com.example.orders.user.dto;

import com.example.orders.auth.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new user with a specified role")
public record CreateUserRequest(
        @Schema(description = "User email address", example = "staff@example.com")
        @NotBlank @Email String email,
        @Schema(description = "Password, minimum 6 characters", example = "password123")
        @NotBlank @Size(min = 6) String password,
        @Schema(description = "User role", example = "ADMIN")
        @NotNull Role role
) {}
