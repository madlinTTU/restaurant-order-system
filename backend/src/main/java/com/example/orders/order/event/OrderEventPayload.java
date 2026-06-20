package com.example.orders.order.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderEventPayload(UUID orderId, BigDecimal totalPrice, String notes) {}
