package com.example.orders.menu.controller;

import com.example.orders.menu.dto.ImageUploadUrlResponse;
import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.menu.dto.MenuItemResponse;
import com.example.orders.menu.dto.PositionEntry;
import com.example.orders.menu.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.ResponseEntity.noContent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Menu - Items")
@RestController
@RequestMapping("/api/menu/items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService itemService;

    @Operation(summary = "Get all menu items")
    @ApiResponse(responseCode = "200", description = "List of menu items")
    @GetMapping
    public List<MenuItemResponse> getAll() {
        return itemService.getAll();
    }

    @Operation(summary = "Get a menu item by ID")
    @ApiResponse(responseCode = "200", description = "Menu item found")
    @ApiResponse(responseCode = "404", description = "Menu item not found", content = @Content)
    @GetMapping("/{id}")
    public MenuItemResponse getById(@PathVariable UUID id) {
        return itemService.getById(id);
    }

    @Operation(summary = "Create a new menu item")
    @ApiResponse(responseCode = "201", description = "Menu item created")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return itemService.create(request, UUID.fromString(userId));
    }

    @Operation(summary = "Update a menu item")
    @ApiResponse(responseCode = "200", description = "Menu item updated")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Menu item not found", content = @Content)
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public MenuItemResponse update(
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return itemService.update(itemId, request, UUID.fromString(userId));
    }

    @Operation(summary = "Generate a pre-signed S3 URL for uploading a menu item image",
            description = "Upload the image directly to the returned URL via HTTP PUT with the matching Content-Type header")
    @ApiResponse(responseCode = "200", description = "Upload URL generated")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Menu item not found", content = @Content)
    @PostMapping("/{id}/image-upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ImageUploadUrlResponse generateImageUploadUrl(
            @PathVariable UUID id,
            @RequestParam String contentType
    ) {
        return itemService.generateImageUploadUrl(id, contentType);
    }

    @Operation(summary = "Delete a menu item")
    @ApiResponse(responseCode = "204", description = "Menu item deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Menu item not found", content = @Content)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }

    @Operation(summary = "Update positions of menu items")
    @ApiResponse(responseCode = "204", description = "Positions updated")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Admin role required", content = @Content)
    @PatchMapping("/positions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePositions(@Valid @RequestBody List<PositionEntry> entries) {
        itemService.updatePositions(entries);
        return noContent().build();
    }
}
