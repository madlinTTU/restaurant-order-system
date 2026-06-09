package com.example.orders.auth.mapper;

import com.example.orders.auth.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(String email, String passwordHash);
}
