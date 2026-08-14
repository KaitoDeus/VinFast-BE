package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for Vehicle management operations and serial part installation tracking.
 */
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehiclePartRepository vehiclePartRepository;
    private final PartRepository partRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          CustomerRepository customerRepository,
                          VehiclePartRepository vehiclePartRepository,
                          PartRepository partRepository,
                          UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.vehiclePartRepository = vehiclePartRepository;
        this.partRepository = partRepository;
        this.userRepository = userRepository;
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
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
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
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));
    }

    public Vehicle updateMileage(Long vehicleId, Integer newMileage) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        if (newMileage == null || newMileage < 0) {
            throw new IllegalArgumentException("Mileage must be a non-negative integer");
        }
        if (vehicle.getMileage() != null && newMileage < vehicle.getMileage()) {
            throw new IllegalArgumentException("New mileage cannot be less than current odometer reading: " + vehicle.getMileage());
        }

        vehicle.setMileage(newMileage);
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(@NonNull Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    public long count() {
        return vehicleRepository.count();
    }

    public long countByStatus(Vehicle.VehicleStatus status) {
        return vehicleRepository.countByStatus(status);
    }

    /**
     * Install a serial part on a vehicle.
     * Automatically calculates warranty end date based on the catalog part's warrantyMonths.
     */
    public VehiclePart installPart(Long vehicleId, Long partId, String serialNumber,
                                   String notes, String installerUsername) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with ID: " + partId));

        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Serial number cannot be blank");
        }

        if (vehiclePartRepository.existsBySerialNumber(serialNumber.trim())) {
            throw new IllegalArgumentException("Serial number already exists: " + serialNumber);
        }

        User installer = null;
        if (installerUsername != null && !installerUsername.isBlank()) {
            installer = userRepository.findByUsername(installerUsername).orElse(null);
        }

        LocalDate now = LocalDate.now();
        int warrantyMonths = part.getWarrantyMonths() != null ? part.getWarrantyMonths() : 12;

        VehiclePart vehiclePart = VehiclePart.builder()
                .vehicle(vehicle)
                .part(part)
                .serialNumber(serialNumber.trim())
                .installationDate(now)
                .warrantyStartDate(now)
                .warrantyEndDate(now.plusMonths(warrantyMonths))
                .status(VehiclePart.PartStatus.ACTIVE)
                .installedBy(installer)
                .notes(notes)
                .build();

        return vehiclePartRepository.save(vehiclePart);
    }

    /**
     * Remove or replace an installed part on a vehicle (sets status to REPLACED).
     */
    public VehiclePart removePart(Long vehiclePartId) {
        VehiclePart vp = vehiclePartRepository.findById(vehiclePartId)
                .orElseThrow(() -> new IllegalArgumentException("Installed part not found with ID: " + vehiclePartId));
        vp.setStatus(VehiclePart.PartStatus.REPLACED);
        return vehiclePartRepository.save(vp);
    }

    private boolean isValidVin(String vin) {
        if (vin == null || vin.length() != 17) {
            return false;
        }
        return vin.matches("[A-HJ-NPR-Z0-9]{17}");
    }
}
