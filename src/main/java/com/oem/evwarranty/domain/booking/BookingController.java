package com.oem.evwarranty.domain.booking;

import com.oem.evwarranty.common.annotation.Auditable;
import com.oem.evwarranty.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Bookings & Rental Contracts.
 * Base Path: /api/v1/bookings
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings Management", description = "Quản lý đơn đặt xe, hợp đồng thuê và các chỉ số KPI vận hành")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    @GetMapping
    @Operation(summary = "Danh sách đơn đặt xe & KPIs", description = "Lấy danh sách các đơn đặt xe kèm bộ chỉ số KPI vận hành tổng quan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookings() {
        BookingService.BookingKpiDTO kpis = bookingService.getKpis();
        List<Booking> bookings = bookingService.findAll();

        List<BookingDTO.BookingResponse> items = bookings.stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> data = Map.of(
                "kpis", kpis,
                "items", items
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    @Operation(summary = "Tạo đơn đặt xe mới", description = "Khách hàng hoặc điều phối viên tạo đơn thuê xe điện mới")
    @Auditable(action = "CREATE_BOOKING", resourceType = "BOOKING")
    public ResponseEntity<ApiResponse<BookingDTO.BookingResponse>> createBooking(
            @Valid @RequestBody BookingDTO.CreateBookingRequest request,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        Booking created = bookingService.createBooking(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đơn đặt xe thành công", bookingMapper.toDTO(created)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn đặt xe", description = "Lấy thông tin chi tiết một đơn đặt xe theo ID hoặc Booking Code")
    public ResponseEntity<ApiResponse<BookingDTO.BookingResponse>> getBookingById(@PathVariable String id) {
        Optional<Booking> bookingOpt;
        try {
            Long numericId = Long.parseLong(id);
            bookingOpt = bookingService.findById(numericId);
        } catch (NumberFormatException e) {
            bookingOpt = bookingService.findByBookingCode(id);
        }

        return bookingOpt.map(bookingMapper::toDTO)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy đơn đặt xe: " + id, "BOOKING_NOT_FOUND")));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'SC_STAFF')")
    @Operation(summary = "Cập nhật trạng thái đơn đặt xe", description = "Chuyển trạng thái đơn: APPROVED, HIRED, DONE, CANCELED")
    @Auditable(action = "UPDATE_BOOKING_STATUS", resourceType = "BOOKING")
    public ResponseEntity<ApiResponse<BookingDTO.BookingResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingDTO.UpdateStatusRequest request) {

        Booking updated = bookingService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", bookingMapper.toDTO(updated)));
    }
}
