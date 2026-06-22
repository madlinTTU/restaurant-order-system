package com.example.orders.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request body for creating or updating a menu item")
public record MenuItemRequest(
        @Schema(description = "Category ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Category ID is required") UUID categoryId,
        @Schema(description = "Item name", example = "Classic Cheeseburger")
        @NotBlank(message = "Name is required") String name,
        @Schema(description = "Item description", example = "Beef patty with cheddar, lettuce and tomato")
        String description,
        @Schema(description = "Price in EUR", example = "9.99")
        @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal price,
        @Schema(description = "Whether the item is available for ordering", example = "true")
        Boolean available
) {}
