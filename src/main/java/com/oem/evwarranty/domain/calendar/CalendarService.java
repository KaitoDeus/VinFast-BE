package com.oem.evwarranty.domain.calendar;

import com.oem.evwarranty.domain.booking.Booking;
import com.oem.evwarranty.domain.booking.BookingRepository;
import com.oem.evwarranty.domain.claim.ServiceHistory;
import com.oem.evwarranty.domain.claim.ServiceHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final BookingRepository bookingRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final List<CalendarEventDTO.EventResponse> manualEvents = new CopyOnWriteArrayList<>();

    public CalendarService(BookingRepository bookingRepository,
                           ServiceHistoryRepository serviceHistoryRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceHistoryRepository = serviceHistoryRepository;
    }

    public List<CalendarEventDTO.EventResponse> getEvents(Integer month, Integer year, String type) {
        List<CalendarEventDTO.EventResponse> allEvents = new ArrayList<>();

        LocalDate startMonthDate;
        LocalDate endMonthDate;

        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            startMonthDate = ym.atDay(1);
            endMonthDate = ym.atEndOfMonth();
        } else {
            startMonthDate = LocalDate.now().minusMonths(3);
            endMonthDate = LocalDate.now().plusMonths(6);
        }

        // 1. Convert Bookings to Calendar Events
        List<Booking> bookings = bookingRepository.findBookingsInRange(startMonthDate, endMonthDate);
        for (Booking b : bookings) {
            String vehicleName = b.getVehicle() != null
                    ? (b.getVehicle().getModelName() != null ? b.getVehicle().getModelName() : b.getVehicle().getModel())
                    : "VinFast EV";
            String clientName = b.getClient() != null ? b.getClient().getFullName() : "Customer";
            String driverName = b.getDriver() != null ? b.getDriver().getFullName() : "Assigned Driver";

            allEvents.add(CalendarEventDTO.EventResponse.builder()
                    .id(b.getBookingCode())
                    .title("Hợp đồng thuê: " + vehicleName)
                    .type("RENTAL")
                    .status(b.getStatus() != null ? b.getStatus().name() : "CONFIRMED")
                    .startDate(b.getStartDate())
                    .endDate(b.getEndDate())
                    .startTime("08:00 AM")
                    .endTime("06:00 PM")
                    .vehicleId(b.getVehicle() != null ? b.getVehicle().getId() : null)
                    .vehicleName(vehicleName)
                    .customerName(clientName)
                    .driverName(driverName)
                    .notes(b.getNotes())
                    .color("#0055A5")
                    .build());
        }

        // 2. Convert Maintenance Service Histories to Calendar Events
        List<ServiceHistory> services = serviceHistoryRepository.findAll();
        for (ServiceHistory sh : services) {
            if (sh.getServiceDate() != null) {
                LocalDate serviceLocalDate = sh.getServiceDate().toLocalDate();
                if (!serviceLocalDate.isBefore(startMonthDate) && !serviceLocalDate.isAfter(endMonthDate)) {
                    String vName = sh.getVehicle() != null ? sh.getVehicle().getModel() : "VinFast EV";
                    allEvents.add(CalendarEventDTO.EventResponse.builder()
                            .id("SRV-" + sh.getId())
                            .title("Bảo dưỡng định kỳ: " + vName)
                            .type("MAINTENANCE")
                            .status("COMPLETED")
                            .startDate(serviceLocalDate)
                            .endDate(serviceLocalDate)
                            .startTime("09:00 AM")
                            .endTime("12:00 PM")
                            .vehicleId(sh.getVehicle() != null ? sh.getVehicle().getId() : null)
                            .vehicleName(vName)
                            .customerName(sh.getVehicle() != null && sh.getVehicle().getCustomer() != null ? sh.getVehicle().getCustomer().getFullName() : "VinFast")
                            .driverName("Service Technician")
                            .notes(sh.getDescription())
                            .color("#F59E0B")
                            .build());
                }
            }
        }

        // 3. Add manual events
        allEvents.addAll(manualEvents);

        // Apply type filter if present
        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {
            return allEvents.stream()
                    .filter(e -> e.getType() != null && e.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return allEvents;
    }

    public CalendarEventDTO.EventResponse createEvent(CalendarEventDTO.CreateEventRequest request) {
        String eventId = "EVT-" + String.format("%04d", (manualEvents.size() + 1));
        CalendarEventDTO.EventResponse event = CalendarEventDTO.EventResponse.builder()
                .id(eventId)
                .title(request.getTitle())
                .type(request.getType())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .vehicleId(request.getVehicleId())
                .vehicleName(request.getVehicleName() != null ? request.getVehicleName() : "VinFast EV")
                .customerName(request.getCustomerName())
                .driverName(request.getDriverName())
                .notes(request.getNotes())
                .color(request.getColor() != null ? request.getColor() : "#10B981")
                .build();

        manualEvents.add(event);
        return event;
    }
}
