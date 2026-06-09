package com.example.orders.menu.mapper;

import com.example.orders.menu.dto.CategoryRequest;
import com.example.orders.menu.dto.CategoryResponse;
import com.example.orders.menu.model.MenuCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MenuCategoryMapper {

    CategoryResponse toResponse(MenuCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", source = "currentUserId")
    @Mapping(target = "modifiedBy", source = "currentUserId")
    MenuCategory toEntity(CategoryRequest request, UUID currentUserId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "modifiedBy", source = "currentUserId")
    void updateEntity(CategoryRequest request, UUID currentUserId, @MappingTarget MenuCategory category);
}
