package com.oem.evwarranty.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientAndDriverIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "DISPATCHER")
    @DisplayName("GET /api/v1/clients - List clients with pagination")
    void testGetClients() throws Exception {
        mockMvc.perform(get("/api/v1/clients")
                        .param("page", "1")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", notNullValue()))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(5));
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    @DisplayName("POST /api/v1/clients - Create client profile")
    void testCreateClient() throws Exception {
        String uniqueEmail = "client_" + System.currentTimeMillis() + "@gmail.com";
        String clientJson = String.format("""
                {
                    "fullName": "Alice Johnson",
                    "email": "%s",
                    "phone": "+84901234567",
                    "address": "District 1, Ho Chi Minh City",
                    "residenceCardNumber": "079092001234",
                    "driverLicenseNumber": "B2-998877"
                }
                """, uniqueEmail);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Alice Johnson"))
                .andExpect(jsonPath("$.data.email").value(uniqueEmail));
    }

    @Test
    @WithMockUser(roles = "FLEET_MANAGER")
    @DisplayName("POST & PATCH /api/v1/drivers - Create driver and change duty status")
    void testCreateDriver_AndUpdateDutyStatus() throws Exception {
        String driverJson = String.format("""
                {
                    "fullName": "Nguyen Van Tai",
                    "phone": "+84912%06d",
                    "licenseNumber": "D-112233",
                    "experienceYears": 5,
                    "assignedVehicleId": 1
                }
                """, System.currentTimeMillis() % 1000000);

        String response = mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(driverJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Tai"))
                .andExpect(jsonPath("$.data.dutyStatus").value("On Duty"))
                .andReturn().getResponse().getContentAsString();

        // Extract numeric ID from response
        Long driverId = objectMapper.readTree(response).path("data").path("numericId").asLong();

        String statusUpdateJson = """
                {
                    "dutyStatus": "IN_TRANSIT"
                }
                """;

        mockMvc.perform(patch("/api/v1/drivers/" + driverId + "/duty-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dutyStatus").value("In Transit"));
    }
}
