package com.oem.evwarranty.common.config;

import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.campaign.ServiceCampaignRepository;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import com.oem.evwarranty.domain.claim.WarrantyPolicy;
import com.oem.evwarranty.domain.claim.WarrantyPolicyRepository;
import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.inventory.Inventory;
import com.oem.evwarranty.domain.inventory.InventoryRepository;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;
import com.oem.evwarranty.domain.user.Role;
import com.oem.evwarranty.domain.user.RoleRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehiclePartRepository;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Automatic database seeder that populates initial data if tables are empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final PartRepository partRepository;
    private final VehiclePartRepository vehiclePartRepository;
    private final WarrantyPolicyRepository warrantyPolicyRepository;
    private final ServiceCampaignRepository campaignRepository;
    private final InventoryRepository inventoryRepository;
    private final WarrantyClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      CustomerRepository customerRepository,
                      VehicleRepository vehicleRepository,
                      PartRepository partRepository,
                      VehiclePartRepository vehiclePartRepository,
                      WarrantyPolicyRepository warrantyPolicyRepository,
                      ServiceCampaignRepository campaignRepository,
                      InventoryRepository inventoryRepository,
                      WarrantyClaimRepository claimRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.partRepository = partRepository;
        this.vehiclePartRepository = vehiclePartRepository;
        this.warrantyPolicyRepository = warrantyPolicyRepository;
        this.campaignRepository = campaignRepository;
        this.inventoryRepository = inventoryRepository;
        this.claimRepository = claimRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            seedRoles();
        }
        if (userRepository.count() == 0) {
            seedUsers();
        }
        if (customerRepository.count() == 0) {
            seedCustomers();
        }
        if (warrantyPolicyRepository.count() == 0) {
            seedWarrantyPolicies();
        }
        if (vehicleRepository.count() == 0) {
            seedVehicles();
        }
        if (partRepository.count() == 0) {
            seedPartsAndInventory();
        }
        if (campaignRepository.count() == 0) {
            seedCampaigns();
        }
        if (claimRepository.count() == 0) {
            seedClaims();
        }
    }

    private void seedRoles() {
        roleRepository.save(Role.builder().name("ADMIN").description("System Administrator").build());
        roleRepository.save(Role.builder().name("EVM_STAFF").description("EV Manufacturer Staff").build());
        roleRepository.save(Role.builder().name("SC_STAFF").description("Service Center Staff").build());
        roleRepository.save(Role.builder().name("SC_TECHNICIAN").description("Service Center Technician").build());
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        Role evmRole = roleRepository.findByName("EVM_STAFF").orElse(null);
        Role scRole = roleRepository.findByName("SC_STAFF").orElse(null);
        Role techRole = roleRepository.findByName("SC_TECHNICIAN").orElse(null);

        String encodedPassword = passwordEncoder.encode("password123");

        User admin = User.builder()
                .username("admin")
                .password(encodedPassword)
                .fullName("System Administrator")
                .email("admin@evwarranty.com")
                .phone("+84912345001")
                .active(true)
                .roles(Set.of(adminRole))
                .build();

        User evmStaff = User.builder()
                .username("evmstaff")
                .password(encodedPassword)
                .fullName("Sarah Lee")
                .email("sarah.lee@evmanufacturer.com")
                .phone("+84912345004")
                .active(true)
                .roles(Set.of(evmRole))
                .build();

        User scStaff = User.builder()
                .username("scstaff")
                .password(encodedPassword)
                .fullName("Nguyen Van A")
                .email("staff.hanoi@servicecenter.com")
                .phone("+84912345002")
                .serviceCenter("SC-HANOI-01")
                .active(true)
                .roles(Set.of(scRole))
                .build();

        User scTech = User.builder()
                .username("sctech")
                .password(encodedPassword)
                .fullName("Tran Van B")
                .email("tech.hanoi@servicecenter.com")
                .phone("+84912345003")
                .serviceCenter("SC-HANOI-01")
                .active(true)
                .roles(Set.of(techRole))
                .build();

        User scStaff2 = User.builder()
                .username("scstaff2")
                .password(encodedPassword)
                .fullName("Le Thi C")
                .email("staff.hcm@servicecenter.com")
                .phone("+84912345005")
                .serviceCenter("SC-HCMC-01")
                .active(true)
                .roles(Set.of(scRole))
                .build();

        User scTech2 = User.builder()
                .username("sctech2")
                .password(encodedPassword)
                .fullName("Pham Van D")
                .email("tech.hcm@servicecenter.com")
                .phone("+84912345006")
                .serviceCenter("SC-HCMC-01")
                .active(true)
                .roles(Set.of(techRole))
                .build();

        userRepository.saveAll(Set.of(admin, evmStaff, scStaff, scTech, scStaff2, scTech2));
    }

    private void seedCustomers() {
        Customer c1 = Customer.builder()
                .fullName("Nguyen Van Minh")
                .email("minh.nguyen@gmail.com")
                .phone("+84903123456")
                .address("123 Le Loi Street")
                .city("Ho Chi Minh City")
                .country("Vietnam")
                .build();

        Customer c2 = Customer.builder()
                .fullName("Tran Thi Huong")
                .email("huong.tran@yahoo.com")
                .phone("+84918234567")
                .address("45 Nguyen Hue Boulevard")
                .city("Hanoi")
                .country("Vietnam")
                .build();

        Customer c3 = Customer.builder()
                .fullName("Le Hoang Nam")
                .email("nam.le@outlook.com")
                .phone("+84922345678")
                .address("789 Tran Hung Dao St")
                .city("Da Nang")
                .country("Vietnam")
                .build();

        customerRepository.saveAll(Set.of(c1, c2, c3));
    }

    private void seedWarrantyPolicies() {
        WarrantyPolicy p1 = WarrantyPolicy.builder()
                .name("Standard EV Bumper-to-Bumper")
                .description("Basic warranty coverage for all vehicle components")
                .durationMonths(48)
                .mileageLimit(50000)
                .coverageType(WarrantyPolicy.CoverageType.BUMPER_TO_BUMPER)
                .applicableModels("VF8, VF9, Model 3, Model Y, Ioniq 5")
                .isActive(true)
                .build();

        WarrantyPolicy p2 = WarrantyPolicy.builder()
                .name("High-Voltage Battery & Drive Unit")
                .description("Extended coverage for high-voltage battery pack and electric motor")
                .durationMonths(96)
                .mileageLimit(160000)
                .coverageType(WarrantyPolicy.CoverageType.BATTERY)
                .applicableModels("VF8, VF9, Model 3, Model Y, Ioniq 5")
                .isActive(true)
                .build();

        warrantyPolicyRepository.saveAll(Set.of(p1, p2));
    }

    private void seedVehicles() {
        Customer c1 = customerRepository.findByEmail("minh.nguyen@gmail.com").orElse(null);
        Customer c2 = customerRepository.findByEmail("huong.tran@yahoo.com").orElse(null);

        Vehicle v1 = Vehicle.builder()
                .vin("VF8E3400123456789")
                .model("VF8 Plus")
                .make("VinFast")
                .year(2023)
                .color("Crimson Red")
                .batteryType("CATL Lithium-ion NMC")
                .batteryCapacity(87.7)
                .motorType("Dual Motor AWD (300kW)")
                .mileage(45200)
                .customer(c1)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .build();

        Vehicle v2 = Vehicle.builder()
                .vin("VF9P0000987654321")
                .model("VF9 Eco")
                .make("VinFast")
                .year(2023)
                .color("Jet Black")
                .batteryType("CATL Lithium-ion NMC")
                .batteryCapacity(123.0)
                .motorType("Dual Motor AWD (300kW)")
                .mileage(28500)
                .customer(c2)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .build();

        Vehicle v3 = Vehicle.builder()
                .vin("5YJ3E1EA1NF123456")
                .model("Model 3 Long Range")
                .make("Tesla")
                .year(2022)
                .color("Pearl White")
                .batteryType("Panasonic NCA")
                .batteryCapacity(82.0)
                .motorType("Dual Motor AWD (324kW)")
                .mileage(68000)
                .customer(c1)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .build();

        vehicleRepository.saveAll(Set.of(v1, v2, v3));
    }

    private void seedPartsAndInventory() {
        Part p1 = Part.builder()
                .partNumber("BAT-VF8-87K")
                .name("VinFast 87.7kWh Battery Pack Assembly")
                .description("High-voltage traction battery pack")
                .category(Part.PartCategory.BATTERY)
                .price(new BigDecimal("14500.00"))
                .warrantyMonths(120)
                .manufacturer("CATL / VinES")
                .modelCompatibility("VF8 Plus, VF8 Eco")
                .isActive(true)
                .minStockLevel(2)
                .build();

        Part p2 = Part.builder()
                .partNumber("BMS-HV-MODU")
                .name("High-Voltage Battery Management System (BMS)")
                .description("Central BMS ECU module")
                .category(Part.PartCategory.ELECTRONICS)
                .price(new BigDecimal("1350.00"))
                .warrantyMonths(48)
                .manufacturer("Continental")
                .modelCompatibility("VF8, VF9, Model 3, Model Y")
                .isActive(true)
                .minStockLevel(5)
                .build();

        Part p3 = Part.builder()
                .partNumber("INV-HV-200K")
                .name("High-Voltage SiC Traction Inverter")
                .description("Silicon Carbide power inverter module")
                .category(Part.PartCategory.ELECTRONICS)
                .price(new BigDecimal("3200.00"))
                .warrantyMonths(60)
                .manufacturer("Denso")
                .modelCompatibility("VF8, Model 3, Ioniq 5")
                .isActive(true)
                .minStockLevel(3)
                .build();

        partRepository.saveAll(Set.of(p1, p2, p3));

        Inventory i1 = Inventory.builder()
                .part(p1)
                .serviceCenter("SC-HANOI-01")
                .quantityOnHand(3)
                .quantityReserved(1)
                .reorderPoint(2)
                .location("Battery Bay A-01")
                .build();

        Inventory i2 = Inventory.builder()
                .part(p2)
                .serviceCenter("SC-HANOI-01")
                .quantityOnHand(12)
                .quantityReserved(2)
                .reorderPoint(5)
                .location("Electronics Shelf E-04")
                .build();

        Inventory i3 = Inventory.builder()
                .part(p3)
                .serviceCenter("SC-HCMC-01")
                .quantityOnHand(5)
                .quantityReserved(0)
                .reorderPoint(3)
                .location("Electronics Shelf E-05")
                .build();

        inventoryRepository.saveAll(Set.of(i1, i2, i3));
    }

    private void seedCampaigns() {
        ServiceCampaign sc1 = ServiceCampaign.builder()
                .campaignNumber("SC2024001")
                .title("BMS Firmware & Battery Thermal Calibration Update")
                .description("Software update to optimize thermal management during DC fast-charging")
                .campaignType(ServiceCampaign.CampaignType.SERVICE_BULLETIN)
                .status(ServiceCampaign.CampaignStatus.ACTIVE)
                .severityLevel(ServiceCampaign.SeverityLevel.MEDIUM)
                .affectedModels("VF8 Plus (2023), Model 3 (2022-2023)")
                .remedyDescription("Reflash BMS software to v3.2.1")
                .estimatedRepairTime(1.0)
                .totalAffected(1200)
                .completedCount(485)
                .startDate(LocalDate.of(2024, 1, 15))
                .build();

        ServiceCampaign sc2 = ServiceCampaign.builder()
                .campaignNumber("SC2024002")
                .title("High-Voltage Traction Inverter O-Ring Seal Inspection")
                .description("Safety recall to inspect and replace silicon sealing gaskets")
                .campaignType(ServiceCampaign.CampaignType.RECALL)
                .status(ServiceCampaign.CampaignStatus.ACTIVE)
                .severityLevel(ServiceCampaign.SeverityLevel.HIGH)
                .affectedModels("VF8, VF9, Model Y (2023)")
                .remedyDescription("Inspect inverter housing seal; replace seal gasket")
                .estimatedRepairTime(2.5)
                .totalAffected(650)
                .completedCount(192)
                .startDate(LocalDate.of(2024, 2, 1))
                .build();

        campaignRepository.saveAll(Set.of(sc1, sc2));
    }

    private void seedClaims() {
        Vehicle v1 = vehicleRepository.findByVin("VF8E3400123456789").orElse(null);
        Vehicle v3 = vehicleRepository.findByVin("5YJ3E1EA1NF123456").orElse(null);
        WarrantyPolicy pol1 = warrantyPolicyRepository.findAll().stream().findFirst().orElse(null);
        User scStaff = userRepository.findByUsername("scstaff").orElse(null);
        User scTech = userRepository.findByUsername("sctech").orElse(null);
        User evmStaff = userRepository.findByUsername("evmstaff").orElse(null);

        if (v1 != null && pol1 != null) {
            WarrantyClaim c1 = WarrantyClaim.builder()
                    .claimNumber("CLM2024001")
                    .vehicle(v1)
                    .warrantyPolicy(pol1)
                    .failureDescription("Intermittent battery cell voltage imbalance warning during fast charging")
                    .diagnosisNotes("Diagnostic DTC P0A80 detected. BMS slave module sensor channel #4 outside tolerance")
                    .repairDescription("Replaced BMS controller board module and updated firmware to v3.2.1")
                    .laborHours(3.5)
                    .laborCost(new BigDecimal("175.00"))
                    .partsCost(new BigDecimal("1350.00"))
                    .totalCost(new BigDecimal("1525.00"))
                    .status(WarrantyClaim.ClaimStatus.COMPLETED)
                    .mileageAtClaim(44800)
                    .submittedBy(scStaff)
                    .technician(scTech)
                    .reviewedBy(evmStaff)
                    .serviceCenter("SC-HANOI-01")
                    .submittedAt(LocalDateTime.now().minusDays(10))
                    .reviewedAt(LocalDateTime.now().minusDays(9))
                    .completedAt(LocalDateTime.now().minusDays(8))
                    .build();

            claimRepository.save(c1);
        }

        if (v3 != null && pol1 != null) {
            WarrantyClaim c2 = WarrantyClaim.builder()
                    .claimNumber("CLM2024002")
                    .vehicle(v3)
                    .warrantyPolicy(pol1)
                    .failureDescription("Traction inverter high-temperature warning during highway driving")
                    .diagnosisNotes("Coolant passage flow sensor reporting restricted flow")
                    .repairDescription("Pending replacement of SiC traction inverter assembly")
                    .laborHours(4.0)
                    .laborCost(new BigDecimal("200.00"))
                    .partsCost(new BigDecimal("3200.00"))
                    .totalCost(new BigDecimal("3400.00"))
                    .status(WarrantyClaim.ClaimStatus.APPROVED)
                    .mileageAtClaim(67500)
                    .submittedBy(scStaff)
                    .technician(scTech)
                    .reviewedBy(evmStaff)
                    .serviceCenter("SC-HANOI-01")
                    .submittedAt(LocalDateTime.now().minusDays(5))
                    .reviewedAt(LocalDateTime.now().minusDays(4))
                    .build();

            claimRepository.save(c2);
        }
    }
}
