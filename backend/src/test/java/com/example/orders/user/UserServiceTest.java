package com.example.orders.user;

import com.example.orders.TestFactory;
import com.example.orders.auth.model.Role;
import com.example.orders.auth.model.User;
import com.example.orders.auth.repository.UserRepository;
import com.example.orders.exception.UserAlreadyExistsException;
import com.example.orders.user.dto.CreateUserRequest;
import com.example.orders.user.dto.UserResponse;
import com.example.orders.user.mapper.UserAdminMapper;
import com.example.orders.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserAdminMapper userMapper;

    @InjectMocks UserService userService;

    @Test
    void listAll_returnsAllUsers() {
        User user = TestFactory.user();
        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        List<UserResponse> result = userService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().email()).isEqualTo(user.getEmail());
    }

    @Test
    void listAll_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertThat(userService.listAll()).isEmpty();
    }


    @Test
    void createUser_success() {
        CreateUserRequest request = TestFactory.createUserRequest(Role.KITCHEN);
        User user = TestFactory.kitchenUser();
        UUID adminId = UUID.randomUUID();
        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hash");
        when(userMapper.toEntity(request, "hash", adminId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.createUser(request, adminId);

        assertThat(result.role()).isEqualTo(Role.KITCHEN);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_throwsWhenEmailTaken() {
        CreateUserRequest request = TestFactory.createUserRequest(Role.KITCHEN);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request, UUID.randomUUID()))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void createUser_throwsWhenCustomerRole() {
        CreateUserRequest request = TestFactory.createUserRequest(Role.CUSTOMER);

        assertThatThrownBy(() -> userService.createUser(request, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(userRepository);
    }
}
