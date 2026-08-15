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
public class PreOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/preorders - Public submission of lead with auto-account provisioning (Section 4.10)")
    void testCreatePreOrder_Public_AutoProvisioning() throws Exception {
        String uniqueEmail = "binh_" + System.currentTimeMillis() + "@gmail.com";
        String leadJson = String.format("""
                {
                    "fullName": "Trần Văn Bình",
                    "phone": "+84988%06d",
                    "email": "%s",
                    "color": "Red",
                    "scooterModel": "Klara S",
                    "content": "Tôi muốn nhận xe tại showroom VinFast Royal City"
                }
                """, System.currentTimeMillis() % 1000000, uniqueEmail);

        mockMvc.perform(post("/api/v1/preorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message", containsString("Đơn đặt mua xe đã được ghi nhận")))
                .andExpect(jsonPath("$.data.preorderId", startsWith("PO-2028-")))
                .andExpect(jsonPath("$.data.accountCreated").value(true))
                .andExpect(jsonPath("$.data.email").value(uniqueEmail))
                .andExpect(jsonPath("$.data.scooterModel").value("Klara S"))
                .andExpect(jsonPath("$.data.redirectLoginUrl", containsString("/login?email=")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET & PATCH /api/v1/preorders - Staff management of leads")
    void testGetPreOrders_AndUpdateStatus() throws Exception {
        String uniqueEmail = "my_" + System.currentTimeMillis() + "@gmail.com";
        String leadJson = String.format("""
                {
                    "fullName": "Trần Thị Mỹ",
                    "phone": "+84918%06d",
                    "email": "%s",
                    "color": "Pearl White",
                    "scooterModel": "VinFast Feliz S",
                    "content": "Tư vấn gói thuê pin"
                }
                """, System.currentTimeMillis() % 1000000, uniqueEmail);

        String createResponse = mockMvc.perform(post("/api/v1/preorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long leadId = objectMapper.readTree(createResponse).path("data").path("numericId").asLong();

        // 1. Get by ID
        mockMvc.perform(get("/api/v1/preorders/" + leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Trần Thị Mỹ"));

        // 2. Update status to CONTACTED
        String statusUpdateJson = """
                {
                    "status": "CONTACTED"
                }
                """;

        mockMvc.perform(patch("/api/v1/preorders/" + leadId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Contacted"));
    }
}
