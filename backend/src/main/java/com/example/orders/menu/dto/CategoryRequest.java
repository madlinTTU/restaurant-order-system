package com.example.orders.menu.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "Name is required") String name,
        String description
) {}
