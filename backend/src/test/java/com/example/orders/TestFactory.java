package com.example.orders;

import com.example.orders.auth.dto.LoginRequest;
import com.example.orders.auth.dto.RegisterRequest;
import com.example.orders.auth.model.Role;
import com.example.orders.auth.model.User;
import com.example.orders.menu.dto.CategoryRequest;
import com.example.orders.menu.dto.CategoryResponse;
import com.example.orders.menu.dto.ImageUploadUrlResponse;
import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.menu.dto.MenuItemResponse;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.model.MenuItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TestFactory {

    public static final String DEFAULT_EMAIL = "user@test.com";
    public static final String DEFAULT_PASSWORD = "password123";

    // --- Auth ---

    public static RegisterRequest registerRequest() {
        return new RegisterRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    public static RegisterRequest registerRequest(String email) {
        return new RegisterRequest(email, DEFAULT_PASSWORD);
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    public static LoginRequest loginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    // --- Users ---

    public static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(DEFAULT_EMAIL);
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setRole(Role.CUSTOMER);
        user.setCreatedBy(user.getId());
        user.setModifiedBy(user.getId());
        return user;
    }

    public static User user(String email) {
        User user = user();
        user.setEmail(email);
        return user;
    }

    public static User adminUser() {
        User user = user();
        user.setRole(Role.ADMIN);
        return user;
    }

    // --- Menu categories ---

    public static CategoryRequest categoryRequest() {
        return new CategoryRequest("Burgers", "All our burgers");
    }

    public static CategoryRequest categoryRequest(String name) {
        return new CategoryRequest(name, null);
    }

    public static MenuCategory menuCategory() {
        MenuCategory category = new MenuCategory();
        category.setId(UUID.randomUUID());
        category.setName("Burgers");
        category.setDescription("All our burgers");
        category.setCreatedAt(OffsetDateTime.now());
        category.setModifiedAt(OffsetDateTime.now());
        category.setCreatedBy(UUID.randomUUID());
        category.setModifiedBy(UUID.randomUUID());
        category.setVersion(0L);
        return category;
    }

    public static CategoryResponse categoryResponse(MenuCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    // --- Menu items ---

    public static MenuItemRequest menuItemRequest(UUID categoryId) {
        return new MenuItemRequest(categoryId, "Classic Burger", "Beef patty", new BigDecimal("9.99"), true);
    }

    public static MenuItemRequest menuItemRequest(UUID categoryId, String name) {
        return new MenuItemRequest(categoryId, name, "Beef patty", new BigDecimal("9.99"), true);
    }

    public static ImageUploadUrlResponse imageUploadUrlResponse() {
        return new ImageUploadUrlResponse("https://minio/bucket/image-upload-url");
    }

    public static MenuItemResponse menuItemResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getImageUrl(),
                item.isAvailable()
        );
    }

    public static MenuItem menuItem(MenuCategory category) {
        MenuItem item = new MenuItem();
        item.setId(UUID.randomUUID());
        item.setCategory(category);
        item.setName("Classic Burger");
        item.setDescription("Beef patty");
        item.setPrice(new BigDecimal("9.99"));
        item.setAvailable(true);
        item.setCreatedAt(OffsetDateTime.now());
        item.setModifiedAt(OffsetDateTime.now());
        item.setCreatedBy(UUID.randomUUID());
        item.setModifiedBy(UUID.randomUUID());
        item.setVersion(0L);
        return item;
    }
}
