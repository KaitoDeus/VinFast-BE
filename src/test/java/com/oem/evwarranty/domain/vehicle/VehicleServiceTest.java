package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehiclePartRepository vehiclePartRepository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private Customer customer;
    private Part part;
    private User user;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Nguyen Van A");

        vehicle = Vehicle.builder()
                .id(1L)
                .vin("VF8E3400123456789")
                .model("VF8 Plus")
                .make("VinFast")
                .year(2024)
                .mileage(15000)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .warrantyStartDate(LocalDate.of(2024, 1, 1))
                .warrantyEndDate(LocalDate.of(2027, 1, 1))
                .build();

        part = Part.builder()
                .id(10L)
                .name("Battery Pack 87.7 kWh")
                .partNumber("BAT-877-VF8")
                .warrantyMonths(96)
                .build();

        user = User.builder()
                .id(5L)
                .username("tech_hanoi")
                .fullName("Tran Van B")
                .build();
    }

    @Test
    @DisplayName("Create vehicle with valid VIN - Success")
    void createVehicle_ValidVin_Success() {
        when(vehicleRepository.existsByVin(vehicle.getVin())).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle created = vehicleService.createVehicle(vehicle, 1L);

        assertNotNull(created);
        assertEquals(vehicle.getVin(), created.getVin());
        assertEquals(customer, created.getCustomer());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Create vehicle with invalid VIN format throws Exception")
    void createVehicle_InvalidVin_ThrowsException() {
        vehicle.setVin("INVALID-SHORT");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.createVehicle(vehicle, null);
        });

        assertEquals("Invalid VIN format", exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Create vehicle with existing VIN throws Exception")
    void createVehicle_ExistingVin_ThrowsException() {
        when(vehicleRepository.existsByVin(vehicle.getVin())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.createVehicle(vehicle, null);
        });

        assertEquals("VIN already exists", exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Find vehicle by VIN returns matching vehicle")
    void findByVin_Found_ReturnsVehicle() {
        when(vehicleRepository.findByVin(vehicle.getVin())).thenReturn(Optional.of(vehicle));

        Optional<Vehicle> found = vehicleService.findByVin(vehicle.getVin());

        assertTrue(found.isPresent());
        assertEquals(vehicle.getVin(), found.get().getVin());
    }

    @Test
    @DisplayName("Update vehicle with non-existent ID throws Exception")
    void updateVehicle_NotFound_ThrowsException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.updateVehicle(999L, vehicle);
        });
    }

    @Test
    @DisplayName("Update mileage - Valid increment succeeds")
    void updateMileage_ValidIncrement_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle updated = vehicleService.updateMileage(1L, 20000);

        assertEquals(20000, updated.getMileage());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    @DisplayName("Update mileage - Decrement throws Exception")
    void updateMileage_Decrement_ThrowsException() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.updateMileage(1L, 10000);
        });

        assertTrue(exception.getMessage().contains("New mileage cannot be less than current odometer reading"));
    }

    @Test
    @DisplayName("Install part on vehicle - Success with auto warranty calculation")
    void installPart_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(partRepository.findById(10L)).thenReturn(Optional.of(part));
        when(vehiclePartRepository.existsBySerialNumber("SN-BAT-001")).thenReturn(false);
        when(userRepository.findByUsername("tech_hanoi")).thenReturn(Optional.of(user));
        when(vehiclePartRepository.save(any(VehiclePart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehiclePart installed = vehicleService.installPart(1L, 10L, "SN-BAT-001", "Installed battery", "tech_hanoi");

        assertNotNull(installed);
        assertEquals("SN-BAT-001", installed.getSerialNumber());
        assertEquals(VehiclePart.PartStatus.ACTIVE, installed.getStatus());
        assertEquals(user, installed.getInstalledBy());
        assertNotNull(installed.getWarrantyEndDate());
        verify(vehiclePartRepository).save(any(VehiclePart.class));
    }

    @Test
    @DisplayName("Install part with duplicate serial number throws Exception")
    void installPart_DuplicateSerialNumber_ThrowsException() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(partRepository.findById(10L)).thenReturn(Optional.of(part));
        when(vehiclePartRepository.existsBySerialNumber("SN-BAT-001")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.installPart(1L, 10L, "SN-BAT-001", "Notes", "tech_hanoi");
        });

        assertEquals("Serial number already exists: SN-BAT-001", exception.getMessage());
        verify(vehiclePartRepository, never()).save(any(VehiclePart.class));
    }

    @Test
    @DisplayName("Remove part marks status as REPLACED")
    void removePart_Success() {
        VehiclePart vp = VehiclePart.builder()
                .id(100L)
                .serialNumber("SN-BAT-OLD")
                .status(VehiclePart.PartStatus.ACTIVE)
                .build();

        when(vehiclePartRepository.findById(100L)).thenReturn(Optional.of(vp));
        when(vehiclePartRepository.save(any(VehiclePart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehiclePart removed = vehicleService.removePart(100L);

        assertEquals(VehiclePart.PartStatus.REPLACED, removed.getStatus());
        verify(vehiclePartRepository).save(vp);
    }
}
