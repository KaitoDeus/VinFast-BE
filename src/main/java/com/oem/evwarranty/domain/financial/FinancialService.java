package com.oem.evwarranty.domain.financial;

import com.oem.evwarranty.common.config.CacheConfig;
import com.oem.evwarranty.common.enums.ExpenseCategory;
import com.oem.evwarranty.common.enums.InvoiceStatus;
import com.oem.evwarranty.common.enums.PaymentMethod;
import com.oem.evwarranty.domain.booking.Booking;
import com.oem.evwarranty.domain.booking.BookingRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FinancialService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public FinancialService(InvoiceRepository invoiceRepository,
                            ExpenseRepository expenseRepository,
                            BookingRepository bookingRepository,
                            UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.expenseRepository = expenseRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = CacheConfig.CACHE_FINANCIAL_OVERVIEW)
    public FinancialDTO.OverviewResponse getFinancialOverview() {
        BigDecimal totalRevenue = invoiceRepository.sumTotalCompletedRevenue();
        BigDecimal totalExpenses = expenseRepository.sumTotalExpenses();
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal pendingPayments = invoiceRepository.sumTotalPendingPayments();

        // Build 12-month synthetic & recorded monthly trend points
        List<FinancialDTO.MonthlyChartPoint> chart = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        double baseRev = totalRevenue.doubleValue() > 0 ? totalRevenue.doubleValue() / 12.0 : 8500.0;
        double baseExp = totalExpenses.doubleValue() > 0 ? totalExpenses.doubleValue() / 12.0 : 2800.0;

        for (int i = 0; i < 12; i++) {
            double rev = Math.round((baseRev * (0.8 + 0.05 * i)) * 100.0) / 100.0;
            double exp = Math.round((baseExp * (0.85 + 0.03 * i)) * 100.0) / 100.0;
            chart.add(new FinancialDTO.MonthlyChartPoint(months[i], rev, exp));
        }

        // Build Category breakdown
        List<FinancialDTO.CategoryBreakdown> breakdowns = new ArrayList<>();
        List<Object[]> rawCategories = expenseRepository.sumExpensesGroupedByCategory();
        double totalExpVal = totalExpenses.doubleValue() > 0 ? totalExpenses.doubleValue() : 1.0;

        if (rawCategories.isEmpty()) {
            breakdowns.add(new FinancialDTO.CategoryBreakdown("MAINTENANCE", 16200.0, 42.4));
            breakdowns.add(new FinancialDTO.CategoryBreakdown("FUEL_CHARGING", 9500.0, 24.9));
            breakdowns.add(new FinancialDTO.CategoryBreakdown("SALARIES", 7500.0, 19.6));
            breakdowns.add(new FinancialDTO.CategoryBreakdown("INSURANCE", 5000.0, 13.1));
        } else {
            for (Object[] row : rawCategories) {
                ExpenseCategory cat = (ExpenseCategory) row[0];
                BigDecimal amt = (BigDecimal) row[1];
                double pct = Math.round((amt.doubleValue() / totalExpVal * 100.0) * 10.0) / 10.0;
                breakdowns.add(new FinancialDTO.CategoryBreakdown(cat.name(), amt.doubleValue(), pct));
            }
        }

        return FinancialDTO.OverviewResponse.builder()
                .totalRevenue("$" + String.format("%,.2f", totalRevenue))
                .totalRevenueValue(totalRevenue.doubleValue())
                .totalExpenses("$" + String.format("%,.2f", totalExpenses))
                .totalExpensesValue(totalExpenses.doubleValue())
                .netProfit("$" + String.format("%,.2f", netProfit))
                .netProfitValue(netProfit.doubleValue())
                .pendingPayments("$" + String.format("%,.2f", pendingPayments))
                .pendingPaymentsValue(pendingPayments.doubleValue())
                .monthlyRevenueChart(chart)
                .expensesByCategory(breakdowns)
                .build();
    }

    public Page<FinancialDTO.InvoiceResponse> getInvoices(String statusStr, Pageable pageable) {
        InvoiceStatus status = null;
        if (statusStr != null && !statusStr.equalsIgnoreCase("ALL") && !statusStr.isBlank()) {
            try {
                status = InvoiceStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {}
        }
        return invoiceRepository.findByStatusWithPagination(status, pageable)
                .map(this::toInvoiceResponse);
    }

    @CacheEvict(value = CacheConfig.CACHE_FINANCIAL_OVERVIEW, allEntries = true)
    public FinancialDTO.InvoiceResponse createInvoice(FinancialDTO.CreateInvoiceRequest request) {
        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId()).orElse(null);
        }

        User client = null;
        if (request.getClientId() != null) {
            client = userRepository.findById(request.getClientId()).orElse(null);
        } else if (booking != null) {
            client = booking.getClient();
        }

        PaymentMethod method = PaymentMethod.VNPAY;
        if (request.getPaymentMethod() != null) {
            try {
                method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
            } catch (Exception ignored) {}
        }

        InvoiceStatus status = InvoiceStatus.PENDING;
        if (request.getStatus() != null) {
            try {
                status = InvoiceStatus.valueOf(request.getStatus().toUpperCase());
            } catch (Exception ignored) {}
        }

        String invoiceNumber = "INV-2028-" + String.format("%03d", invoiceRepository.count() + 1);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .booking(booking)
                .client(client)
                .amount(request.getAmount())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(7))
                .status(status)
                .paymentMethod(method)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        return toInvoiceResponse(saved);
    }

    @CacheEvict(value = CacheConfig.CACHE_FINANCIAL_OVERVIEW, allEntries = true)
    public FinancialDTO.InvoiceResponse markInvoicePaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));

        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setPaidDate(LocalDate.now());
        return toInvoiceResponse(invoiceRepository.save(invoice));
    }

    public Page<FinancialDTO.ExpenseResponse> getExpenses(String categoryStr, Pageable pageable) {
        ExpenseCategory category = null;
        if (categoryStr != null && !categoryStr.equalsIgnoreCase("ALL") && !categoryStr.isBlank()) {
            try {
                category = ExpenseCategory.valueOf(categoryStr.toUpperCase());
            } catch (Exception ignored) {}
        }
        return expenseRepository.findByCategoryWithPagination(category, pageable)
                .map(this::toExpenseResponse);
    }

    @CacheEvict(value = CacheConfig.CACHE_FINANCIAL_OVERVIEW, allEntries = true)
    public FinancialDTO.ExpenseResponse createExpense(FinancialDTO.CreateExpenseRequest request) {
        ExpenseCategory category = ExpenseCategory.OTHER;
        if (request.getCategory() != null) {
            try {
                category = ExpenseCategory.valueOf(request.getCategory().toUpperCase());
            } catch (Exception ignored) {}
        }

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .category(category)
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now())
                .recipientName(request.getRecipientName())
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .build();

        Expense saved = expenseRepository.save(expense);
        return toExpenseResponse(saved);
    }

    public FinancialDTO.InvoiceResponse toInvoiceResponse(Invoice inv) {
        if (inv == null) return null;

        String clientName = inv.getClient() != null ? inv.getClient().getFullName() : "Customer";
        String clientEmail = inv.getClient() != null ? inv.getClient().getEmail() : "customer@vinfast.vn";
        String bookingCode = inv.getBooking() != null ? inv.getBooking().getBookingCode() : "N/A";
        String amountStr = "$" + String.format("%,.2f", inv.getAmount());

        String invDate = inv.getCreatedAt() != null
                ? inv.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        return FinancialDTO.InvoiceResponse.builder()
                .id(inv.getInvoiceNumber())
                .numericId(inv.getId())
                .bookingCode(bookingCode)
                .clientName(clientName)
                .clientEmail(clientEmail)
                .amount(amountStr)
                .amountValue(inv.getAmount().doubleValue())
                .dueDate(inv.getDueDate())
                .paidDate(inv.getPaidDate())
                .status(inv.getStatus() != null ? capitalize(inv.getStatus().name()) : "Pending")
                .paymentMethod(inv.getPaymentMethod() != null ? inv.getPaymentMethod().name() : "VNPAY")
                .invoiceDate(invDate)
                .build();
    }

    public FinancialDTO.ExpenseResponse toExpenseResponse(Expense exp) {
        if (exp == null) return null;

        String amountStr = "$" + String.format("%,.2f", exp.getAmount());

        return FinancialDTO.ExpenseResponse.builder()
                .id(exp.getId())
                .title(exp.getTitle())
                .category(exp.getCategory() != null ? exp.getCategory().name() : "OTHER")
                .amount(amountStr)
                .amountValue(exp.getAmount().doubleValue())
                .expenseDate(exp.getExpenseDate())
                .recipientName(exp.getRecipientName())
                .paymentMethod(exp.getPaymentMethod())
                .description(exp.getDescription())
                .build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
