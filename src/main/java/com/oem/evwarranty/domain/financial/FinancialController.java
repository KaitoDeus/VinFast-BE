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
 * REST Controllers for Financial Operations, Invoices, and Expenses.
 */
@RestController
@RequestMapping("/api/v1/financials")
@Tag(name = "Financials & Reports", description = "Báo cáo doanh thu tổng quan, lợi nhuận ròng và biểu đồ tài chính")
public class FinancialController {

    private final FinancialService financialService;

    public FinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tổng quan tài chính & biểu đồ doanh thu", description = "Lấy tổng doanh thu, chi phí, lợi nhuận ròng, công nợ và biểu đồ 12 tháng")
    public ResponseEntity<ApiResponse<FinancialDTO.OverviewResponse>> getOverview() {
        FinancialDTO.OverviewResponse overview = financialService.getFinancialOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }
}
