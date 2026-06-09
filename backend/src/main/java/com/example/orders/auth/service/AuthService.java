package com.example.orders.auth.service;

import com.example.orders.auth.dto.LoginRequest;
import com.example.orders.auth.dto.RegisterRequest;
import com.example.orders.auth.dto.TokenResponse;
import com.example.orders.auth.mapper.UserMapper;
import com.example.orders.auth.model.User;
import com.example.orders.auth.repository.UserRepository;
import com.example.orders.config.JwtProperties;
import com.example.orders.exception.InvalidCredentialsException;
import com.example.orders.exception.InvalidTokenException;
import com.example.orders.exception.UserAlreadyExistsException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public TokenResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        User user = userMapper.toEntity(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return issueTokens(user, response);
    }

    public TokenResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return issueTokens(user, response);
    }

    public void logout(String accessToken, String refreshToken, HttpServletResponse response) {
        if (!jwtService.isTokenValid(accessToken)) {
            throw new InvalidTokenException("Invalid token");
        }
        String jti = jwtService.extractJti(accessToken);
        long ttl = jwtService.getRemainingTtlSeconds(accessToken);
        if (ttl > 0) {
            redisTemplate.opsForValue().set("jwt:blacklist:" + jti, "1", Duration.ofSeconds(ttl));
        }
        if (refreshToken != null) {
            redisTemplate.delete("jwt:refresh:" + refreshToken);
        }
        clearRefreshCookie(response);
    }

    public TokenResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token missing");
        }
        String userId = redisTemplate.opsForValue().get("jwt:refresh:" + refreshToken);
        if (userId == null) {
            throw new InvalidTokenException("Refresh token invalid or expired");
        }
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        redisTemplate.delete("jwt:refresh:" + refreshToken);
        return issueTokens(user, response);
    }

    private TokenResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "jwt:refresh:" + refreshToken,
                user.getId().toString(),
                Duration.ofSeconds(jwtProperties.getRefreshTokenExpiration())
        );
        setRefreshCookie(response, refreshToken);
        return new TokenResponse(accessToken, "Bearer");
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(Duration.ofSeconds(jwtProperties.getRefreshTokenExpiration()))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
