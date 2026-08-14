package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.common.config.JwtTokenProvider;
import com.oem.evwarranty.domain.inventory.Part;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiclePartRestController.class)
@Import(VehicleMapper.class)
public class VehiclePartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.oem.evwarranty.domain.user.CustomUserDetailsService customUserDetailsService;

    private VehiclePart buildMockVehiclePart() {
        Part part = Part.builder()
                .id(1L)
                .name("Battery Module BMS v3")
                .partNumber("BAT-BMS-V3")
                .build();

        return VehiclePart.builder()
                .id(10L)
                .serialNumber("SN-BAT-2024-001")
                .part(part)
                .warrantyStartDate(LocalDate.of(2024, 6, 1))
                .warrantyEndDate(LocalDate.of(2025, 6, 1))
                .status(VehiclePart.PartStatus.ACTIVE)
                .notes("Installed during scheduled maintenance")
                .build();
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles/{vehicleId}/parts - List installed parts")
    void testListInstalledParts() throws Exception {
        VehiclePart vp = buildMockVehiclePart();

        when(vehicleService.findPartsByVehicleId(1L)).thenReturn(List.of(vp));

        mockMvc.perform(get("/api/v1/sc/vehicles/1/parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serialNumber").value("SN-BAT-2024-001"))
                .andExpect(jsonPath("$[0].partName").value("Battery Module BMS v3"))
                .andExpect(jsonPath("$[0].partCode").value("BAT-BMS-V3"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("POST /api/v1/sc/vehicles/{vehicleId}/parts - Install part on vehicle")
    void testInstallPart() throws Exception {
        VehiclePart vp = buildMockVehiclePart();

        when(vehicleService.installPart(eq(1L), eq(1L), eq("SN-NEW-001"), anyString(), any()))
                .thenReturn(vp);

        String requestBody = """
                {
                    "partId": 1,
                    "serialNumber": "SN-NEW-001",
                    "notes": "New battery module installed"
                }
                """;

        mockMvc.perform(post("/api/v1/sc/vehicles/1/parts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SN-BAT-2024-001"))
                .andExpect(jsonPath("$.partName").value("Battery Module BMS v3"));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("DELETE /api/v1/sc/vehicles/{vehicleId}/parts/{id} - Remove part (mark as REPLACED)")
    void testRemovePart() throws Exception {
        VehiclePart removed = buildMockVehiclePart();
        removed.setStatus(VehiclePart.PartStatus.REPLACED);

        when(vehicleService.removePart(10L)).thenReturn(removed);

        mockMvc.perform(delete("/api/v1/sc/vehicles/1/parts/10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Part marked as REPLACED successfully"))
                .andExpect(jsonPath("$.partStatus").value("REPLACED"));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles/{vehicleId}/parts/{id} - Get installed part details")
    void testGetPartDetails() throws Exception {
        VehiclePart vp = buildMockVehiclePart();

        when(vehicleService.findPartsByVehicleId(1L)).thenReturn(List.of(vp));

        mockMvc.perform(get("/api/v1/sc/vehicles/1/parts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("SN-BAT-2024-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles/{vehicleId}/parts/{id} - Part not found returns 404")
    void testGetPartDetailsNotFound() throws Exception {
        when(vehicleService.findPartsByVehicleId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/sc/vehicles/1/parts/999"))
                .andExpect(status().isNotFound());
    }
}
