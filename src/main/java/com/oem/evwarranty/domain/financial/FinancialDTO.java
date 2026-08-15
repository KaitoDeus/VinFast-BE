package com.oem.evwarranty.domain.financial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FinancialDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewResponse {
        private String totalRevenue; // "$124,500.00"
        private Double totalRevenueValue;
        private String totalExpenses; // "$38,200.00"
        private Double totalExpensesValue;
        private String netProfit; // "$86,300.00"
        private Double netProfitValue;
        private String pendingPayments; // "$12,400.00"
        private Double pendingPaymentsValue;
        private List<MonthlyChartPoint> monthlyRevenueChart;
        private List<CategoryBreakdown> expensesByCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyChartPoint {
        private String month; // "Jan", "Feb", ...
        private Double revenue;
        private Double expenses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category; // "MAINTENANCE"
        private Double amount;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceResponse {
        private String id; // "INV-2028-001"
        private Long numericId;
        private String bookingCode;
        private String clientName;
        private String clientEmail;
        private String amount; // "$480.00"
        private Double amountValue;
        private LocalDate dueDate;
        private LocalDate paidDate;
        private String status; // "Completed", "Pending", "Overdue"
        private String paymentMethod; // "VNPay", "MoMo", "Credit Card"
        private String invoiceDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInvoiceRequest {
        private Long bookingId;
        private Long clientId;

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        private LocalDate dueDate;
        private String paymentMethod = "VNPAY";
        private String status = "PENDING";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseResponse {
        private Long id;
        private String title;
        private String category;
        private String amount; // "$120.00"
        private Double amountValue;
        private LocalDate expenseDate;
        private String recipientName;
        private String paymentMethod;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateExpenseRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        private LocalDate expenseDate;
        private String recipientName;
        private String paymentMethod = "BANK_TRANSFER";
        private String description;
    }
}
