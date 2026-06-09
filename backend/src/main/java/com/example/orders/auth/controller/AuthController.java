package com.example.orders.auth.controller;

import com.example.orders.auth.dto.LoginRequest;
import com.example.orders.auth.dto.RegisterRequest;
import com.example.orders.auth.dto.TokenResponse;
import com.example.orders.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, refreshToken, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(refreshToken, response));
    }
}
