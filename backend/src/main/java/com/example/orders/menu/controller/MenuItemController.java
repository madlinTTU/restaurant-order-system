package com.example.orders.menu.controller;

import com.example.orders.menu.dto.ImageUploadUrlResponse;
import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.menu.dto.MenuItemResponse;
import com.example.orders.menu.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu/items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService itemService;

    @GetMapping
    public List<MenuItemResponse> getAll() {
        return itemService.getAll();
    }

    @GetMapping("/{id}")
    public MenuItemResponse getById(@PathVariable UUID id) {
        return itemService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return itemService.create(request, UUID.fromString(userId));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse update(
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return itemService.update(itemId, request, UUID.fromString(userId));
    }

    @PostMapping("/{id}/image-upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ImageUploadUrlResponse generateImageUploadUrl(
            @PathVariable UUID id,
            @RequestParam String contentType
    ) {
        return itemService.generateImageUploadUrl(id, contentType);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }
}
