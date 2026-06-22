package com.example.orders.user.service;

import com.example.orders.auth.model.Role;
import com.example.orders.auth.model.User;
import com.example.orders.auth.repository.UserRepository;
import com.example.orders.exception.UserAlreadyExistsException;
import com.example.orders.user.dto.CreateUserRequest;
import com.example.orders.user.dto.UserResponse;
import com.example.orders.user.mapper.UserAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAdminMapper userMapper;

    public List<UserResponse> listAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID createdBy) {
        if (request.role() == Role.CUSTOMER) {
            throw new IllegalArgumentException("Cannot create CUSTOMER accounts via admin API");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        User user = userMapper.toEntity(request, passwordEncoder.encode(request.password()), createdBy);
        return userMapper.toResponse(userRepository.save(user));
    }
}
