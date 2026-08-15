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
public class TelemetryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "DISPATCHER")
    @DisplayName("GET /api/v1/tracking/vehicles - Live fleet GPS coordinates and telemetry snapshot")
    void testGetLiveVehicles() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/tracking/charging-stations - V-GREEN Charging Stations list")
    void testGetChargingStations() throws Exception {
        mockMvc.perform(get("/api/v1/tracking/charging-stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].power", containsString("kW")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/tracking/telemetry - Ingest OBD-II telemetry and broadcast to STOMP WebSocket")
    void testIngestTelemetry() throws Exception {
        String telemetryJson = """
                {
                    "vehicleId": 1,
                    "latitude": 10.795100,
                    "longitude": 106.721800,
                    "speedKmh": 55.0,
                    "heading": 90.0,
                    "batteryPercent": 85,
                    "motorTemperature": 42.5,
                    "engineStatus": "ON",
                    "statusText": "In Transit"
                }
                """;

        mockMvc.perform(post("/api/v1/tracking/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(telemetryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.battery").value("85%"))
                .andExpect(jsonPath("$.data.speed").value("55 km/h"));
    }
}
