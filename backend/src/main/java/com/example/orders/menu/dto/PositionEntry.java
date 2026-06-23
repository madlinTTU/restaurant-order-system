package com.example.orders.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Position entry for a single item")
public record PositionEntry(
        @NotNull UUID id,
        int position
) {}
