package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.common.config.CacheConfig;
import com.oem.evwarranty.common.enums.CarType;
import com.oem.evwarranty.common.enums.VehicleStatus;
import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for Vehicle management operations, caching, and serial part installation tracking.
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

    @Cacheable(value = CacheConfig.CACHE_VEHICLES, key = "#query + '-' + #carTypeStr + '-' + #statusStr + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Vehicle> findVehiclesWithFilters(String query, String carTypeStr, String statusStr, @NonNull Pageable pageable) {
        CarType carType = null;
        if (carTypeStr != null && !carTypeStr.equalsIgnoreCase("ALL") && !carTypeStr.isBlank()) {
            try {
                carType = CarType.valueOf(carTypeStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        VehicleStatus status = null;
        if (statusStr != null && !statusStr.equalsIgnoreCase("ALL") && !statusStr.isBlank()) {
            try {
                status = VehicleStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        return vehicleRepository.findVehiclesWithFilters(query, carType, status, pageable);
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

    @CacheEvict(value = CacheConfig.CACHE_VEHICLES, allEntries = true)
    public Vehicle createVehicle(Vehicle vehicle, Long customerId) {
        if (vehicle.getVin() != null && !vehicle.getVin().isBlank()) {
            if (!isValidVin(vehicle.getVin())) {
                throw new IllegalArgumentException("Invalid VIN format");
            }
            if (vehicleRepository.existsByVin(vehicle.getVin())) {
                throw new IllegalArgumentException("VIN already exists");
            }
        } else {
            vehicle.setVin("VF" + (System.currentTimeMillis() % 100000000000000L));
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

    @CacheEvict(value = CacheConfig.CACHE_VEHICLES, allEntries = true)
    public Vehicle updateVehicle(@NonNull Long id, Vehicle updatedVehicle) {
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    if (updatedVehicle.getModel() != null) vehicle.setModel(updatedVehicle.getModel());
                    if (updatedVehicle.getModelName() != null) vehicle.setModelName(updatedVehicle.getModelName());
                    if (updatedVehicle.getBrand() != null) vehicle.setBrand(updatedVehicle.getBrand());
                    if (updatedVehicle.getMake() != null) vehicle.setMake(updatedVehicle.getMake());
                    if (updatedVehicle.getCarType() != null) vehicle.setCarType(updatedVehicle.getCarType());
                    if (updatedVehicle.getLicensePlate() != null) vehicle.setLicensePlate(updatedVehicle.getLicensePlate());
                    if (updatedVehicle.getDailyPrice() != null) vehicle.setDailyPrice(updatedVehicle.getDailyPrice());
                    if (updatedVehicle.getYear() != null) vehicle.setYear(updatedVehicle.getYear());
                    if (updatedVehicle.getColor() != null) vehicle.setColor(updatedVehicle.getColor());
                    if (updatedVehicle.getTransmission() != null) vehicle.setTransmission(updatedVehicle.getTransmission());
                    if (updatedVehicle.getCapacity() != null) vehicle.setCapacity(updatedVehicle.getCapacity());
                    if (updatedVehicle.getRangeKm() != null) vehicle.setRangeKm(updatedVehicle.getRangeKm());
                    if (updatedVehicle.getBatteryType() != null) vehicle.setBatteryType(updatedVehicle.getBatteryType());
                    if (updatedVehicle.getBatteryCapacity() != null) vehicle.setBatteryCapacity(updatedVehicle.getBatteryCapacity());
                    if (updatedVehicle.getBatteryFuelPercent() != null) vehicle.setBatteryFuelPercent(updatedVehicle.getBatteryFuelPercent());
                    if (updatedVehicle.getTopSpeedKmh() != null) vehicle.setTopSpeedKmh(updatedVehicle.getTopSpeedKmh());
                    if (updatedVehicle.getAccelerationSpec() != null) vehicle.setAccelerationSpec(updatedVehicle.getAccelerationSpec());
                    if (updatedVehicle.getHeroImageUrl() != null) vehicle.setHeroImageUrl(updatedVehicle.getHeroImageUrl());
                    if (updatedVehicle.getDescription() != null) vehicle.setDescription(updatedVehicle.getDescription());
                    if (updatedVehicle.getUnitsCount() != null) vehicle.setUnitsCount(updatedVehicle.getUnitsCount());
                    if (updatedVehicle.getMotorType() != null) vehicle.setMotorType(updatedVehicle.getMotorType());
                    if (updatedVehicle.getMileage() != null) vehicle.setMileage(updatedVehicle.getMileage());
                    if (updatedVehicle.getStatus() != null) vehicle.setStatus(updatedVehicle.getStatus());
                    if (updatedVehicle.getFleetStatus() != null) vehicle.setFleetStatus(updatedVehicle.getFleetStatus());
                    return vehicleRepository.save(vehicle);
                })
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));
    }

    @CacheEvict(value = CacheConfig.CACHE_VEHICLES, allEntries = true)
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

    @CacheEvict(value = CacheConfig.CACHE_VEHICLES, allEntries = true)
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
