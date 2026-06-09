package com.example.orders.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemRequest(
        @NotNull(message = "Category ID is required") UUID categoryId,
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal price,
        Boolean available
) {}
