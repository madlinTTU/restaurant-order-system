package com.example.orders.order;

import com.example.orders.TestFactory;
import com.example.orders.auth.model.User;
import com.example.orders.auth.repository.UserRepository;
import com.example.orders.config.SecurityUtils;
import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.model.MenuItem;
import com.example.orders.menu.repository.MenuItemRepository;
import com.example.orders.order.dto.AdminOrderResponse;
import com.example.orders.order.dto.CreateOrderRequest;
import com.example.orders.order.dto.OrderFilterRequest;
import com.example.orders.order.dto.OrderFilterRequest.SortBy;
import com.example.orders.order.dto.OrderFilterRequest.SortDir;
import com.example.orders.order.dto.OrderResponse;
import com.example.orders.order.event.OrderEvent;
import com.example.orders.order.event.OrderEventProducer;
import com.example.orders.order.mapper.OrderMapper;
import com.example.orders.order.model.Order;
import com.example.orders.order.model.OrderStatus;
import com.example.orders.order.repository.OrderEventRepository;
import com.example.orders.order.repository.OrderRepository;
import com.example.orders.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  OrderRepository orderRepository;
  @Mock
  OrderEventRepository orderEventRepository;
  @Mock
  MenuItemRepository menuItemRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  OrderMapper orderMapper;
  @Mock
  SecurityUtils securityUtils;
  @Mock
  OrderEventProducer orderEventProducer;

  @InjectMocks
  OrderService orderService;


  @Test
  void createOrder_savesAndReturnsOrder() {
    UUID userId = UUID.randomUUID();
    MenuCategory category = TestFactory.menuCategory();
    MenuItem menuItem = TestFactory.menuItem(category);
    CreateOrderRequest request = TestFactory.createOrderRequest(menuItem.getId());

    Order savedOrder = TestFactory.order(userId);
    OrderEvent savedEvent = TestFactory.orderEvent(savedOrder.getId(), OrderStatus.PLACED);
    OrderResponse response = TestFactory.orderResponse(savedOrder);

    when(menuItemRepository.findAllById(List.of(request.items().getFirst().menuItemId()))).thenReturn(List.of(menuItem));
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
    when(orderEventRepository.save(any(OrderEvent.class))).thenReturn(savedEvent);
    when(orderMapper.toResponse(savedOrder)).thenReturn(response);

    OrderResponse result = orderService.createOrder(request, userId);

    assertThat(result).isEqualTo(response);
    verify(orderRepository).save(any(Order.class));
    verify(orderEventProducer).publish(argThat(e ->
        e.eventId().equals(savedEvent.getId()) &&
        e.customerId().equals(savedOrder.getUserId()) &&
        e.status() == OrderStatus.PLACED &&
        e.payload().orderId().equals(savedOrder.getId())
    ));
  }

  @Test
  void createOrder_throwsWhenMenuItemNotFound() {
    UUID userId = UUID.randomUUID();
    CreateOrderRequest request = TestFactory.createOrderRequest(UUID.randomUUID());

    when(menuItemRepository.findAllById(List.of(request.items().getFirst().menuItemId()))).thenReturn(List.of());

    assertThatThrownBy(() -> orderService.createOrder(request, userId))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createOrder_throwsWhenMenuItemNotAvailable() {
    UUID userId = UUID.randomUUID();
    MenuCategory category = TestFactory.menuCategory();
    MenuItem menuItem = TestFactory.menuItem(category);
    menuItem.setAvailable(false);
    CreateOrderRequest request = TestFactory.createOrderRequest(menuItem.getId());

    when(menuItemRepository.findAllById(List.of(request.items().getFirst().menuItemId()))).thenReturn(List.of(menuItem));

    assertThatThrownBy(() -> orderService.createOrder(request, userId))
      .isInstanceOf(IllegalArgumentException.class);
  }


  @Test
  void getActiveOrders_returnsOnlyActiveStatuses() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    OrderResponse response = TestFactory.orderResponse(order);

    when(orderRepository.findAllByOrderStatusIn(argThat(statuses ->
        statuses.contains(OrderStatus.PLACED) &&
        statuses.contains(OrderStatus.CONFIRMED) &&
        statuses.contains(OrderStatus.PREPARING) &&
        statuses.contains(OrderStatus.READY) &&
        !statuses.contains(OrderStatus.DELIVERED)
    ))).thenReturn(List.of(order));
    when(orderMapper.toResponse(order)).thenReturn(response);

    List<OrderResponse> result = orderService.getActiveOrders();

    assertThat(result).isEqualTo(List.of(response));
  }

  @Test
  void getOrders_returnsAllOrdersForAdmin() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    OrderResponse response = TestFactory.orderResponse(order);

    when(securityUtils.isAdmin()).thenReturn(true);
    when(orderRepository.findAll()).thenReturn(List.of(order));
    when(orderMapper.toResponse(order)).thenReturn(response);

    List<OrderResponse> result = orderService.getOrders(userId);

    assertThat(result).isEqualTo(List.of(response));
    verify(orderRepository).findAll();
    verify(orderRepository, never()).findAllByUserId(any());
  }

  @Test
  void getOrders_returnsOnlyOwnOrdersForCustomer() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    OrderResponse response = TestFactory.orderResponse(order);

    when(securityUtils.isAdmin()).thenReturn(false);
    when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(order));
    when(orderMapper.toResponse(order)).thenReturn(response);

    List<OrderResponse> result = orderService.getOrders(userId);

    assertThat(result).isEqualTo(List.of(response));
    verify(orderRepository).findAllByUserId(userId);
    verify(orderRepository, never()).findAll();
  }


  @Test
  void getOrder_returnsOrderForOwner() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    OrderResponse response = TestFactory.orderResponse(order);

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(securityUtils.isAdmin()).thenReturn(false);
    when(orderMapper.toResponse(order)).thenReturn(response);

    OrderResponse result = orderService.getOrder(order.getId(), userId);

    assertThat(result).isEqualTo(response);
  }

  @Test
  void getOrder_returnsOrderForAdmin() {
    UUID ownerId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    Order order = TestFactory.order(ownerId);
    OrderResponse response = TestFactory.orderResponse(order);

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(securityUtils.isAdmin()).thenReturn(true);
    when(orderMapper.toResponse(order)).thenReturn(response);

    OrderResponse result = orderService.getOrder(order.getId(), adminId);

    assertThat(result).isEqualTo(response);
  }

  @Test
  void getOrder_throwsWhenNotFound() {
    UUID ownerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> orderService.getOrder(orderId, ownerId))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getOrder_throwsWhenNotOwner() {
    UUID ownerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    Order order = TestFactory.order(ownerId);

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    assertThatThrownBy(() -> orderService.getOrder(order.getId(), otherUserId))
      .isInstanceOf(ResourceNotFoundException.class);
  }


  @Test
  void updateStatus_updatesStatus() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    OrderResponse response = TestFactory.orderResponse(order);

    OrderEvent savedEvent = TestFactory.orderEvent(order.getId(), OrderStatus.CONFIRMED);

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(orderEventRepository.save(any(OrderEvent.class))).thenReturn(savedEvent);
    when(orderMapper.toResponse(order)).thenReturn(response);

    OrderResponse result = orderService.updateStatus(order.getId(), OrderStatus.CONFIRMED, userId);

    assertThat(result).isEqualTo(response);
    assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    verify(orderEventProducer).publish(argThat(e ->
        e.eventId().equals(savedEvent.getId()) &&
        e.customerId().equals(order.getUserId()) &&
        e.status() == OrderStatus.CONFIRMED &&
        e.payload().orderId().equals(order.getId())
    ));
  }

  @Test
  void updateStatus_throwsWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.updateStatus(orderId, OrderStatus.CONFIRMED, userId))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateStatus_throwsWhenOrderIsTerminal() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    order.setOrderStatus(OrderStatus.DELIVERED);

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateStatus(order.getId(), OrderStatus.CONFIRMED, userId))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateStatus_throwsWhenSkippingStatus() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    order.setOrderStatus(OrderStatus.PLACED);

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.updateStatus(order.getId(), OrderStatus.PREPARING, userId))
      .isInstanceOf(IllegalArgumentException.class);
  }


  @Test
  void getAdminOrders_withNoFilters_returnsAllOrdersWithEmails() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    User user = TestFactory.user("customer@test.com");
    user.setId(userId);
    OrderResponse orderResponse = TestFactory.orderResponse(order);
    OrderFilterRequest filters = new OrderFilterRequest(null, null, null, null, null, null);

    when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(order));
    when(userRepository.findByIdIn(anySet())).thenReturn(List.of(user));
    when(orderMapper.toResponse(order)).thenReturn(orderResponse);

    List<AdminOrderResponse> result = orderService.getAdminOrders(filters);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().orderData()).isEqualTo(orderResponse);
    assertThat(result.getFirst().customerEmail()).isEqualTo("customer@test.com");
  }

  @Test
  void getAdminOrders_withEmailFilterMatchingUser_filtersCorrectly() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    User user = TestFactory.user("john@test.com");
    user.setId(userId);
    OrderResponse orderResponse = TestFactory.orderResponse(order);
    OrderFilterRequest filters = new OrderFilterRequest(null, "john", null, null, null, null);

    when(userRepository.findIdsByEmailContainingIgnoreCase("john")).thenReturn(Set.of(userId));
    when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(order));
    when(userRepository.findByIdIn(anySet())).thenReturn(List.of(user));
    when(orderMapper.toResponse(order)).thenReturn(orderResponse);

    List<AdminOrderResponse> result = orderService.getAdminOrders(filters);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().customerEmail()).isEqualTo("john@test.com");
  }

  @Test
  void getAdminOrders_withEmailFilterMatchingNoUser_returnsEmptyWithoutQueryingOrders() {
    OrderFilterRequest filters = new OrderFilterRequest(null, "notfound@test.com", null, null, null, null);

    when(userRepository.findIdsByEmailContainingIgnoreCase("notfound@test.com")).thenReturn(Set.of());

    List<AdminOrderResponse> result = orderService.getAdminOrders(filters);

    assertThat(result).isEmpty();
    verify(orderRepository, never()).findAll(any(Specification.class), any(Sort.class));
  }

  @Test
  void getAdminOrders_withBlankEmailFilter_doesNotCallUserRepository() {
    OrderFilterRequest filters = new OrderFilterRequest(null, "  ", null, null, null, null);

    when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

    orderService.getAdminOrders(filters);

    verify(userRepository, never()).findIdsByEmailContainingIgnoreCase(any());
  }

  @Test
  void getAdminOrders_whenUserIdNotFoundForOrder_throwsResourceNotFoundException() {
    UUID userId = UUID.randomUUID();
    Order order = TestFactory.order(userId);
    OrderResponse orderResponse = TestFactory.orderResponse(order);
    OrderFilterRequest filters = new OrderFilterRequest(null, null, null, null, null, null);

    when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(order));
    when(userRepository.findByIdIn(anySet())).thenReturn(List.of());
    when(orderMapper.toResponse(order)).thenReturn(orderResponse);

    assertThatThrownBy(() -> orderService.getAdminOrders(filters))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining(userId.toString());
  }

  @Test
  void getAdminOrders_withEmptyOrderList_returnsEmptyWithoutFetchingUsers() {
    OrderFilterRequest filters = new OrderFilterRequest(null, null, null, null, null, null);

    when(orderRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());

    List<AdminOrderResponse> result = orderService.getAdminOrders(filters);

    assertThat(result).isEmpty();
    verify(userRepository, never()).findByIdIn(any());
  }

  @ParameterizedTest
  @MethodSource("sortCases")
  void getAdminOrders_sortsBy(SortBy sortBy, SortDir sortDir, String expectedField, Direction expectedDirection) {
    OrderFilterRequest filters = new OrderFilterRequest(null, null, null, null, sortBy, sortDir);
    ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

    when(orderRepository.findAll(any(Specification.class), sortCaptor.capture())).thenReturn(List.of());

    orderService.getAdminOrders(filters);

    Sort.Order order = sortCaptor.getValue().getOrderFor(expectedField);
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(expectedDirection);
  }

  static Stream<Arguments> sortCases() {
    return Stream.of(
      Arguments.of(null,                    null,          "createdAt",  Direction.DESC),
      Arguments.of(SortBy.CREATED_AT,       SortDir.ASC,   "createdAt",  Direction.ASC),
      Arguments.of(SortBy.CREATED_AT,       SortDir.DESC,  "createdAt",  Direction.DESC),
      Arguments.of(SortBy.LAST_MODIFIED_AT, SortDir.ASC,   "modifiedAt", Direction.ASC),
      Arguments.of(SortBy.LAST_MODIFIED_AT, SortDir.DESC,  "modifiedAt", Direction.DESC),
      Arguments.of(SortBy.TOTAL_PRICE,      SortDir.ASC,   "totalPrice", Direction.ASC),
      Arguments.of(SortBy.TOTAL_PRICE,      SortDir.DESC,  "totalPrice", Direction.DESC)
    );
  }
}
