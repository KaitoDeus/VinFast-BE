package com.oem.evwarranty.domain.telemetry;

import com.oem.evwarranty.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for GPS Tracking & Telemetry IoT.
 * Base Path: /api/v1/tracking
 */
@RestController
@RequestMapping("/api/v1/tracking")
@Tag(name = "GPS Tracking & Telemetry IoT", description = "Định vị GPS thời gian thực, trạm sạc V-GREEN và ingest dữ liệu IoT")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/vehicles")
    @Operation(summary = "Vị trí GPS & trạng thái xe thời gian thực", description = "Lấy snapshot tọa độ GPS, tốc độ, dung lượng pin và trạng thái của toàn bộ đội xe")
    public ResponseEntity<ApiResponse<List<TelemetryDTO.LiveVehicleTrackingDTO>>> getLiveVehicles() {
        List<TelemetryDTO.LiveVehicleTrackingDTO> vehicles = telemetryService.getLiveFleetTelemetry();
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/charging-stations")
    @Operation(summary = "Danh sách trạm sạc V-GREEN", description = "Lấy danh sách các trạm sạc với vị trí, công suất và số cổng khả dụng")
    public ResponseEntity<ApiResponse<List<TelemetryDTO.ChargingStationDTO>>> getChargingStations() {
        List<TelemetryDTO.ChargingStationDTO> stations = telemetryService.getChargingStations();
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    @PostMapping("/telemetry")
    @Operation(summary = "Ingest gói tin IoT từ xe", description = "Ghi nhận dữ liệu định vị, cảm biến nhiệt độ & pin và phát sóng qua WebSocket /topic/telemetry/fleet")
    public ResponseEntity<ApiResponse<TelemetryDTO.LiveVehicleTrackingDTO>> ingestTelemetry(
            @Valid @RequestBody TelemetryDTO.TelemetryIngestRequest request) {

        TelemetryDTO.LiveVehicleTrackingDTO result = telemetryService.ingestTelemetry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dữ liệu IoT đã được ghi nhận và phát sóng thành công", result));
    }
}
