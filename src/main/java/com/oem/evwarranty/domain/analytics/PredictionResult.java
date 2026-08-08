package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.inventory.Part;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to represent the prediction result for a specific vehicle part.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResult {
    private String partName;
    private Double failureProbability;
    private RiskLevel riskLevel;
    private String recommendedAction;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}


