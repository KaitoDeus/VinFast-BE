package com.oem.evwarranty.domain.driver;

import com.oem.evwarranty.common.enums.DriverDutyStatus;
import com.oem.evwarranty.common.enums.UserRole;
import com.oem.evwarranty.domain.user.Role;
import com.oem.evwarranty.domain.user.RoleRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public DriverService(DriverProfileRepository driverProfileRepository,
                         UserRepository userRepository,
                         RoleRepository roleRepository,
                         VehicleRepository vehicleRepository,
                         PasswordEncoder passwordEncoder) {
        this.driverProfileRepository = driverProfileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<DriverDTO.DriverResponse> getDrivers(String query, String statusStr, Pageable pageable) {
        DriverDutyStatus status = null;
        if (statusStr != null && !statusStr.equalsIgnoreCase("ALL") && !statusStr.isBlank()) {
            try {
                status = DriverDutyStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        return driverProfileRepository.searchDrivers(query, status, pageable)
                .map(this::toResponse);
    }

    public Optional<DriverDTO.DriverResponse> getDriverById(Long id) {
        return driverProfileRepository.findById(id)
                .map(this::toResponse);
    }

    public DriverDTO.DriverResponse createDriver(DriverDTO.CreateDriverRequest request) {
        String email = request.getEmail() != null ? request.getEmail() : "driver_" + (System.currentTimeMillis() % 10000) + "@vinfast.vn";
        String username = "driver_" + (System.currentTimeMillis() % 100000);

        Role driverRole = roleRepository.findByName(UserRole.DRIVER.name())
                .orElseGet(() -> roleRepository.save(Role.builder().name(UserRole.DRIVER.name()).description("Driver Role").build()));

        User user = User.builder()
                .username(username)
                .email(email)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : "/team/driver-1.png")
                .password(passwordEncoder.encode("Driver@123"))
                .roles(Set.of(driverRole))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        Vehicle assignedVehicle = null;
        if (request.getAssignedVehicleId() != null) {
            assignedVehicle = vehicleRepository.findById(request.getAssignedVehicleId()).orElse(null);
        }

        DriverProfile profile = DriverProfile.builder()
                .user(savedUser)
                .assignedVehicle(assignedVehicle)
                .status(DriverDutyStatus.ON_DUTY)
                .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 3)
                .rating(BigDecimal.valueOf(4.90))
                .performanceBadge("Top Rated")
                .licenseNumber(request.getLicenseNumber())
                .monthlyWorkHours(160)
                .avatarUrl(request.getAvatarUrl())
                .build();

        DriverProfile savedProfile = driverProfileRepository.save(profile);
        return toResponse(savedProfile);
    }

    public DriverDTO.DriverResponse updateDutyStatus(Long id, String dutyStatusStr) {
        DriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with ID: " + id));

        DriverDutyStatus newStatus = DriverDutyStatus.valueOf(dutyStatusStr.toUpperCase());
        profile.setStatus(newStatus);
        return toResponse(driverProfileRepository.save(profile));
    }

    public DriverDTO.DriverResponse toResponse(DriverProfile dp) {
        if (dp == null) return null;

        String formattedId = "DRV-" + String.format("%03d", dp.getId());
        String fullName = dp.getUser() != null ? dp.getUser().getFullName() : "Nguyen Van Tai";
        String phone = dp.getUser() != null ? dp.getUser().getPhone() : "+84 912 345 678";
        String avatarUrl = dp.getAvatarUrl() != null ? dp.getAvatarUrl() :
                (dp.getUser() != null && dp.getUser().getAvatarUrl() != null ? dp.getUser().getAvatarUrl() : "/team/driver-1.png");

        String assignedCar = "Unassigned";
        Long vehicleId = null;
        if (dp.getAssignedVehicle() != null) {
            vehicleId = dp.getAssignedVehicle().getId();
            String vName = dp.getAssignedVehicle().getModelName() != null ? dp.getAssignedVehicle().getModelName() : dp.getAssignedVehicle().getModel();
            String plate = dp.getAssignedVehicle().getLicensePlate() != null ? dp.getAssignedVehicle().getLicensePlate() : "N/A";
            assignedCar = vName + " - " + plate;
        }

        String dutyStatus = dp.getStatus() != null ? formatDutyStatus(dp.getStatus().name()) : "On Duty";
        double rating = dp.getRating() != null ? dp.getRating().doubleValue() : 4.90;
        int exp = dp.getExperienceYears() != null ? dp.getExperienceYears() : 3;
        int hours = dp.getMonthlyWorkHours() != null ? dp.getMonthlyWorkHours() : 160;

        return DriverDTO.DriverResponse.builder()
                .id(formattedId)
                .numericId(dp.getId())
                .fullName(fullName)
                .phone(phone)
                .assignedCar(assignedCar)
                .assignedVehicleId(vehicleId)
                .dutyStatus(dutyStatus)
                .status(dp.getStatus() != null ? dp.getStatus().name() : "ON_DUTY")
                .rating(rating)
                .experience(exp + " years")
                .experienceYears(exp)
                .performanceBadge(dp.getPerformanceBadge() != null ? dp.getPerformanceBadge() : "Top Rated")
                .monthlyWorkHours(hours + " hrs")
                .workHours(hours)
                .avatarUrl(avatarUrl)
                .licenseNumber(dp.getLicenseNumber())
                .build();
    }

    private String formatDutyStatus(String status) {
        if (status == null) return "On Duty";
        switch (status.toUpperCase()) {
            case "ON_DUTY": return "On Duty";
            case "OFF_DUTY": return "Off Duty";
            case "IN_TRANSIT": return "In Transit";
            case "ON_LEAVE": return "On Leave";
            default: return status;
        }
    }
}
