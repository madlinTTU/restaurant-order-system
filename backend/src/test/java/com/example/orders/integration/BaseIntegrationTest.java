package com.example.orders.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE order_items, order_events, orders, menu_items, menu_categories, users RESTART IDENTITY CASCADE");
    }

    protected String registerCustomerAndGetToken(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    protected String registerAdminAndGetToken(String email, String password) throws Exception {
        UUID id = UUID.randomUUID();
        String hash = passwordEncoder.encode(password);
        jdbcTemplate.update(
                "INSERT INTO users (uuid, email, password_hash, role, created_by, modified_by) VALUES (?, ?, ?, 'ADMIN', ?, ?)",
                id, email, hash, id, id
        );

        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"accessToken\":\"") + 15;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
