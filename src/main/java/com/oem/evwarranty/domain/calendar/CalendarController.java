package com.oem.evwarranty.domain.calendar;

import com.oem.evwarranty.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Calendar & Fleet Scheduling.
 * Base Path: /api/v1/calendar
 */
@RestController
@RequestMapping("/api/v1/calendar")
@Tag(name = "Calendar & Scheduling", description = "Lịch biểu cho thuê, bảo dưỡng kỹ thuật và sự kiện xe điện")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    @Operation(summary = "Lấy danh sách sự kiện lịch", description = "Lấy toàn bộ lịch thuê xe, bảo dưỡng định kỳ và kiểm định theo tháng/năm")
    public ResponseEntity<ApiResponse<List<CalendarEventDTO.EventResponse>>> getEvents(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "ALL") String type) {

        List<CalendarEventDTO.EventResponse> events = calendarService.getEvents(month, year, type);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PostMapping("/events")
    @Operation(summary = "Tạo sự kiện lịch mới", description = "Thêm lịch bảo dưỡng, bàn giao hoặc kiểm tra kỹ thuật mới vào hệ thống")
    public ResponseEntity<ApiResponse<CalendarEventDTO.EventResponse>> createEvent(
            @Valid @RequestBody CalendarEventDTO.CreateEventRequest request) {

        CalendarEventDTO.EventResponse event = calendarService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo sự kiện lịch thành công", event));
    }
}
