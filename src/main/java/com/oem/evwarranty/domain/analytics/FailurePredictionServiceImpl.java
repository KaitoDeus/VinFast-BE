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
                    anomalyNote = " (SÃƒÂ¡Ã‚ÂºÃ‚Â¯p Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â¿n ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â¡ng hÃƒÂ¡Ã‚Â»Ã‚Âng hÃƒÆ’Ã‚Â³c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh kÃƒÂ¡Ã‚Â»Ã‚Â³)";
                }

                long specificVehicleFailures = targetVehicle.getWarrantyClaims().stream()
                        .filter(c -> c.getVehiclePart() != null
                                && c.getVehiclePart().getPart().getName().equals(partName))
                        .count();

                if (specificVehicleFailures > 0) {
                    riskMultiplier *= 2.0;
                    anomalyNote = " (PhÃƒÆ’Ã‚Â¡t hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i lÃƒÂ¡Ã‚ÂºÃ‚Â·p lÃƒÂ¡Ã‚ÂºÃ‚Â¡i)";
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
            return "NGUY CÃƒÂ¡Ã‚ÂºÃ‚Â¤P: " + partName + " cÃƒÆ’Ã‚Â³ dÃƒÂ¡Ã‚ÂºÃ‚Â¥u hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u hÃƒÂ¡Ã‚Â»Ã‚Âng hÃƒÆ’Ã‚Â³c bÃƒÂ¡Ã‚ÂºÃ‚Â¥t thÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âng. YÃƒÆ’Ã‚Âªu cÃƒÂ¡Ã‚ÂºÃ‚Â§u kiÃƒÂ¡Ã‚Â»Ã†â€™m tra chÃƒÂ¡Ã‚ÂºÃ‚Â©n Ãƒâ€žÃ¢â‚¬ËœoÃƒÆ’Ã‚Â¡n ngay lÃƒÂ¡Ã‚ÂºÃ‚Â­p tÃƒÂ¡Ã‚Â»Ã‚Â©c.";
        } else if (probability > 0.35) {
            return "CÃƒÂ¡Ã‚ÂºÃ‚Â¢NH BÃƒÆ’Ã‚ÂO: XÃƒÆ’Ã‚Â¡c suÃƒÂ¡Ã‚ÂºÃ‚Â¥t hÃƒÂ¡Ã‚Â»Ã‚Âng " + partName + " cao dÃƒÂ¡Ã‚Â»Ã‚Â±a trÃƒÆ’Ã‚Âªn lÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ch sÃƒÂ¡Ã‚Â»Ã‚Â­ dÃƒÆ’Ã‚Â²ng xe. CÃƒÂ¡Ã‚ÂºÃ‚Â§n kiÃƒÂ¡Ã‚Â»Ã†â€™m tra trong lÃƒÂ¡Ã‚ÂºÃ‚Â§n bÃƒÂ¡Ã‚ÂºÃ‚Â£o dÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â¡ng tÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi.";
        } else if (probability > 0.15) {
            return "KHUYÃƒÂ¡Ã‚ÂºÃ‚Â¾N NGHÃƒÂ¡Ã‚Â»Ã…Â : Theo dÃƒÆ’Ã‚Âµi hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u suÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚Â»Ã‚Â§a " + partName + ". DÃƒÂ¡Ã‚Â»Ã‚Â¯ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u cho thÃƒÂ¡Ã‚ÂºÃ‚Â¥y cÃƒÆ’Ã‚Â³ dÃƒÂ¡Ã‚ÂºÃ‚Â¥u hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u hao mÃƒÆ’Ã‚Â²n trung bÃƒÆ’Ã‚Â¬nh.";
        } else {
            return "KhuyÃƒÂ¡Ã‚ÂºÃ‚Â¿n nghÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ giÃƒÆ’Ã‚Â¡m sÃƒÆ’Ã‚Â¡t Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh kÃƒÂ¡Ã‚Â»Ã‚Â³ cho " + partName + ".";
        }
    }
}



