package com.example.orders.order.specification;

import com.example.orders.order.model.Order;
import com.example.orders.order.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OrderSpecification {

    public static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        return (root, query, cb) ->
                (statuses == null || statuses.isEmpty()) ? null : root.get("orderStatus").in(statuses);
    }

    public static Specification<Order> hasUserIds(Set<UUID> userIds) {
        return (root, query, cb) ->
                (userIds == null || userIds.isEmpty()) ? null : root.get("userId").in(userIds);
    }

    public static Specification<Order> dateFrom(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    public static Specification<Order> dateTill(LocalDate till) {
        return (root, query, cb) ->
                till == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), till.atTime(23, 59, 59).atOffset(ZoneOffset.UTC));
    }
}
