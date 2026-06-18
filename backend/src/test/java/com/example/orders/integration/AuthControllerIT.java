package com.example.orders.integration;

import com.example.orders.TestFactory;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends BaseIntegrationTest {

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void register_returnsTokens() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").exists())
      .andExpect(jsonPath("$.tokenType").value("Bearer"))
      .andExpect(cookie().exists("refresh_token"))
      .andExpect(cookie().httpOnly("refresh_token", true));
  }

  @Test
  void register_conflictWhenEmailTaken() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isOk());

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isConflict());
  }

  @Test
  void register_badRequestWhenInvalidEmail() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest("notanemail"))))
      .andExpect(status().isBadRequest());
  }

  @Test
  void login_returnsTokens() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isOk());

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.loginRequest())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").exists())
      .andExpect(jsonPath("$.tokenType").value("Bearer"))
      .andExpect(cookie().exists("refresh_token"))
      .andExpect(cookie().httpOnly("refresh_token", true));
  }

  @Test
  void login_unauthorizedWhenWrongEmail() throws Exception {
    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.loginRequest())))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void login_unauthorizedWhenWrongPassword() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isOk());

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.loginRequest(TestFactory.DEFAULT_EMAIL, "wrongpassword"))))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void logout_clearsCookie() throws Exception {
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isOk());

    mockMvc.perform(post("/auth/logout"))
      .andExpect(status().isNoContent())
      .andExpect(cookie().maxAge("refresh_token", 0));
  }

  @Test
  void refresh_returnsNewTokens() throws Exception {
    MvcResult registerResult = mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(TestFactory.registerRequest())))
      .andExpect(status().isOk())
      .andReturn();

    Cookie refreshCookie = registerResult.getResponse().getCookie("refresh_token");

    mockMvc.perform(post("/auth/refresh")
        .cookie(refreshCookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").exists())
      .andExpect(cookie().exists("refresh_token"));
  }

  @Test
  void refresh_unauthorizedWhenNoCookie() throws Exception {
    mockMvc.perform(post("/auth/refresh"))
      .andExpect(status().isUnauthorized());
  }

}
