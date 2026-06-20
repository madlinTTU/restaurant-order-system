package com.example.orders.order.event;

import com.example.orders.order.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusEvent(
    UUID eventId,
    UUID customerId,
    OrderStatus status,
    Instant timestamp,
    OrderEventPayload payload
) {}
