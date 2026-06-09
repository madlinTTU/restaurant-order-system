package com.example.orders.menu.mapper;

import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.menu.dto.MenuItemResponse;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.model.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    MenuItemResponse toResponse(MenuItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "createdBy", source = "currentUserId")
    @Mapping(target = "modifiedBy", source = "currentUserId")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "available", expression = "java(request.available() == null || request.available())")
    MenuItem toEntity(MenuItemRequest request, MenuCategory category, UUID currentUserId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "modifiedBy", source = "currentUserId")
    @Mapping(target = "category", source = "category")
    void updateEntity(MenuItemRequest request, MenuCategory category, UUID currentUserId, @MappingTarget MenuItem item);
}
