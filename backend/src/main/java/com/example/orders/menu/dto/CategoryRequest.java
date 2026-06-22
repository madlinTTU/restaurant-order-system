package com.example.orders.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for creating or updating a menu category")
public record CategoryRequest(
        @Schema(description = "Category name", example = "Burgers")
        @NotBlank(message = "Name is required") String name,
        @Schema(description = "Category description", example = "Juicy burgers made fresh daily")
        String description
) {}
