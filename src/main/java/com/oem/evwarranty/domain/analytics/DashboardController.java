package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.user.Role;


import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.analytics.ReportService;
import com.oem.evwarranty.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Controller for Dashboard pages based on user role.
 */
@Controller
@Tag(name = "Dashboard", description = "Operations for viewing system analytics and personal dashboards")
public class DashboardController {

    private final ReportService reportService;
    private final UserService userService;

    public DashboardController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "View dashboard", description = "Main landing page with statistics tailored to user role")
    public String dashboard(Model model, Authentication auth) {
        Map<String, Object> stats = reportService.getDashboardStats();
        model.addAttribute("stats", stats);

        User user = userService.findByUsername(auth.getName()).orElse(null);
        model.addAttribute("currentUser", user);

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            populateGlobalStats(model);
            return "dashboard/admin";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EVM_STAFF"))) {
            populateGlobalStats(model);
            return "dashboard/evm";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SC_STAFF"))) {
            if (user != null && user.getServiceCenter() != null) {
                Map<String, Object> scStats = reportService.getServiceCenterStats(user.getServiceCenter());
                stats.putAll(scStats);
                model.addAttribute("claimStats", scStats.get("claimStats"));
                model.addAttribute("vehicleStats", scStats.get("vehicleStats"));
                model.addAttribute("campaignStats", scStats.get("campaignStats"));
            } else {
                populateGlobalStats(model);
            }
            return "dashboard/sc-staff";
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SC_TECHNICIAN"))) {
            return "dashboard/sc-technician";
        }

        return "dashboard/default";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    @Operation(summary = "Dashboard statistics API", description = "JSON endpoint to get dashboard numbers for AJAX components")
    public Map<String, Object> getStatsApi() {
        return reportService.getDashboardStats();
    }

    private void populateGlobalStats(Model model) {
        model.addAttribute("claimStats", reportService.getClaimStatsByStatus());
        model.addAttribute("campaignStats", reportService.getCampaignStatsByStatus());
        model.addAttribute("vehicleStats", reportService.getVehicleStatsByStatus());
    }
}



