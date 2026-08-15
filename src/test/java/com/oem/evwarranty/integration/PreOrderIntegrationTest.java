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
    @DisplayName("POST /api/v1/preorders - Public submission of lead from Landing Page")
    void testCreatePreOrder_Public() throws Exception {
        String leadJson = """
                {
                    "fullName": "Nguyen Van Binh",
                    "phone": "0987654321",
                    "email": "binh.nguyen@vinfast.vn",
                    "color": "Crimson Red",
                    "scooterModel": "VinFast Evo 200",
                    "content": "Muốn lái thử xe tại Landmark 81"
                }
                """;

        mockMvc.perform(post("/api/v1/preorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Binh"))
                .andExpect(jsonPath("$.data.scooterModel").value("VinFast Evo 200"))
                .andExpect(jsonPath("$.data.status").value("Pending"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET & PATCH /api/v1/preorders - Staff management of leads")
    void testGetPreOrders_AndUpdateStatus() throws Exception {
        String leadJson = """
                {
                    "fullName": "Tran Thi My",
                    "phone": "0918112233",
                    "email": "my.tran@vinfast.vn",
                    "color": "Pearl White",
                    "scooterModel": "VinFast Feliz S",
                    "content": "Tư vấn gói thuê pin"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/preorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leadJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long leadId = objectMapper.readTree(createResponse).path("data").path("numericId").asLong();

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
