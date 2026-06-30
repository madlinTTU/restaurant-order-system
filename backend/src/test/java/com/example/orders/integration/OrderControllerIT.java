package com.example.orders.integration;

import com.example.orders.TestFactory;
import com.example.orders.order.dto.CreateOrderRequest;
import com.example.orders.order.dto.UpdateOrderStatusRequest;
import com.example.orders.order.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerIT extends BaseIntegrationTest {

  @Autowired
  ObjectMapper objectMapper;

  private String adminToken;
  private String customerToken;
  private String kitchenToken;
  private String menuItemId;

  @BeforeEach
  void setup() throws Exception {
    adminToken = registerAdminAndGetToken("admin@test.com", "password123");
    customerToken = registerCustomerAndGetToken("customer@test.com", "password123");
    kitchenToken = registerKitchenAndGetToken("kitchen@test.com", "password123");
    menuItemId = createMenuItem();
  }

  private String createMenuItem() throws Exception {
    MvcResult catResult = mockMvc.perform(post("/api/menu/categories")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.categoryRequest())))
      .andExpect(status().isCreated())
      .andReturn();
    String categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asString();

    MvcResult itemResult = mockMvc.perform(post("/api/menu/items")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.menuItemRequest(UUID.fromString(categoryId)))))
      .andExpect(status().isCreated())
      .andReturn();
    return objectMapper.readTree(itemResult.getResponse().getContentAsString()).get("id").asString();
  }

  private String createOrder(String token) throws Exception {
    CreateOrderRequest request = TestFactory.createOrderRequest(UUID.fromString(menuItemId));
    MvcResult result = mockMvc.perform(post("/api/orders")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asString();
  }


  @Test
  void createOrder_returnsCreatedOrder() throws Exception {
    CreateOrderRequest request = TestFactory.createOrderRequest(UUID.fromString(menuItemId));

    mockMvc.perform(post("/api/orders")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.status").value("PLACED"))
      .andExpect(jsonPath("$.items.length()").value(1))
      .andExpect(jsonPath("$.totalPrice").value(9.99));
  }

  @Test
  void createOrder_forbiddenWhenNoToken() throws Exception {
    CreateOrderRequest request = TestFactory.createOrderRequest(UUID.fromString(menuItemId));

    mockMvc.perform(post("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void createOrder_notFoundWhenMenuItemNotExist() throws Exception {
    CreateOrderRequest request = TestFactory.createOrderRequest(UUID.randomUUID());

    mockMvc.perform(post("/api/orders")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isNotFound());
  }


  @Test
  void getActiveOrders_returnsActiveOrders() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/active")
        .header("Authorization", "Bearer " + kitchenToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].status").value("PLACED"));
  }

  @Test
  void getActiveOrders_adminCanAccess() throws Exception {
    mockMvc.perform(get("/api/orders/active")
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isOk());
  }

  @Test
  void getActiveOrders_forbiddenWhenCustomer() throws Exception {
    mockMvc.perform(get("/api/orders/active")
        .header("Authorization", "Bearer " + customerToken))
      .andExpect(status().isForbidden());
  }

  @Test
  void getActiveOrders_forbiddenWhenNoToken() throws Exception {
    mockMvc.perform(get("/api/orders/active"))
      .andExpect(status().isForbidden());
  }


  @Test
  void getOrders_adminSeesAllOrders() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders")
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getOrders_customerSeesOnlyOwnOrders() throws Exception {
    createOrder(customerToken);
    String otherToken = registerCustomerAndGetToken("other@test.com", "password123");
    createOrder(otherToken);

    mockMvc.perform(get("/api/orders")
        .header("Authorization", "Bearer " + customerToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getOrders_forbiddenWhenNoToken() throws Exception {
    mockMvc.perform(get("/api/orders"))
      .andExpect(status().isForbidden());
  }


  @Test
  void getOrder_returnsOrderForOwner() throws Exception {
    String orderId = createOrder(customerToken);

    mockMvc.perform(get("/api/orders/" + orderId)
        .header("Authorization", "Bearer " + customerToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(orderId));
  }

  @Test
  void getOrder_returnsOrderForAdmin() throws Exception {
    String orderId = createOrder(customerToken);

    mockMvc.perform(get("/api/orders/" + orderId)
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(orderId));
  }

  @Test
  void getOrder_notFoundWhenNotOwner() throws Exception {
    String orderId = createOrder(customerToken);
    String otherToken = registerCustomerAndGetToken("other@test.com", "password123");

    mockMvc.perform(get("/api/orders/" + orderId)
        .header("Authorization", "Bearer " + otherToken))
      .andExpect(status().isNotFound());
  }

  @Test
  void getOrder_notFoundWhenMissing() throws Exception {
    mockMvc.perform(get("/api/orders/" + UUID.randomUUID())
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isNotFound());
  }


  @Test
  void updateStatus_kitchenCanAdvanceStatus() throws Exception {
    String orderId = createOrder(customerToken);
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

    mockMvc.perform(patch("/api/orders/" + orderId + "/status")
        .header("Authorization", "Bearer " + kitchenToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  void updateStatus_adminCanAdvanceStatus() throws Exception {
    String orderId = createOrder(customerToken);
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

    mockMvc.perform(patch("/api/orders/" + orderId + "/status")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  void updateStatus_forbiddenWhenNotAdmin() throws Exception {
    String orderId = createOrder(customerToken);
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

    mockMvc.perform(patch("/api/orders/" + orderId + "/status")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void updateStatus_badRequestWhenInvalidTransition() throws Exception {
    String orderId = createOrder(customerToken);
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

    mockMvc.perform(patch("/api/orders/" + orderId + "/status")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }

  @Test
  void updateStatus_notFoundWhenOrderMissing() throws Exception {
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

    mockMvc.perform(patch("/api/orders/" + UUID.randomUUID() + "/status")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isNotFound());
  }


  // --- GET /api/orders/admin ---

  @Test
  void getAdminOrders_returnsOrdersWithCorrectStructure() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].orderData.id").exists())
      .andExpect(jsonPath("$[0].orderData.status").value("PLACED"))
      .andExpect(jsonPath("$[0].orderData.totalPrice").value(9.99))
      .andExpect(jsonPath("$[0].orderData.createdAt").exists())
      .andExpect(jsonPath("$[0].orderData.modifiedAt").exists())
      .andExpect(jsonPath("$[0].customerEmail").value("customer@test.com"));
  }

  @Test
  void getAdminOrders_forbiddenWhenNoToken() throws Exception {
    mockMvc.perform(get("/api/orders/admin"))
      .andExpect(status().isForbidden());
  }

  @Test
  void getAdminOrders_forbiddenWhenCustomer() throws Exception {
    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + customerToken))
      .andExpect(status().isForbidden());
  }

  @Test
  void getAdminOrders_forbiddenWhenKitchen() throws Exception {
    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + kitchenToken))
      .andExpect(status().isForbidden());
  }

  @Test
  void getAdminOrders_filterByMatchingStatus_returnsOrders() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("statuses", "PLACED"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getAdminOrders_filterByNonMatchingStatus_returnsEmpty() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("statuses", "DELIVERED"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getAdminOrders_filterByMultipleStatuses_returnsMatchingOrders() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("statuses", "PLACED", "CONFIRMED"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getAdminOrders_filterByEmailSearch_returnsMatchingOrders() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("userEmailSearch", "customer"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].customerEmail").value("customer@test.com"));
  }

  @Test
  void getAdminOrders_filterByEmailSearch_returnsEmptyWhenNoMatch() throws Exception {
    createOrder(customerToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("userEmailSearch", "notexistent"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getAdminOrders_filterByDateFrom_today_returnsOrders() throws Exception {
    createOrder(customerToken);
    String today = LocalDate.now().toString();

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("dateFrom", today))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getAdminOrders_filterByDateTill_yesterday_returnsEmpty() throws Exception {
    createOrder(customerToken);
    String yesterday = LocalDate.now().minusDays(1).toString();

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken)
        .param("dateTill", yesterday))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getAdminOrders_returnsOrdersForAllCustomers() throws Exception {
    createOrder(customerToken);
    String otherToken = registerCustomerAndGetToken("other@test.com", "password123");
    createOrder(otherToken);

    mockMvc.perform(get("/api/orders/admin")
        .header("Authorization", "Bearer " + adminToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2));
  }
}
