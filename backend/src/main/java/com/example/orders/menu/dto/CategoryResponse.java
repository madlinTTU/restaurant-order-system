package com.example.orders.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Menu category")
public record CategoryResponse(
        @Schema(description = "Category ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Category name", example = "Burgers")
        String name,
        @Schema(description = "Category description", example = "Juicy burgers made fresh daily")
        String description,
        @Schema(description = "Position in the category list", example = "0")
        int position
) {}
