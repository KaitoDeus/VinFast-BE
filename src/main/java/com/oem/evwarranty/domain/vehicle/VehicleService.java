package com.oem.evwarranty.domain.vehicle;


import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehiclePart;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.vehicle.VehiclePartRepository;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for Vehicle management operations.
 */
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehiclePartRepository vehiclePartRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          CustomerRepository customerRepository,
                          VehiclePartRepository vehiclePartRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.vehiclePartRepository = vehiclePartRepository;
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> findById(@NonNull Long id) {
        return vehicleRepository.findById(id);
    }

    public Optional<Vehicle> findByVin(String vin) {
        return vehicleRepository.findByVin(vin);
    }

    public Page<Vehicle> findAll(@NonNull Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    public Page<Vehicle> searchVehicles(String search, @NonNull Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return vehicleRepository.findAll(pageable);
        }
        return vehicleRepository.searchVehicles(search, pageable);
    }

    public List<Vehicle> findByCustomerId(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }

    public List<Vehicle> findVehiclesUnderWarranty() {
        return vehicleRepository.findVehiclesUnderWarranty();
    }

    public List<VehiclePart> findPartsByVehicleId(Long vehicleId) {
        return vehiclePartRepository.findByVehicleId(vehicleId);
    }

    public Vehicle createVehicle(Vehicle vehicle, Long customerId) {
        if (!isValidVin(vehicle.getVin())) {
            throw new IllegalArgumentException("Invalid VIN format");
        }

        if (vehicleRepository.existsByVin(vehicle.getVin())) {
            throw new IllegalArgumentException("VIN already exists");
        }

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            vehicle.setCustomer(customer);
        }

        if (vehicle.getWarrantyStartDate() == null) {
            vehicle.setWarrantyStartDate(LocalDate.now());
        }
        if (vehicle.getWarrantyEndDate() == null) {
            vehicle.setWarrantyEndDate(vehicle.getWarrantyStartDate().plusYears(3));
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(@NonNull Long id, Vehicle updatedVehicle) {
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicle.setModel(updatedVehicle.getModel());
                    vehicle.setMake(updatedVehicle.getMake());
                    vehicle.setYear(updatedVehicle.getYear());
                    vehicle.setColor(updatedVehicle.getColor());
                    vehicle.setBatteryType(updatedVehicle.getBatteryType());
                    vehicle.setBatteryCapacity(updatedVehicle.getBatteryCapacity());
                    vehicle.setMotorType(updatedVehicle.getMotorType());
                    vehicle.setMileage(updatedVehicle.getMileage());
                    vehicle.setStatus(updatedVehicle.getStatus());
                    return vehicleRepository.save(vehicle);
                })
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    public void deleteVehicle(@NonNull Long id) {
        vehicleRepository.deleteById(id);
    }

    public long count() {
        return vehicleRepository.count();
    }

    public long countByStatus(Vehicle.VehicleStatus status) {
        return vehicleRepository.countByStatus(status);
    }

    private boolean isValidVin(String vin) {
        if (vin == null || vin.length() != 17) {
            return false;
        }
        return vin.matches("[A-HJ-NPR-Z0-9]{17}");
    }
}



