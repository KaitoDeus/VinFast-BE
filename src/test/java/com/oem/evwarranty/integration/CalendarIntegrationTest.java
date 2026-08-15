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
public class CalendarIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/calendar/events - Fetch aggregated events for month")
    void testGetCalendarEvents() throws Exception {
        mockMvc.perform(get("/api/v1/calendar/events")
                        .param("month", "8")
                        .param("year", "2028")
                        .param("type", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/calendar/events - Create new calendar event")
    void testCreateCalendarEvent() throws Exception {
        String eventJson = """
                {
                    "title": "Bảo dưỡng định kỳ 10,000 km - VF 8",
                    "type": "MAINTENANCE",
                    "startDate": "2028-08-22",
                    "endDate": "2028-08-22",
                    "startTime": "08:30 AM",
                    "endTime": "11:30 AM",
                    "notes": "Kiểm tra hệ thống pin cao áp và áp suất lốp",
                    "color": "#F59E0B"
                }
                """;

        mockMvc.perform(post("/api/v1/calendar/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id", startsWith("EVT-")))
                .andExpect(jsonPath("$.data.type").value("MAINTENANCE"));
    }
}
