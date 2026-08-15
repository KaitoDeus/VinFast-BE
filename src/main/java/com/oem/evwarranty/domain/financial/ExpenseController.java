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
 * REST Controller for Expenses Management.
 * Base Path: /api/v1/expenses
 */
@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses Management", description = "Ghi nhận chi phí vận hành đội xe, bảo dưỡng, sạc điện và lương bổng")
public class ExpenseController {

    private final FinancialService financialService;

    public ExpenseController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Danh sách chi phí", description = "Lấy danh sách các khoản chi phí phân trang và lọc theo danh mục: MAINTENANCE, FUEL_CHARGING, SALARIES, INSURANCE")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExpenses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "ALL") String category) {

        int pageIndex = Math.max(0, page - 1);
        Page<FinancialDTO.ExpenseResponse> expensePage = financialService.getExpenses(
                category, PageRequest.of(pageIndex, limit, Sort.by("id").descending()));

        Map<String, Object> data = Map.of(
                "items", expensePage.getContent(),
                "pagination", ApiPagination.fromPage(expensePage)
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Ghi nhận chi phí mới", description = "Thêm khoản chi phí vận hành vào sổ cái")
    @Auditable(action = "CREATE_EXPENSE", resourceType = "EXPENSE")
    public ResponseEntity<ApiResponse<FinancialDTO.ExpenseResponse>> createExpense(
            @Valid @RequestBody FinancialDTO.CreateExpenseRequest request) {

        FinancialDTO.ExpenseResponse created = financialService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ghi nhận chi phí thành công", created));
    }
}
