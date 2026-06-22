package com.example.orders.user.dto;

import com.example.orders.auth.model.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        Role role,
        OffsetDateTime createdAt
) {}
