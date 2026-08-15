package com.oem.evwarranty.domain.preorder;

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
 * REST Controller for B2C Pre-orders & Landing Page Leads.
 * Base Path: /api/v1/preorders
 * Complies with Section 4.10 of BACKEND_JAVA_SPECIFICATION.md.
 */
@RestController
@RequestMapping("/api/v1/preorders")
@Tag(name = "B2C Pre-orders & Leads", description = "Đăng ký cọc xe máy điện, lái thử và quản lý khách hàng tiềm năng")
public class PreOrderController {

    private final PreOrderService preOrderService;

    public PreOrderController(PreOrderService preOrderService) {
        this.preOrderService = preOrderService;
    }

    @PostMapping
    @Operation(summary = "Đăng ký đặt cọc / nhận tư vấn xe (Public)", description = "Khách hàng gửi thông tin đăng ký nhận tư vấn và đặt cọc xe máy điện từ Landing Page")
    public ResponseEntity<ApiResponse<PreOrderDTO.Response>> createPreOrder(
            @Valid @RequestBody PreOrderDTO.CreateRequest request) {

        PreOrderDTO.Response created = preOrderService.createPreOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đơn đặt mua xe đã được ghi nhận. Tài khoản và mật khẩu tạm thời đã được gửi về Email và Số điện thoại của bạn.", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Danh sách đơn đặt cọc phân trang", description = "Quản lý và tra cứu khách hàng đăng ký mua xe máy điện theo trạng thái và từ khóa")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {

        int pageIndex = Math.max(0, page - 1);
        Page<PreOrderDTO.Response> preOrderPage = preOrderService.getPreOrders(
                query, status, PageRequest.of(pageIndex, limit, Sort.by("id").descending()));

        Map<String, Object> data = Map.of(
                "items", preOrderPage.getContent(),
                "pagination", ApiPagination.fromPage(preOrderPage)
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Chi tiết đơn đặt cọc", description = "Xem chi tiết thông tin đơn đặt hàng, mẫu xe, màu sắc và yêu cầu khách hàng")
    public ResponseEntity<ApiResponse<PreOrderDTO.Response>> getPreOrderById(@PathVariable Long id) {
        return preOrderService.getPreOrderById(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy đơn đặt hàng với ID: " + id, "PREORDER_NOT_FOUND")));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật trạng thái chăm sóc khách hàng", description = "Chuyển trạng thái xử lý: PENDING, CONTACTED, CONFIRMED, CANCELED")
    @Auditable(action = "UPDATE_PREORDER_STATUS", resourceType = "PRE_ORDER")
    public ResponseEntity<ApiResponse<PreOrderDTO.Response>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PreOrderDTO.UpdateStatusRequest request) {

        PreOrderDTO.Response updated = preOrderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái xử lý thành công", updated));
    }
}
