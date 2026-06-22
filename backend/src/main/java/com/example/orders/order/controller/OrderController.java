package com.example.orders.order.controller;

import com.example.orders.order.dto.CreateOrderRequest;
import com.example.orders.order.dto.OrderResponse;
import com.example.orders.order.dto.UpdateOrderStatusRequest;
import com.example.orders.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal String userId) {
        return orderService.createOrder(request, UUID.fromString(userId));
    }

    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal String userId) {
        return orderService.getOrders(UUID.fromString(userId));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public List<OrderResponse> getActiveOrders() {
        return orderService.getActiveOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id, @AuthenticationPrincipal String userId) {
        return orderService.getOrder(id, UUID.fromString(userId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal String userId) {
        return orderService.updateStatus(id, request.status(), UUID.fromString(userId));
    }
}
