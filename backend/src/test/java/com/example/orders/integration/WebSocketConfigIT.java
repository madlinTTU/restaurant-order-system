package com.example.orders.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebSocketConfigIT extends BaseIntegrationTest {

    @Test
    void sockJsInfoEndpointIsAccessible() throws Exception {
        mockMvc.perform(get("/ws/info"))
                .andExpect(status().isOk());
    }
}
