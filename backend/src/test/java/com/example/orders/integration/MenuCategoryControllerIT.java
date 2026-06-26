package com.example.orders.integration;

import com.example.orders.TestFactory;
import com.example.orders.menu.dto.CategoryRequest;
import com.example.orders.menu.dto.PositionEntry;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuCategoryControllerIT extends BaseIntegrationTest {

  @Autowired
  ObjectMapper objectMapper;

  private String adminToken;
  private String customerToken;

  @BeforeEach
  void setup() throws Exception {
    adminToken = registerAdminAndGetToken("admin@test.com", "password123");
    customerToken = registerCustomerAndGetToken("customer@test.com", "password123");
  }

  @Test
  void getAll_returnsEmptyList() throws Exception {
    mockMvc.perform(get("/api/menu/categories"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getAll_returnsCategoriesAfterCreate() throws Exception {
    CategoryRequest request = TestFactory.categoryRequest();

    mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated());

    mockMvc.perform(get("/api/menu/categories"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].name").value(request.name()));

  }

  @Test
  void create_returnsCreatedCategory() throws Exception {
    CategoryRequest request = TestFactory.categoryRequest();

    mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.name").value(request.name()))
      .andExpect(jsonPath("$.description").value(request.description()))
      .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void create_forbiddenWhenNotAdmin() throws Exception {
    CategoryRequest request = TestFactory.categoryRequest();

    mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void create_forbiddenWhenNoToken() throws Exception {
    CategoryRequest request = TestFactory.categoryRequest();

    mockMvc.perform(post("/api/menu/categories")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void create_badRequestWhenNameIsBlank() throws Exception {
    CategoryRequest request = TestFactory.categoryRequest("");

    mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }


  @Test
  void update_returnsUpdatedCategory() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest())))
      .andExpect(status().isCreated())
      .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
    CategoryRequest updateRequest = TestFactory.categoryRequest("Pizza");

    mockMvc.perform(put("/api/menu/categories/" + id)
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Pizza"));
  }

  @Test
  void update_forbiddenWhenNotAdmin() throws Exception {
    mockMvc.perform(put("/api/menu/categories/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest("Pizza"))))
      .andExpect(status().isForbidden());
  }

  @Test
  void update_notFoundWhenCategoryNotExist() throws Exception {
    mockMvc.perform(put("/api/menu/categories/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest("Pizza"))))
      .andExpect(status().isNotFound());
  }


  @Test
  void delete_noContent() throws Exception {
    MvcResult createResult = mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest())))
      .andExpect(status().isCreated())
      .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(delete("/api/menu/categories/" + id)
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isNoContent());
  }

  @Test
  void delete_forbiddenWhenNotAdmin() throws Exception {
    mockMvc.perform(delete("/api/menu/categories/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + customerToken))
      .andExpect(status().isForbidden());
  }

  @Test
  void delete_notFoundWhenCategoryNotExist() throws Exception {
    mockMvc.perform(delete("/api/menu/categories/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isNotFound());
  }

  @Test
  void updatePositions_noContent() throws Exception {
    MvcResult r1 = mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest("Burgers"))))
      .andExpect(status().isCreated()).andReturn();
    MvcResult r2 = mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest("Pizza"))))
      .andExpect(status().isCreated()).andReturn();

    UUID id1 = UUID.fromString(objectMapper.readTree(r1.getResponse().getContentAsString()).get("id").asText());
    UUID id2 = UUID.fromString(objectMapper.readTree(r2.getResponse().getContentAsString()).get("id").asText());

    List<PositionEntry> entries = List.of(new PositionEntry(id1, 1), new PositionEntry(id2, 0));

    mockMvc.perform(patch("/api/menu/categories/positions")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(entries)))
      .andExpect(status().isNoContent());
  }

  @Test
  void updatePositions_forbiddenWhenNotAdmin() throws Exception {
    mockMvc.perform(patch("/api/menu/categories/positions")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(List.of())))
      .andExpect(status().isForbidden());
  }
}
