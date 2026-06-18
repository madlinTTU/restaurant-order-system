package com.example.orders.order.mapper;

import com.example.orders.order.dto.OrderItemResponse;
import com.example.orders.order.dto.OrderResponse;
import com.example.orders.order.model.Order;
import com.example.orders.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", source = "orderStatus")
    OrderResponse toResponse(Order order);

    @Mapping(target = "menuItemId", source = "menuItem.id")
    @Mapping(target = "menuItemName", source = "menuItem.name")
    OrderItemResponse toItemResponse(OrderItem item);
}
