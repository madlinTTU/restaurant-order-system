package com.example.orders.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Menu item")
public record MenuItemResponse(
        @Schema(description = "Item ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Category ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID categoryId,
        @Schema(description = "Category name", example = "Burgers")
        String categoryName,
        @Schema(description = "Item name", example = "Classic Cheeseburger")
        String name,
        @Schema(description = "Item description", example = "Beef patty with cheddar, lettuce and tomato")
        String description,
        @Schema(description = "Price in EUR", example = "9.99")
        BigDecimal price,
        @Schema(description = "Public URL of the item image")
        String imageUrl,
        @Schema(description = "Whether the item is available for ordering", example = "true")
        boolean available
) {}
