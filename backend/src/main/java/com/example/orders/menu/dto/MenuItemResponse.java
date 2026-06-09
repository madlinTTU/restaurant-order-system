package com.example.orders.menu.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available
) {}
