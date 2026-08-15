package com.oem.evwarranty.domain.client;

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
 * REST Controller for Clients Management.
 * Base Path: /api/v1/clients
 */
@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients Management", description = "Quản lý hồ sơ khách hàng, lịch sử thuê xe và điểm thưởng")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Danh sách khách hàng phân trang", description = "Tìm kiếm và lọc danh sách khách hàng theo tên, email, số điện thoại")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String query) {

        int pageIndex = Math.max(0, page - 1);
        Page<ClientDTO.ClientResponse> clientPage = clientService.getClients(
                query, PageRequest.of(pageIndex, limit, Sort.by("id").descending()));

        Map<String, Object> data = Map.of(
                "items", clientPage.getContent(),
                "pagination", ApiPagination.fromPage(clientPage)
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Thêm khách hàng mới", description = "Tạo hồ sơ khách hàng mới cùng tài khoản xác thực")
    @Auditable(action = "CREATE_CLIENT", resourceType = "CLIENT")
    public ResponseEntity<ApiResponse<ClientDTO.ClientResponse>> createClient(
            @Valid @RequestBody ClientDTO.CreateClientRequest request) {

        ClientDTO.ClientResponse created = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khách hàng mới thành công", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Chi tiết hồ sơ khách hàng", description = "Lấy thông tin định danh, CCCD, GPLX và tổng chi tiêu của khách hàng")
    public ResponseEntity<ApiResponse<ClientDTO.ClientResponse>> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy khách hàng với ID: " + id, "CLIENT_NOT_FOUND")));
    }
}
