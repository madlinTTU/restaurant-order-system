package com.example.orders.user.controller;

import com.example.orders.user.dto.CreateUserRequest;
import com.example.orders.user.dto.UserResponse;
import com.example.orders.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin - Users")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "List of users")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @GetMapping
    public List<UserResponse> listAll() {
        return userService.listAll();
    }

    @Operation(summary = "Create a new user with a specified role")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return userService.createUser(request, UUID.fromString(userId));
    }
}
