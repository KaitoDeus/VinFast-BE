package com.oem.evwarranty.domain.vehicle;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for VinFast Fleet & Units Management.
 * Base Path: /api/v1/vehicles
 * Complies 100% with BACKEND_JAVA_SPECIFICATION.md.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Fleet & Vehicles Management", description = "Quản lý danh sách xe điện, thông số kỹ thuật và trạng thái kho")
public class VehicleRestController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public VehicleRestController(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    @GetMapping
    @Operation(summary = "Danh sách xe phân trang & bộ lọc", description = "Lấy danh sách xe điện theo trang, từ khóa tìm kiếm, loại xe và trạng thái")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVehicles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "ALL") String carType,
            @RequestParam(required = false, defaultValue = "ALL") String status) {

        int pageIndex = Math.max(0, page - 1);
        Page<Vehicle> vehiclePage = vehicleService.findVehiclesWithFilters(
                query, carType, status,
                PageRequest.of(pageIndex, limit, Sort.by("id").ascending()));

        List<VehicleDTO> items = vehiclePage.getContent().stream()
                .map(vehicleMapper::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> responseData = Map.of(
                "items", items,
                "pagination", ApiPagination.fromPage(vehiclePage)
        );

        return ResponseEntity.ok(ApiResponse.success(responseData));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Thêm xe mới vào kho", description = "Thêm mẫu xe điện mới với thông số kỹ thuật hoàn chỉnh")
    @Auditable(action = "CREATE_VEHICLE", resourceType = "VEHICLE")
    public ResponseEntity<ApiResponse<VehicleDTO>> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        Vehicle created = vehicleService.createVehicle(vehicle, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm xe mới thành công", vehicleMapper.toDTO(created)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết xe theo ID", description = "Lấy đầy đủ thông số kỹ thuật (Specs), Gallery ảnh và lịch sử xe")
    public ResponseEntity<ApiResponse<VehicleDTO>> getVehicleById(@PathVariable Long id) {
        return vehicleService.findById(id)
                .map(vehicleMapper::toDTO)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy xe với ID: " + id, "VEHICLE_NOT_FOUND")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Cập nhật thông tin xe", description = "Cập nhật giá thuê, trạng thái hoặc thông số kỹ thuật của xe")
    @Auditable(action = "UPDATE_VEHICLE", resourceType = "VEHICLE")
    public ResponseEntity<ApiResponse<VehicleDTO>> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        Vehicle updated = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin xe thành công", vehicleMapper.toDTO(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa xe khỏi kho", description = "Xóa thông tin xe điện khỏi cơ sở dữ liệu")
    @Auditable(action = "DELETE_VEHICLE", resourceType = "VEHICLE")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa xe thành công", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm xe theo VIN", description = "Tra cứu xe điện bằng số VIN duy nhất")
    public ResponseEntity<ApiResponse<VehicleDTO>> searchByVin(@RequestParam String vin) {
        return vehicleService.findByVin(vin)
                .map(vehicleMapper::toDTO)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy xe với VIN: " + vin, "VEHICLE_NOT_FOUND")));
    }

    @GetMapping("/{id}/parts")
    @Operation(summary = "Lấy linh kiện gắn trên xe")
    public ResponseEntity<ApiResponse<List<VehiclePartDTO>>> getVehicleParts(@PathVariable Long id) {
        List<VehiclePart> parts = vehicleService.findPartsByVehicleId(id);
        List<VehiclePartDTO> dtos = parts.stream()
                .map(vehicleMapper::toPartDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
