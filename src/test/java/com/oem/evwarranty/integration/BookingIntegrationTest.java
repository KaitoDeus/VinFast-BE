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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "DISPATCHER")
    @DisplayName("GET /api/v1/bookings - List bookings with KPI summary")
    void testGetBookings_WithKpis() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.kpis", notNullValue()))
                .andExpect(jsonPath("$.data.items", notNullValue()));
    }

    @Test
    @WithMockUser(username = "scstaff", roles = "SC_STAFF")
    @DisplayName("POST /api/v1/bookings - Create new booking and change status")
    void testCreateBooking_AndStatusTransition() throws Exception {
        String bookingJson = """
                {
                    "vehicleId": 1,
                    "rentalPlan": "DAILY",
                    "startDate": "2028-08-20",
                    "endDate": "2028-08-25",
                    "notes": "Giao xe tai san bay Noi Bai",
                    "totalAmount": 480.0
                }
                """;

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id", startsWith("BK-2028-")))
                .andExpect(jsonPath("$.data.totalAmount").value(480.0))
                .andExpect(jsonPath("$.data.status").value("Approved"));
    }
}
