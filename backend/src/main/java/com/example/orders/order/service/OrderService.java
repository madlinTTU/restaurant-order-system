package com.example.orders.order.service;

import com.example.orders.config.SecurityUtils;
import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.model.MenuItem;
import com.example.orders.menu.repository.MenuItemRepository;
import com.example.orders.order.dto.CreateOrderRequest;
import com.example.orders.order.dto.OrderItemRequest;
import com.example.orders.order.dto.OrderResponse;
import com.example.orders.order.event.OrderEvent;
import com.example.orders.order.event.OrderEventPayload;
import com.example.orders.order.event.OrderEventProducer;
import com.example.orders.order.event.OrderStatusEvent;
import com.example.orders.order.mapper.OrderMapper;
import com.example.orders.order.model.Order;
import com.example.orders.order.model.OrderItem;
import com.example.orders.order.model.OrderStatus;
import com.example.orders.order.repository.OrderEventRepository;
import com.example.orders.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderEventRepository orderEventRepository;
  private final MenuItemRepository menuItemRepository;
  private final OrderMapper orderMapper;
  private final SecurityUtils securityUtils;
  private final OrderEventProducer orderEventProducer;

  @Transactional
  public OrderResponse createOrder(CreateOrderRequest request, UUID currentUserId) {
    List<UUID> menuItemIds = request.items().stream()
      .map(OrderItemRequest::menuItemId)
      .toList();

    Map<UUID, MenuItem> menuItemMap = menuItemRepository.findAllById(menuItemIds).stream()
      .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

    validateItems(request.items(), menuItemMap);
    Order order = initializeOrder(request, menuItemMap, currentUserId);

    Order savedOrder = orderRepository.save(order);
    saveAndPublishEvent(savedOrder, OrderStatus.PLACED);
    return orderMapper.toResponse(savedOrder);
  }

  private void saveAndPublishEvent(Order order, OrderStatus status) {
    OrderEvent savedEvent = orderEventRepository.save(OrderEvent.builder()
        .orderId(order.getId())
        .status(status)
        .build());
    orderEventProducer.publish(new OrderStatusEvent(
        savedEvent.getId(),
        order.getUserId(),
        status,
        savedEvent.getCreatedAt().toInstant(),
        new OrderEventPayload(order.getId(), order.getTotalPrice(), order.getNotes())
    ));
  }

  private Order initializeOrder(CreateOrderRequest request, Map<UUID, MenuItem> menuItemMap, UUID currentUserId) {
    Order order = new Order();
    order.setUserId(currentUserId);
    order.setNotes(request.notes());
    order.setCreatedBy(currentUserId);
    order.setModifiedBy(currentUserId);
    order.setTotalPrice(addOrderItems(request.items(), menuItemMap, order));
    return order;
  }

  private BigDecimal addOrderItems(List<OrderItemRequest> items, Map<UUID, MenuItem> menuItemMap, Order order) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (OrderItemRequest itemReq : items) {
      MenuItem menuItem = menuItemMap.get(itemReq.menuItemId());
      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(order);
      orderItem.setMenuItem(menuItem);
      orderItem.setQuantity(itemReq.quantity());
      orderItem.setUnitPrice(menuItem.getPrice());
      order.getItems().add(orderItem);
      totalPrice = totalPrice.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
    }
    return totalPrice;
  }

  private void validateItems(List<OrderItemRequest> items, Map<UUID, MenuItem> menuItemMap) {
    for (OrderItemRequest itemReq : items) {
      MenuItem menuItem = menuItemMap.get(itemReq.menuItemId());
      if (menuItem == null) {
        throw new ResourceNotFoundException("Menu item not found: " + itemReq.menuItemId());
      }
      if (!menuItem.isAvailable()) {
        throw new IllegalArgumentException("Menu item is not available: " + menuItem.getName());
      }
    }
  }

  public List<OrderResponse> getOrders(UUID currentUserId) {
    if (securityUtils.isAdmin()) {
      return orderRepository.findAll().stream()
        .map(orderMapper::toResponse)
        .toList();
    }
    return orderRepository.findAllByUserId(currentUserId).stream()
      .map(orderMapper::toResponse)
      .toList();
  }

  public OrderResponse getOrder(UUID orderId, UUID currentUserId) {
    Order order = orderRepository.findById(orderId)
      .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    if (!securityUtils.isAdmin() && !order.getUserId().equals(currentUserId)) {
      throw new ResourceNotFoundException("Order not found: " + orderId);
    }
    return orderMapper.toResponse(order);
  }

  @Transactional
  public OrderResponse updateStatus(UUID orderId, OrderStatus newStatus, UUID currentUserId) {
    Order order = orderRepository.findById(orderId)
      .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    if (!order.getOrderStatus().canTransitionTo(newStatus)) {
      throw new IllegalArgumentException("Cannot transition from " + order.getOrderStatus() + " to " + newStatus);
    }
    order.setOrderStatus(newStatus);
    order.setModifiedBy(currentUserId);
    saveAndPublishEvent(order, newStatus);
    return orderMapper.toResponse(order);
  }
}
