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
public class FleetVehicleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/vehicles - List vehicles with pagination & default filters")
    void testGetVehicles_Pagination() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .param("page", "1")
                        .param("limit", "5")
                        .param("carType", "ALL")
                        .param("status", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", notNullValue()))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(5));
    }

    @Test
    @WithMockUser(roles = "FLEET_MANAGER")
    @DisplayName("POST /api/v1/vehicles - Add new vehicle to fleet & verify details")
    void testCreateVehicle_AndGetById() throws Exception {
        String uniqueVin = "VF7E" + (System.currentTimeMillis() % 10000000000000L);
        String vehicleJson = String.format("""
                {
                    "brand": "VinFast",
                    "modelName": "VF 7 Plus",
                    "carType": "CROSSOVER",
                    "licensePlate": "29B-%d",
                    "dailyPrice": 95.0,
                    "transmission": "Automatic",
                    "capacity": "5 seats",
                    "rangeKm": 450,
                    "batteryCapacity": 75.3,
                    "batteryFuelPercent": 95,
                    "topSpeedKmh": 175,
                    "accelerationSpec": "5.8s (0-100km/h)",
                    "heroImageUrl": "/cars/vf7.png",
                    "description": "VinFast VF 7 Electric Crossover"
                }
                """, System.currentTimeMillis() % 100000);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.brand").value("VinFast"))
                .andExpect(jsonPath("$.data.modelName").value("VF 7 Plus"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/vehicles/search - Search by VIN")
    void testSearchByVin() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/search")
                        .param("vin", "VF8E3400123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vin").value("VF8E3400123456789"));
    }
}
