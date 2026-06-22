package com.example.orders.order.repository;

import com.example.orders.order.model.Order;
import com.example.orders.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByUserId(UUID userId);
    List<Order> findAllByOrderStatusIn(List<OrderStatus> statuses);
}
