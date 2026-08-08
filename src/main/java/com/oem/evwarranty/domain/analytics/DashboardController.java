package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Dashboard statistics (/api/v1/dashboard).
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard REST API", description = "Operations for retrieving system analytics and dashboard statistics")
public class DashboardController {

    private final ReportService reportService;
    private final UserService userService;

    public DashboardController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get dashboard analytics", description = "Retrieve statistics tailored to the current user role")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        result.put("stats", reportService.getDashboardStats());

        if (auth != null) {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            result.put("currentUser", user);

            if (user != null && user.getServiceCenter() != null) {
                Map<String, Object> scStats = reportService.getServiceCenterStats(user.getServiceCenter());
                result.putAll(scStats);
            } else {
                result.put("claimStats", reportService.getClaimStatsByStatus());
                result.put("campaignStats", reportService.getCampaignStatsByStatus());
                result.put("vehicleStats", reportService.getVehicleStatsByStatus());
            }
        }

        return ResponseEntity.ok(result);
    }
}
