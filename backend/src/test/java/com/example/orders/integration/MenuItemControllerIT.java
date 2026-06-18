package com.example.orders.integration;

import com.example.orders.TestFactory;
import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuItemControllerIT extends BaseIntegrationTest {

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  StorageService storageService;

  private String adminToken;
  private String customerToken;
  private String categoryId;

  @BeforeEach
  void setup() throws Exception {
    adminToken = registerAdminAndGetToken("admin@test.com", "password123");
    customerToken = registerCustomerAndGetToken("customer@test.com", "password123");
    categoryId = createCategory(adminToken);
  }

  private String createCategory(String token) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest())))
      .andExpect(status().isCreated())
      .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asString();
  }

  private String createItem(String token, UUID catId) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.menuItemRequest(catId))))
      .andExpect(status().isCreated())
      .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asString();
  }

  @Test
  void getAll_returnsEmptyList() throws Exception {
    mockMvc.perform(get("/api/menu/items"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void getAll_returnsItemsAfterCreate() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.fromString(categoryId));
    mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated());

    mockMvc.perform(get("/api/menu/items"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].name").value(request.name()));
  }

  @Test
  void getById_returnsItem() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.fromString(categoryId));
    MvcResult createResult = mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

    mockMvc.perform(get("/api/menu/items/" + id))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id))
      .andExpect(jsonPath("$.name").value(request.name()));
  }

  @Test
  void getById_notFoundWhenMissing() throws Exception {
    mockMvc.perform(get("/api/menu/items/" + UUID.randomUUID()))
      .andExpect(status().isNotFound());
  }

  @Test
  void create_returnsCreatedItem() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.fromString(categoryId));

    mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.name").value(request.name()))
      .andExpect(jsonPath("$.price").value(request.price()));
  }

  @Test
  void create_forbiddenWhenNotAdmin() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.fromString(categoryId));

    mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void create_notFoundWhenCategoryNotExist() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.randomUUID());

    mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isNotFound());
  }

  @Test
  void update_returnsUpdatedItem() throws Exception {
    String id = createItem(adminToken, UUID.fromString(categoryId));
    MenuItemRequest updateRequest = TestFactory.menuItemRequest(UUID.fromString(categoryId), "Updated Burger");

    mockMvc.perform(put("/api/menu/items/" + id)
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value(updateRequest.name()));
  }

  @Test
  void update_notFoundWhenItemNotExist() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.fromString(categoryId));

    mockMvc.perform(put("/api/menu/items/" + UUID.randomUUID())
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
  }

  @Test
  void update_forbiddenWhenNotAdmin() throws Exception {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.fromString(categoryId));

    mockMvc.perform(put("/api/menu/items/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void delete_noContent() throws Exception {
    String id = createItem(adminToken, UUID.fromString(categoryId));

    mockMvc.perform(delete("/api/menu/items/" + id)
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isNoContent());
  }

  @Test
  void delete_notFoundWhenItemNotExist() throws Exception {
    mockMvc.perform(delete("/api/menu/items/" + UUID.randomUUID())
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
  }


  @Test
  void delete_forbiddenWhenNotAdmin() throws Exception {
    mockMvc.perform(delete("/api/menu/items/" + UUID.randomUUID())
                    .header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isForbidden());
  }

  @Test
  void generateImageUploadUrl_returnsUrl() throws Exception {
    String id = createItem(adminToken, UUID.fromString(categoryId));
    when(storageService.generateKey(any(), any())).thenReturn("menu/images/test/file.jpg");
    when(storageService.generatePresignedUploadUrl(any())).thenReturn("https://minio/presigned-url");

    mockMvc.perform(post("/api/menu/items/" + id + "/image-upload-url")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("contentType", "image/jpeg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uploadUrl").exists());
  }

  @Test
  void generateImageUploadUrl_forbiddenWhenNotAdmin() throws Exception {
    mockMvc.perform(post("/api/menu/items/" + UUID.randomUUID() + "/image-upload-url")
                    .header("Authorization", "Bearer " + customerToken)
                    .param("contentType", "image/jpeg"))
            .andExpect(status().isForbidden());
  }

  @Test
  void generateImageUploadUrl_notFoundWhenItemNotExist() throws Exception {
    when(storageService.generateKey(any(), any())).thenReturn("menu/images/test/file.jpg");
    when(storageService.generatePresignedUploadUrl(any())).thenReturn("https://minio/presigned-url");

    mockMvc.perform(post("/api/menu/items/" + UUID.randomUUID() + "/image-upload-url")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("contentType", "image/jpeg"))
            .andExpect(status().isNotFound());
  }

  @Test
  void generateImageUploadUrl_badRequestWhenInvalidContentType() throws Exception {
    String id = createItem(adminToken, UUID.fromString(categoryId));
    when(storageService.generateKey(any(), any())).thenThrow(new IllegalArgumentException("Unsupported image type: video/mp4"));

    mockMvc.perform(post("/api/menu/items/" + id + "/image-upload-url")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("contentType", "video/mp4"))
            .andExpect(status().isBadRequest());
  }
}
