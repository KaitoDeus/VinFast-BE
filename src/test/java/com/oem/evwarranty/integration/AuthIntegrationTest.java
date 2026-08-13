package com.oem.evwarranty.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("E2E Test: Full Authentication Flow - Login and access protected /me endpoint")
    void testLoginSuccess_AndAccessProtectedEndpointWithJwt() throws Exception {
        // 1. Perform Login
        String loginJson = "{\"username\": \"admin\", \"password\": \"password123\"}";

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String jwtToken = extractJwtToken(loginResult.getResponse().getContentAsString());

        // 2. Access protected /me endpoint using Bearer Token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("E2E Test: Login Failure - Invalid password returns 401 Unauthorized")
    void testLoginFailure_InvalidPassword() throws Exception {
        String loginJson = "{\"username\": \"admin\", \"password\": \"wrongpassword\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("E2E Test: Protected Endpoint - Request without token returns 401 Unauthorized")
    void testAccessProtectedEndpoint_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Extract JWT token from login response JSON.
     * Handles both {"token":"xxx"} and {"token": "xxx"} formats.
     */
    private String extractJwtToken(String responseJson) {
        // Find "token" key and extract value
        int tokenKeyIdx = responseJson.indexOf("\"token\"");
        int colonIdx = responseJson.indexOf(":", tokenKeyIdx);
        int valueStart = responseJson.indexOf("\"", colonIdx) + 1;
        int valueEnd = responseJson.indexOf("\"", valueStart);
        return responseJson.substring(valueStart, valueEnd);
    }
}
