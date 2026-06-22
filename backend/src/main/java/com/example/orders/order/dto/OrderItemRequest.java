package com.example.orders.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "A single item within an order request")
public record OrderItemRequest(
        @Schema(description = "Menu item ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "Menu item ID is required") UUID menuItemId,
        @Schema(description = "Quantity", example = "2")
        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity
) {}
