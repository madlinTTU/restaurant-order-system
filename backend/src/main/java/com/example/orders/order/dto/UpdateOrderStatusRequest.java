package com.example.orders.order.dto;

import com.example.orders.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required") OrderStatus status
) {}
