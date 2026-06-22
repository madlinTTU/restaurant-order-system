package com.example.orders.user.mapper;

import com.example.orders.auth.model.User;
import com.example.orders.user.dto.CreateUserRequest;
import com.example.orders.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserAdminMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "modifiedBy", source = "createdBy")
    User toEntity(CreateUserRequest request, String passwordHash, UUID createdBy);
}
