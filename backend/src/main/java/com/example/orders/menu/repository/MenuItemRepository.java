package com.example.orders.menu.repository;

import com.example.orders.menu.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findAllByImageUrlIsNotNull();

    List<MenuItem> findAllByOrderByCategoryIdAscPositionAsc();

    Optional<Integer> findTopByCategoryIdOrderByPositionDesc(UUID categoryId);
}
