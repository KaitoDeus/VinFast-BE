package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.common.config.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private VehicleMapper vehicleMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.oem.evwarranty.domain.user.CustomUserDetailsService customUserDetailsService;

    private Vehicle buildMockVehicle() {
        return Vehicle.builder()
                .id(1L)
                .vin("VF8E3400123456789")
                .model("VF8 Plus")
                .make("VinFast")
                .year(2024)
                .color("White")
                .mileage(15000)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .warrantyStartDate(LocalDate.of(2024, 1, 1))
                .warrantyEndDate(LocalDate.of(2027, 1, 1))
                .build();
    }

    private VehicleDTO buildMockDTO() {
        return VehicleDTO.builder()
                .id(1L)
                .vin("VF8E3400123456789")
                .model("VF8 Plus")
                .make("VinFast")
                .year(2024)
                .color("White")
                .mileage(15000)
                .status("ACTIVE")
                .warrantyEndDate(LocalDate.of(2027, 1, 1))
                .underWarranty(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles - List vehicles with pagination")
    void testListVehicles() throws Exception {
        Vehicle vehicle = buildMockVehicle();
        VehicleDTO dto = buildMockDTO();
        Page<Vehicle> page = new PageImpl<>(Collections.singletonList(vehicle), PageRequest.of(0, 10), 1);

        when(vehicleService.searchVehicles(any(), any(Pageable.class))).thenReturn(page);
        when(vehicleMapper.toDTO(any(Vehicle.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/sc/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].vin").value("VF8E3400123456789"))
                .andExpect(jsonPath("$.content[0].model").value("VF8 Plus"));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles/{id} - Get vehicle by ID")
    void testGetVehicleById() throws Exception {
        Vehicle vehicle = buildMockVehicle();
        VehicleDTO dto = buildMockDTO();

        when(vehicleService.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toDTO(any(Vehicle.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/sc/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value("VF8E3400123456789"))
                .andExpect(jsonPath("$.mileage").value(15000));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("PATCH /api/v1/sc/vehicles/{id}/mileage - Update vehicle mileage")
    void testUpdateMileage() throws Exception {
        Vehicle updated = buildMockVehicle();
        updated.setMileage(20000);
        VehicleDTO dto = buildMockDTO();
        dto.setMileage(20000);

        when(vehicleService.updateMileage(1L, 20000)).thenReturn(updated);
        when(vehicleMapper.toDTO(any(Vehicle.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/sc/vehicles/1/mileage")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mileage\": 20000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mileage").value(20000));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles/{id}/warranty-status - Check warranty status")
    void testWarrantyStatus() throws Exception {
        Vehicle vehicle = buildMockVehicle();

        when(vehicleService.findById(1L)).thenReturn(Optional.of(vehicle));

        mockMvc.perform(get("/api/v1/sc/vehicles/1/warranty-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.underWarranty").value(true))
                .andExpect(jsonPath("$.vin").value("VF8E3400123456789"));
    }

    @Test
    @WithMockUser(roles = "SC_STAFF")
    @DisplayName("GET /api/v1/sc/vehicles/{id} - Vehicle not found returns 404")
    void testGetVehicleNotFound() throws Exception {
        when(vehicleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/sc/vehicles/999"))
                .andExpect(status().isNotFound());
    }
}
