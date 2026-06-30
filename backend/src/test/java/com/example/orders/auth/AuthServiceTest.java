package com.example.orders.auth;

import com.example.orders.TestFactory;
import com.example.orders.auth.dto.TokenResponse;
import com.example.orders.auth.mapper.UserMapper;
import com.example.orders.auth.model.User;
import com.example.orders.auth.repository.UserRepository;
import com.example.orders.auth.service.AuthService;
import com.example.orders.auth.service.JwtService;
import com.example.orders.exception.InvalidCredentialsException;
import com.example.orders.exception.InvalidTokenException;
import com.example.orders.exception.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock HttpServletResponse response;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(jwtService.getRefreshTokenExpiration()).thenReturn(604800L);
    }

    // --- register ---

    @Test
    void register_success() {
        User user = TestFactory.user();
        when(userRepository.existsByEmail(TestFactory.DEFAULT_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TestFactory.DEFAULT_PASSWORD)).thenReturn("passwordHash");
        when(userMapper.toEntity(TestFactory.DEFAULT_EMAIL, "passwordHash")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        TokenResponse result = authService.register(TestFactory.registerRequest(), response);

        assertThat(result.accessToken()).isEqualTo("accessToken");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(user);
        verify(valueOps).set(eq("jwt:refresh:refreshToken"), eq(user.getId().toString()), any());
    }

    @Test
    void register_throwsWhenEmailTaken() {
        when(userRepository.existsByEmail(TestFactory.DEFAULT_EMAIL)).thenReturn(true);
        assertThatThrownBy(() -> authService.register(TestFactory.registerRequest(), response))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    // --- login ---

    @Test
    void login_success() {
        User user = TestFactory.user();
        when(userRepository.findByEmail(TestFactory.DEFAULT_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TestFactory.DEFAULT_PASSWORD, user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        TokenResponse result = authService.login(TestFactory.loginRequest(), response);

        assertThat(result.accessToken()).isEqualTo("accessToken");
        assertThat(result.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByEmail(TestFactory.DEFAULT_EMAIL)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(TestFactory.loginRequest(), response))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsWhenPasswordWrong() {
        User user = TestFactory.user();
        when(userRepository.findByEmail(TestFactory.DEFAULT_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TestFactory.DEFAULT_PASSWORD, user.getPasswordHash())).thenReturn(false);
        assertThatThrownBy(() -> authService.login(TestFactory.loginRequest(), response))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // --- logout ---

    @Test
    void logout_blacklistsAccessTokenAndDeletesRefreshToken() {
        when(jwtService.isTokenValid("accessToken")).thenReturn(true);
        when(jwtService.extractJti("accessToken")).thenReturn("jti-123");
        when(jwtService.getRemainingTtlSeconds("accessToken")).thenReturn(300L);

        authService.logout("accessToken", "refreshToken", response);

        verify(valueOps).set(eq("jwt:blacklist:jti-123"), eq("1"), any());
        verify(redisTemplate).delete("jwt:refresh:refreshToken");
    }

    @Test
    void logout_worksWithoutTokens() {
        authService.logout(null, null, response);
        verifyNoInteractions(valueOps);
        verify(redisTemplate, never()).delete(anyString());
    }

    // --- refresh ---

    @Test
    void refresh_success() {
        User user = TestFactory.user();
        String refreshToken = "refreshToken";
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(valueOps.get("jwt:refresh:refreshToken")).thenReturn(user.getId().toString());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("newRefreshToken");

        TokenResponse result = authService.refresh(refreshToken, response);

        assertThat(result.accessToken()).isEqualTo("accessToken");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        verify(redisTemplate).delete("jwt:refresh:refreshToken");
        verify(valueOps).set(eq("jwt:refresh:newRefreshToken"), eq(user.getId().toString()), any());
    }

    @Test
    void refresh_throwsWhenTokenMissing() {
        assertThatThrownBy(() -> authService.refresh(null, response))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refresh_throwsWhenTokenInvalid() {
        when(jwtService.isTokenValid("badToken")).thenReturn(false);
        assertThatThrownBy(() -> authService.refresh("badToken", response))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refresh_throwsWhenTokenNotInRedis() {
        when(jwtService.isTokenValid("orphanToken")).thenReturn(true);
        when(valueOps.get("jwt:refresh:orphanToken")).thenReturn(null);
        assertThatThrownBy(() -> authService.refresh("orphanToken", response))
                .isInstanceOf(InvalidTokenException.class);
    }
}
