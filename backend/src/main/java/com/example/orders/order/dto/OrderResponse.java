package com.example.orders.order.dto;

import com.example.orders.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        String notes,
        OffsetDateTime createdAt
) {}
