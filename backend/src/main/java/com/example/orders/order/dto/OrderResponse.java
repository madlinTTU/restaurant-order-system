package com.example.orders.order.dto;

import com.example.orders.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order with its current status and items")
public record OrderResponse(
        @Schema(description = "Order ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Customer user ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID userId,
        @Schema(description = "Current order status")
        OrderStatus status,
        @Schema(description = "List of ordered items")
        List<OrderItemResponse> items,
        @Schema(description = "Total order price in EUR", example = "19.98")
        BigDecimal totalPrice,
        @Schema(description = "Special instructions for the kitchen", example = "No onions please")
        String notes,
        @Schema(description = "Order creation timestamp")
        OffsetDateTime createdAt,
        @Schema(description = "Last modification timestamp")
        OffsetDateTime modifiedAt
) {}
