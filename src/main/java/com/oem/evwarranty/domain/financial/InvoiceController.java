package com.oem.evwarranty.domain.financial;

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
 * REST Controller for Invoices Management.
 * Base Path: /api/v1/invoices
 */
@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices Management", description = "Quản lý hóa đơn điện tử, trạng thái thanh toán và cổng thanh toán VNPay/MoMo")
public class InvoiceController {

    private final FinancialService financialService;

    public InvoiceController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    @Operation(summary = "Danh sách hóa đơn", description = "Lấy danh sách hóa đơn phân trang và lọc theo trạng thái: COMPLETED, PENDING, OVERDUE")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInvoices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "ALL") String status) {

        int pageIndex = Math.max(0, page - 1);
        Page<FinancialDTO.InvoiceResponse> invoicePage = financialService.getInvoices(
                status, PageRequest.of(pageIndex, limit, Sort.by("id").descending()));

        Map<String, Object> data = Map.of(
                "items", invoicePage.getContent(),
                "pagination", ApiPagination.fromPage(invoicePage)
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN', 'DISPATCHER')")
    @Operation(summary = "Tạo hóa đơn mới", description = "Lập hóa đơn thanh toán cho hợp đồng thuê xe")
    @Auditable(action = "CREATE_INVOICE", resourceType = "INVOICE")
    public ResponseEntity<ApiResponse<FinancialDTO.InvoiceResponse>> createInvoice(
            @Valid @RequestBody FinancialDTO.CreateInvoiceRequest request) {

        FinancialDTO.InvoiceResponse created = financialService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lập hóa đơn mới thành công", created));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xác nhận thanh toán hóa đơn", description = "Đánh dấu hóa đơn đã thanh toán thành công (COMPLETED)")
    @Auditable(action = "PAY_INVOICE", resourceType = "INVOICE")
    public ResponseEntity<ApiResponse<FinancialDTO.InvoiceResponse>> payInvoice(@PathVariable Long id) {
        FinancialDTO.InvoiceResponse paid = financialService.markInvoicePaid(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận thanh toán thành công", paid));
    }
}
