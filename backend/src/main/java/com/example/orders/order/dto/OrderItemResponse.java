package com.example.orders.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "A single item within an order")
public record OrderItemResponse(
        @Schema(description = "Order item ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Menu item ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID menuItemId,
        @Schema(description = "Menu item name", example = "Classic Cheeseburger")
        String menuItemName,
        @Schema(description = "Quantity ordered", example = "2")
        int quantity,
        @Schema(description = "Price per unit at the time of ordering", example = "9.99")
        BigDecimal unitPrice
) {}
