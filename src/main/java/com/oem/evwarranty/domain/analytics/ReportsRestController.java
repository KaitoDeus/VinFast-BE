package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.user.UserService;


import com.oem.evwarranty.domain.analytics.AIPredictionService;
import com.oem.evwarranty.domain.analytics.PerformanceService;
import com.oem.evwarranty.domain.analytics.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports API", description = "Endpoints for dashboard statistics and analytics")
public class ReportsRestController {

    private final ReportService reportService;
    private final AIPredictionService aiService;
    private final PerformanceService performanceService;
    private final UserService userService;

    public ReportsRestController(ReportService reportService,
            AIPredictionService aiService,
            PerformanceService performanceService,
            UserService userService) {
        this.reportService = reportService;
        this.aiService = aiService;
        this.performanceService = performanceService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get overall dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @GetMapping("/claims/status")
    @Operation(summary = "Get claim count breakdown by status")
    public ResponseEntity<Map<String, Long>> getClaimStats() {
        return ResponseEntity.ok(reportService.getClaimStatsByStatus());
    }

    @GetMapping("/ai/root-causes")
    @Operation(summary = "AI analysis of root causes for failures")
    public ResponseEntity<Map<String, Double>> getRootCauses() {
        return ResponseEntity.ok(aiService.analyzeRootCauses());
    }

    @GetMapping("/ai/forecast-costs")
    @Operation(summary = "AI forecast of warranty costs for next 6 months")
    public ResponseEntity<Map<String, BigDecimal>> getCostForecast() {
        return ResponseEntity.ok(aiService.forecastWarrantyCosts());
    }

    @GetMapping("/performance/technicians")
    @Operation(summary = "Technician efficiency and performance metrics")
    public ResponseEntity<Map<String, Object>> getTechPerformance() {
        return ResponseEntity.ok(performanceService.getTechnicianPerformance());
    }

    @GetMapping("/performance/turnaround")
    @Operation(summary = "Service Center turnaround time average (in hours)")
    public ResponseEntity<Map<String, Double>> getTurnaroundTime() {
        return ResponseEntity.ok(performanceService.getServiceCenterTurnaroundTime());
    }

    @GetMapping("/ai/risk-score")
    @Operation(summary = "Calculate risk score for a vehicle model")
    public ResponseEntity<Double> getModelRisk(@RequestParam String model) {
        return ResponseEntity.ok(aiService.calculateModelRiskScore(model));
    }
}


