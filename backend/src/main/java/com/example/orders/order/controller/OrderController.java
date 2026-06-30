package com.example.orders.order.controller;

import com.example.orders.order.dto.AdminOrderResponse;
import com.example.orders.order.dto.CreateOrderRequest;
import com.example.orders.order.dto.OrderFilterRequest;
import com.example.orders.order.dto.OrderResponse;
import com.example.orders.order.dto.UpdateOrderStatusRequest;
import com.example.orders.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order")
    @ApiResponse(responseCode = "201", description = "Order placed successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request,
                                     @AuthenticationPrincipal String userId) {
        return orderService.createOrder(request, UUID.fromString(userId));
    }

    @Operation(summary = "Get all orders for the authenticated customer")
    @ApiResponse(responseCode = "200", description = "List of orders")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal String userId) {
        return orderService.getOrders(UUID.fromString(userId));
    }

    @Operation(summary = "Get all orders with filters (admin only)")
    @ApiResponse(responseCode = "200", description = "List of orders")
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminOrderResponse> getAdminOrders(@ParameterObject @ModelAttribute OrderFilterRequest filter) {
        return orderService.getAdminOrders(filter);
    }

    @Operation(summary = "Get all active orders (for kitchen staff)")
    @ApiResponse(responseCode = "200", description = "List of active orders")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Kitchen or Admin role required", content = @Content)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public List<OrderResponse> getActiveOrders() {
        return orderService.getActiveOrders();
    }

    @Operation(summary = "Get a specific order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id, @AuthenticationPrincipal String userId) {
        return orderService.getOrder(id, UUID.fromString(userId));
    }

    @Operation(summary = "Update the status of an order")
    @ApiResponse(responseCode = "200", description = "Order status updated")
    @ApiResponse(responseCode = "400", description = "Invalid status transition", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal String userId) {
        return orderService.updateStatus(id, request.status(), UUID.fromString(userId));
    }
}
