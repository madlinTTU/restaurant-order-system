package com.example.orders.order.dto;

public record AdminOrderResponse(
        OrderResponse orderData,
        String customerEmail
) {}
