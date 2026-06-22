package com.example.orders.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request body for placing a new order")
public record CreateOrderRequest(
        @Schema(description = "List of items to order")
        @NotEmpty(message = "Order must contain at least one item") List<@Valid OrderItemRequest> items,
        @Schema(description = "Special instructions for the kitchen", example = "No onions please")
        String notes
) {}
