package com.oem.evwarranty.domain.booking;

import com.oem.evwarranty.common.enums.BookingStatus;
import com.oem.evwarranty.common.enums.PaymentStatus;
import com.oem.evwarranty.common.enums.RentalPlan;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          VehicleRepository vehicleRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Page<Booking> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Optional<Booking> findByBookingCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode);
    }

    public BookingKpiDTO getKpis() {
        long upcoming = bookingRepository.countUpcoming();
        long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        long canceled = bookingRepository.countByStatus(BookingStatus.CANCELED);
        long completed = bookingRepository.countByStatus(BookingStatus.DONE);

        return new BookingKpiDTO(upcoming, pending, canceled, completed, 2.77);
    }

    public Booking createBooking(BookingDTO.CreateBookingRequest request, String creatorUsername) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + request.getVehicleId()));

        User client = null;
        if (request.getClientId() != null) {
            client = userRepository.findById(request.getClientId()).orElse(null);
        } else if (creatorUsername != null) {
            client = userRepository.findByUsername(creatorUsername).orElse(null);
        }

        User driver = null;
        if (request.getDriverId() != null) {
            driver = userRepository.findById(request.getDriverId()).orElse(null);
        }

        RentalPlan plan = RentalPlan.DAILY;
        if (request.getRentalPlan() != null) {
            try {
                plan = RentalPlan.valueOf(request.getRentalPlan().toUpperCase());
            } catch (Exception ignored) {}
        }

        BigDecimal totalAmount = request.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            long days = Math.max(1, ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()));
            BigDecimal dailyRate = vehicle.getDailyPrice() != null ? vehicle.getDailyPrice() : BigDecimal.valueOf(120.0);
            totalAmount = dailyRate.multiply(BigDecimal.valueOf(days));
        }

        String bookingCode = "BK-2028-" + String.format("%03d", (bookingRepository.count() + 1));

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .client(client)
                .vehicle(vehicle)
                .driver(driver)
                .rentalPlan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.PAID)
                .status(BookingStatus.APPROVED)
                .notes(request.getNotes())
                .build();

        return bookingRepository.save(booking);
    }

    public Booking updateStatus(Long id, String statusStr) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        BookingStatus newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    public record BookingKpiDTO(long upcomingBookings, long pendingBookings, long canceledBookings, long completedBookings, double weeklyGrowthPercentage) {}
}
