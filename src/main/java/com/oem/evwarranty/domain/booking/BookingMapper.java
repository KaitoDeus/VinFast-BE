package com.oem.evwarranty.domain.booking;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class BookingMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingDTO.BookingResponse toDTO(Booking booking) {
        if (booking == null) return null;

        String clientName = booking.getClient() != null ? booking.getClient().getFullName() : "Alice Johnson";
        String clientAvatar = (booking.getClient() != null && booking.getClient().getAvatarUrl() != null)
                ? booking.getClient().getAvatarUrl() : "/team/avatar-1.png";

        String carModel = booking.getVehicle() != null
                ? (booking.getVehicle().getModelName() != null ? booking.getVehicle().getModelName() : booking.getVehicle().getModel())
                : "VinFast VF 8";

        String driverName = booking.getDriver() != null ? booking.getDriver().getFullName() : "Nguyen Van Tai";

        String rentalPeriod = "";
        if (booking.getStartDate() != null && booking.getEndDate() != null) {
            rentalPeriod = booking.getStartDate().format(DATE_FORMATTER) + " - " +
                    booking.getEndDate().format(DATE_FORMATTER) + " " +
                    booking.getEndDate().format(YEAR_FORMATTER);
        }

        String rentalPlan = booking.getRentalPlan() != null ? capitalize(booking.getRentalPlan().name()) : "Daily";
        String paymentStatus = booking.getPaymentStatus() != null ? capitalize(booking.getPaymentStatus().name()) : "Paid";
        String status = booking.getStatus() != null ? capitalize(booking.getStatus().name()) : "Approved";

        String bookingDate = booking.getCreatedAt() != null
                ? booking.getCreatedAt().toLocalDate().format(ISO_DATE)
                : (booking.getStartDate() != null ? booking.getStartDate().format(ISO_DATE) : "2028-08-01");

        return BookingDTO.BookingResponse.builder()
                .id(booking.getBookingCode())
                .numericId(booking.getId())
                .bookingDate(bookingDate)
                .clientName(clientName)
                .clientAvatar(clientAvatar)
                .carModel(carModel)
                .rentalPlan(rentalPlan)
                .rentalPeriod(rentalPeriod)
                .driverName(driverName)
                .paymentStatus(paymentStatus)
                .status(status)
                .totalAmount(booking.getTotalAmount() != null ? booking.getTotalAmount().doubleValue() : 480.0)
                .notes(booking.getNotes())
                .vehicleId(booking.getVehicle() != null ? booking.getVehicle().getId() : null)
                .clientId(booking.getClient() != null ? booking.getClient().getId() : null)
                .driverId(booking.getDriver() != null ? booking.getDriver().getId() : null)
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
