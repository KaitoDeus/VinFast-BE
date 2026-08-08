package com.oem.evwarranty.domain.analytics;


import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.analytics.PredictionResult;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import com.oem.evwarranty.domain.analytics.FailurePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FailurePredictionServiceImpl implements FailurePredictionService {

    private final VehicleRepository vehicleRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;

    @Override
    public List<PredictionResult> predictFailures(Long vehicleId) {
        Vehicle targetVehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));

        List<Vehicle> similarVehicles = vehicleRepository.findByModel(targetVehicle.getModel());

        if (similarVehicles.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarrantyClaim> historicalClaims = similarVehicles.stream()
                .flatMap(v -> warrantyClaimRepository.findByVehicleId(v.getId()).stream())
                .collect(Collectors.toList());

        Map<String, List<WarrantyClaim>> claimsByPart = historicalClaims.stream()
                .filter(wc -> wc.getVehiclePart() != null && wc.getVehiclePart().getPart() != null)
                .collect(Collectors.groupingBy(wc -> wc.getVehiclePart().getPart().getName()));

        double fleetSize = similarVehicles.size();
        List<PredictionResult> predictions = new ArrayList<>();

        claimsByPart.forEach((partName, claims) -> {
            long count = claims.size();
            double avgMileageAtFailure = claims.stream()
                    .filter(c -> c.getMileageAtClaim() != null)
                    .mapToInt(WarrantyClaim::getMileageAtClaim)
                    .average()
                    .orElse(0.0);

            double baseProbability = (double) count / fleetSize;
            double riskMultiplier = 1.0;
            String anomalyNote = "";

            if (targetVehicle.getMileage() != null) {
                if (targetVehicle.getMileage() > avgMileageAtFailure * 0.8
                        && targetVehicle.getMileage() < avgMileageAtFailure) {
                    riskMultiplier *= 1.4;
                    anomalyNote = " (SÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¯p ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿n ngÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¡ng hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âng hÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³c ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹nh kÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â³)";
                }

                long specificVehicleFailures = targetVehicle.getWarrantyClaims().stream()
                        .filter(c -> c.getVehiclePart() != null
                                && c.getVehiclePart().getPart().getName().equals(partName))
                        .count();

                if (specificVehicleFailures > 0) {
                    riskMultiplier *= 2.0;
                    anomalyNote = " (PhÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡t hiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡n lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Âi lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â·p lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡i)";
                }
            }

            double finalProbability = Math.min(0.99, baseProbability * riskMultiplier);

            if (finalProbability > 0.05) {
                predictions.add(PredictionResult.builder()
                        .partName(partName + anomalyNote)
                        .failureProbability(finalProbability)
                        .riskLevel(determineRiskLevel(finalProbability))
                        .recommendedAction(generateRecommendation(partName, finalProbability))
                        .build());
            }
        });

        predictions.sort(Comparator.comparing(PredictionResult::getFailureProbability).reversed());
        return predictions;
    }

    private PredictionResult.RiskLevel determineRiskLevel(double probability) {
        if (probability > 0.6) return PredictionResult.RiskLevel.CRITICAL;
        if (probability > 0.35) return PredictionResult.RiskLevel.HIGH;
        if (probability > 0.15) return PredictionResult.RiskLevel.MEDIUM;
        return PredictionResult.RiskLevel.LOW;
    }

    private String generateRecommendation(String partName, double probability) {
        if (probability > 0.6) {
            return "NGUY CÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¤P: " + partName + " cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥u hiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡u hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âng hÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³c bÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t thÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âng. YÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªu cÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â§u kiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€ Ã¢â‚¬â„¢m tra chÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â©n ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“oÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡n ngay lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â­p tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â©c.";
        } else if (probability > 0.35) {
            return "CÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¢NH BÃƒÆ’Ã†â€™Ãƒâ€šÃ‚ÂO: XÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡c suÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t hÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Âng " + partName + " cao dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â±a trÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªn lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ch sÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â­ dÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â²ng xe. CÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â§n kiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€ Ã¢â‚¬â„¢m tra trong lÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â§n bÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£o dÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¡ng tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Âºi.";
        } else if (probability > 0.15) {
            return "KHUYÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¾N NGHÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€¦Ã‚Â : Theo dÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµi hiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡u suÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥t cÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â§a " + partName + ". DÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â¯ liÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡u cho thÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥y cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ dÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¥u hiÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¡u hao mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â²n trung bÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¬nh.";
        } else {
            return "KhuyÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¿n nghÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ giÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡m sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡t ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹nh kÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â³ cho " + partName + ".";
        }
    }
}



