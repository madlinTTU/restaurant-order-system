package com.example.orders.order.dto;

import com.example.orders.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating an order status")
public record UpdateOrderStatusRequest(
        @Schema(description = "New order status")
        @NotNull(message = "Status is required") OrderStatus status
) {}
