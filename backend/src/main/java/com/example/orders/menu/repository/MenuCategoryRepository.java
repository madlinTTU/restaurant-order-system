package com.example.orders.menu.repository;

import com.example.orders.menu.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
}
