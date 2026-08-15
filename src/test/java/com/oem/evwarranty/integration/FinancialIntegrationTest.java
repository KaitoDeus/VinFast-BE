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
public class FinancialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "FINANCE_OFFICER")
    @DisplayName("GET /api/v1/financials/overview - Financial overview with 12-month revenue chart")
    void testGetFinancialOverview() throws Exception {
        mockMvc.perform(get("/api/v1/financials/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue", notNullValue()))
                .andExpect(jsonPath("$.data.netProfit", notNullValue()))
                .andExpect(jsonPath("$.data.monthlyRevenueChart", hasSize(12)));
    }

    @Test
    @WithMockUser(roles = "FINANCE_OFFICER")
    @DisplayName("POST & PATCH /api/v1/invoices - Create invoice and mark as paid")
    void testCreateInvoice_AndPay() throws Exception {
        String invoiceJson = """
                {
                    "amount": 350.00,
                    "paymentMethod": "VNPAY",
                    "status": "PENDING"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invoiceJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Pending"))
                .andReturn().getResponse().getContentAsString();

        Long invoiceId = objectMapper.readTree(response).path("data").path("numericId").asLong();

        mockMvc.perform(patch("/api/v1/invoices/" + invoiceId + "/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Completed"));
    }

    @Test
    @WithMockUser(roles = "FINANCE_OFFICER")
    @DisplayName("POST /api/v1/expenses - Record new expense")
    void testCreateExpense() throws Exception {
        String expenseJson = """
                {
                    "title": "Bảo hiểm thân vỏ VF 8",
                    "category": "INSURANCE",
                    "amount": 1250.00,
                    "recipientName": "Bảo hiểm Quân đội MIC",
                    "paymentMethod": "BANK_TRANSFER"
                }
                """;

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Bảo hiểm thân vỏ VF 8"))
                .andExpect(jsonPath("$.data.category").value("INSURANCE"));
    }
}
