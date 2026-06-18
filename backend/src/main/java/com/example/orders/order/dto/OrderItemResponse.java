package com.example.orders.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID menuItemId,
        String menuItemName,
        int quantity,
        BigDecimal unitPrice
) {}
