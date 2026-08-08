package com.oem.evwarranty.domain.vehicle;



import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleService;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFullName("John Doe");

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setVin("1234567890ABCDEFG");
        vehicle.setModel("Model S");
        vehicle.setMake("Tesla");
    }

    @Test
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
    void createVehicle_InvalidVin_ThrowsException() {
        vehicle.setVin("INVALID");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.createVehicle(vehicle, null);
        });

        assertEquals("Invalid VIN format", exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_ExistingVin_ThrowsException() {
        when(vehicleRepository.existsByVin(vehicle.getVin())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.createVehicle(vehicle, null);
        });

        assertEquals("VIN already exists", exception.getMessage());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void findByVin_Found_ReturnsVehicle() {
        when(vehicleRepository.findByVin(vehicle.getVin())).thenReturn(Optional.of(vehicle));

        Optional<Vehicle> found = vehicleService.findByVin(vehicle.getVin());

        assertTrue(found.isPresent());
        assertEquals(vehicle.getVin(), found.get().getVin());
    }

    @Test
    void updateVehicle_NotFound_ThrowsException() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.updateVehicle(1L, vehicle);
        });
    }
}
