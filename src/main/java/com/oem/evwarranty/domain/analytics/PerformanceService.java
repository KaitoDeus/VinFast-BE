package com.oem.evwarranty.domain.analytics;


import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Performance Service for tracking technician and service center metrics.
 */
@Service
public class PerformanceService {

    private final WarrantyClaimRepository claimRepository;

    public PerformanceService(WarrantyClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    /**
     * Calculates efficiency of technicians based on labor hours and completion.
     */
    public Map<String, Object> getTechnicianPerformance() {
        List<WarrantyClaim> completedClaims = claimRepository.findByStatus(WarrantyClaim.ClaimStatus.COMPLETED);

        return completedClaims.stream()
                .filter(c -> c.getTechnician() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getTechnician().getFullName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> techStats = new HashMap<>();
                                    techStats.put("completedClaims", list.size());
                                    techStats.put("totalLaborHours", list.stream()
                                            .mapToDouble(c -> c.getLaborHours() != null ? c.getLaborHours() : 0.0)
                                            .sum());
                                    techStats.put("avgHoursPerClaim", list.stream()
                                            .mapToDouble(c -> c.getLaborHours() != null ? c.getLaborHours() : 0.0)
                                            .average().orElse(0.0));
                                    return techStats;
                                })));
    }

    /**
     * Gets Service Center turnaround time stats.
     */
    public Map<String, Double> getServiceCenterTurnaroundTime() {
        List<WarrantyClaim> completedClaims = claimRepository.findByStatus(WarrantyClaim.ClaimStatus.COMPLETED);

        return completedClaims.stream()
                .filter(c -> c.getServiceCenter() != null && c.getSubmittedAt() != null && c.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        WarrantyClaim::getServiceCenter,
                        Collectors.averagingDouble(c -> {
                            long hours = java.time.Duration.between(c.getSubmittedAt(), c.getCompletedAt()).toHours();
                            return (double) hours;
                        })));
    }
}


