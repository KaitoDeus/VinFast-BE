package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.inventory.Inventory;


import com.oem.evwarranty.domain.analytics.PredictionResult;
import com.oem.evwarranty.domain.analytics.FailurePredictionServiceImpl;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import com.oem.evwarranty.domain.vehicle.VehiclePart;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FailurePredictionServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private WarrantyClaimRepository warrantyClaimRepository;

    @InjectMocks
    private FailurePredictionServiceImpl predictionService;

    private Vehicle targetVehicle;
    private Vehicle otherVehicle;
    private Part batteryPart;

    @BeforeEach
    void setUp() {
        targetVehicle = Vehicle.builder()
                .id(1L)
                .model("Model S")
                .mileage(60000)
                .build();

        otherVehicle = Vehicle.builder()
                .id(2L)
                .model("Model S")
                .build();

        batteryPart = Part.builder()
                .id(1L)
                .name("High Voltage Battery")
                .build();
    }

    @Test
    void testPredictFailures_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(targetVehicle));
        when(vehicleRepository.findByModel("Model S")).thenReturn(Arrays.asList(targetVehicle, otherVehicle));

        VehiclePart vp = VehiclePart.builder().part(batteryPart).build();
        WarrantyClaim claim = WarrantyClaim.builder()
                .vehicle(otherVehicle)
                .vehiclePart(vp)
                .mileageAtClaim(70000)
                .build();

        when(warrantyClaimRepository.findByVehicleId(1L)).thenReturn(List.of());
        when(warrantyClaimRepository.findByVehicleId(2L)).thenReturn(List.of(claim));

        List<PredictionResult> results = predictionService.predictFailures(1L);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getPartName().contains("High Voltage Battery"));
        assertTrue(results.get(0).getFailureProbability() >= 0.5);
    }

    @Test
    void testPredictFailures_NoHistory() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(targetVehicle));
        when(vehicleRepository.findByModel("Model S")).thenReturn(Arrays.asList(targetVehicle));
        when(warrantyClaimRepository.findByVehicleId(1L)).thenReturn(List.of());

        // Act
        List<PredictionResult> results = predictionService.predictFailures(1L);

        // Assert
        assertTrue(results.isEmpty());
    }
}
