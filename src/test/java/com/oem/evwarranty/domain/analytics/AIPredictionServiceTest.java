package com.oem.evwarranty.domain.analytics;



import com.oem.evwarranty.domain.analytics.AIPredictionService;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AIPredictionServiceTest {

    @Mock
    private WarrantyClaimRepository claimRepository;

    @InjectMocks
    private AIPredictionService aiPredictionService;

    @Test
    void analyzeRootCauses_Success() {
        WarrantyClaim claim1 = new WarrantyClaim();
        claim1.setDiagnosisNotes("The BATTERY is dead.");

        WarrantyClaim claim2 = new WarrantyClaim();
        claim2.setFailureDescription("MOTOR failure detected.");

        when(claimRepository.findByStatus(WarrantyClaim.ClaimStatus.COMPLETED))
                .thenReturn(Arrays.asList(claim1, claim2));

        Map<String, Double> results = aiPredictionService.analyzeRootCauses();

        assertNotNull(results);
        assertTrue(results.containsKey("BATTERY"));
        assertTrue(results.containsKey("MOTOR"));
        assertEquals(50.0, results.get("BATTERY"));
    }

    @Test
    void forecastWarrantyCosts_WithData_Success() {
        WarrantyClaim claim = new WarrantyClaim();
        claim.setTotalCost(new BigDecimal("1000.00"));

        when(claimRepository.findAll()).thenReturn(Arrays.asList(claim));

        Map<String, BigDecimal> forecast = aiPredictionService.forecastWarrantyCosts();

        assertNotNull(forecast);
        assertEquals(6, forecast.size());
        assertTrue(forecast.containsKey("Month +1"));
    }

    @Test
    void calculateModelRiskScore_Success() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel("Model 3");

        WarrantyClaim claim = new WarrantyClaim();
        claim.setVehicle(vehicle);

        when(claimRepository.findAll()).thenReturn(Arrays.asList(claim));

        double score = aiPredictionService.calculateModelRiskScore("Model 3");

        // failureRate = 1 / 1000 = 0.001
        // score = 0.001 * 100 * 2.5 = 0.25
        assertEquals(0.25, score);
    }
}
