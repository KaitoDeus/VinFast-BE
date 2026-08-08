package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.vehicle.Vehicle;


import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI Prediction Service for Warranty Analysis and Forecasting.
 * Simulates AI-driven analysis of failure rates and cost projections.
 */
@Service
public class AIPredictionService {

    private final WarrantyClaimRepository claimRepository;

    public AIPredictionService(WarrantyClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    /**
     * Predicts common failure causes based on diagnosis notes.
     */
    public Map<String, Double> analyzeRootCauses() {
        List<WarrantyClaim> completedClaims = claimRepository.findByStatus(WarrantyClaim.ClaimStatus.COMPLETED);
        Map<String, Integer> keywordCounts = new HashMap<>();
        String[] keywords = { "BATTERY", "MOTOR", "SOFTWARE", "BMS", "CHARGER", "WIRING", "SENSOR" };

        for (WarrantyClaim claim : completedClaims) {
            String notes = (claim.getDiagnosisNotes() != null ? claim.getDiagnosisNotes().toUpperCase() : "") +
                    (claim.getFailureDescription() != null ? claim.getFailureDescription().toUpperCase() : "");

            for (String kw : keywords) {
                if (notes.contains(kw)) {
                    keywordCounts.put(kw, keywordCounts.getOrDefault(kw, 0) + 1);
                }
            }
        }

        int totalMatches = keywordCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalMatches == 0) {
            return new HashMap<>();
        }

        return keywordCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (double) e.getValue() / totalMatches * 100.0));
    }

    /**
     * Forecasts warranty costs for the next 6 months.
     */
    public Map<String, BigDecimal> forecastWarrantyCosts() {
        List<WarrantyClaim> recentClaims = claimRepository.findAll();
        BigDecimal currentAvg = recentClaims.stream()
                .filter(c -> c.getTotalCost() != null)
                .map(WarrantyClaim::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (recentClaims.isEmpty()) {
            return new HashMap<>();
        }

        BigDecimal avgPerClaim = currentAvg.divide(new BigDecimal(recentClaims.size()), 2, RoundingMode.HALF_UP);
        Map<String, BigDecimal> forecast = new HashMap<>();
        double growthRate = 1.05;

        BigDecimal baseClaims = new BigDecimal(recentClaims.size());
        for (int i = 1; i <= 6; i++) {
            BigDecimal predictedCount = baseClaims.multiply(new BigDecimal(Math.pow(growthRate, i)));
            BigDecimal predictedCost = predictedCount.multiply(avgPerClaim).setScale(2, RoundingMode.HALF_UP);
            forecast.put("Month +" + i, predictedCost);
        }

        return forecast;
    }

    /**
     * Calculates the Risk Score for a specific vehicle model.
     */
    public double calculateModelRiskScore(String model) {
        long totalVehiclesOfModel = 1000;
        long totalClaimsOfModel = claimRepository.findAll().stream()
                .filter(c -> c.getVehicle() != null && model.equals(c.getVehicle().getModel()))
                .count();

        if (totalVehiclesOfModel == 0) {
            return 0.0;
        }

        double failureRate = (double) totalClaimsOfModel / totalVehiclesOfModel;
        return Math.min(failureRate * 100.0 * 2.5, 100.0);
    }
}


