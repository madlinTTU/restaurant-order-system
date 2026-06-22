package com.example.orders.integration;

import com.example.orders.TestFactory;
import com.example.orders.auth.model.Role;
import com.example.orders.user.dto.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setup() throws Exception {
        adminToken = registerAdminAndGetToken("admin@test.com", "password123");
        customerToken = registerCustomerAndGetToken("customer@test.com", "password123");
    }

    // --- GET /api/admin/users ---

    @Test
    void listAll_returnsUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listAll_forbiddenWhenCustomer() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAll_forbiddenWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/admin/users ---

    @Test
    void create_createsKitchenUser() throws Exception {
        CreateUserRequest request = TestFactory.createUserRequest("kitchen@test.com", Role.KITCHEN);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("kitchen@test.com"))
                .andExpect(jsonPath("$.role").value("KITCHEN"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void create_createsAdminUser() throws Exception {
        CreateUserRequest request = TestFactory.createUserRequest("admin2@test.com", Role.ADMIN);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void create_conflictWhenEmailTaken() throws Exception {
        CreateUserRequest request = TestFactory.createUserRequest("kitchen@test.com", Role.KITCHEN);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_badRequestWhenCustomerRole() throws Exception {
        CreateUserRequest request = TestFactory.createUserRequest("cust@test.com", Role.CUSTOMER);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_badRequestWhenPasswordTooShort() throws Exception {
        CreateUserRequest request = new CreateUserRequest("kitchen@test.com", "123", Role.KITCHEN);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_forbiddenWhenCustomer() throws Exception {
        CreateUserRequest request = TestFactory.createUserRequest("kitchen@test.com", Role.KITCHEN);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
