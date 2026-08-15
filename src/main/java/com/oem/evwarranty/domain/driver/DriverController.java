package com.oem.evwarranty.domain.driver;

import com.oem.evwarranty.common.annotation.Auditable;
import com.oem.evwarranty.common.dto.ApiPagination;
import com.oem.evwarranty.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Drivers Management.
 * Base Path: /api/v1/drivers
 */
@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Drivers Management", description = "Quản lý tài xế, phân bổ xe và theo dõi trạng thái ca trực")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Danh sách tài xế phân trang & bộ lọc", description = "Lấy danh sách tài xế theo trạng thái ca trực (ON_DUTY, OFF_DUTY, IN_TRANSIT) và từ khóa tìm kiếm")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDrivers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String dutyStatus) {

        int pageIndex = Math.max(0, page - 1);
        Page<DriverDTO.DriverResponse> driverPage = driverService.getDrivers(
                query, dutyStatus, PageRequest.of(pageIndex, limit, Sort.by("id").descending()));

        Map<String, Object> data = Map.of(
                "items", driverPage.getContent(),
                "pagination", ApiPagination.fromPage(driverPage)
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Thêm tài xế mới vào đội xe", description = "Đăng ký hồ sơ tài xế, GPLX và gán xe bàn giao")
    @Auditable(action = "CREATE_DRIVER", resourceType = "DRIVER")
    public ResponseEntity<ApiResponse<DriverDTO.DriverResponse>> createDriver(
            @Valid @RequestBody DriverDTO.CreateDriverRequest request) {

        DriverDTO.DriverResponse created = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm tài xế mới thành công", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Chi tiết hồ sơ tài xế", description = "Lấy thông tin đánh giá sao, số giờ làm việc trong tháng và xe đang gán")
    public ResponseEntity<ApiResponse<DriverDTO.DriverResponse>> getDriverById(@PathVariable Long id) {
        return driverService.getDriverById(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy tài xế với ID: " + id, "DRIVER_NOT_FOUND")));
    }

    @PatchMapping("/{id}/duty-status")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật trạng thái ca trực", description = "Chuyển trạng thái trực của tài xế: ON_DUTY, OFF_DUTY, IN_TRANSIT, ON_LEAVE")
    @Auditable(action = "UPDATE_DRIVER_DUTY_STATUS", resourceType = "DRIVER")
    public ResponseEntity<ApiResponse<DriverDTO.DriverResponse>> updateDutyStatus(
            @PathVariable Long id,
            @Valid @RequestBody DriverDTO.UpdateDutyStatusRequest request) {

        DriverDTO.DriverResponse updated = driverService.updateDutyStatus(id, request.getDutyStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái trực thành công", updated));
    }
}
