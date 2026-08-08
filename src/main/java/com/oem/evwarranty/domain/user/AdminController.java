package com.oem.evwarranty.domain.user;

import com.oem.evwarranty.domain.analytics.ReportService;
import com.oem.evwarranty.domain.audit.AuditLog;
import com.oem.evwarranty.domain.audit.AuditLogService;
import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.campaign.ServiceCampaignService;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimService;
import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.customer.CustomerService;
import com.oem.evwarranty.domain.inventory.Inventory;
import com.oem.evwarranty.domain.inventory.InventoryService;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartService;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure REST Controller for Admin operations (/api/v1/admin/*).
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin REST API", description = "REST APIs for system administration, user management, and global domain administration")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final ServiceCampaignService campaignService;
    private final WarrantyClaimService claimService;
    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final PartService partService;
    private final InventoryService inventoryService;
    private final ReportService reportService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService,
                           RoleRepository roleRepository,
                           ServiceCampaignService campaignService,
                           WarrantyClaimService claimService,
                           VehicleService vehicleService,
                           CustomerService customerService,
                           PartService partService,
                           InventoryService inventoryService,
                           ReportService reportService,
                           AuditLogService auditLogService) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.campaignService = campaignService;
        this.claimService = claimService;
        this.vehicleService = vehicleService;
        this.customerService = customerService;
        this.partService = partService;
        this.inventoryService = inventoryService;
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Admin dashboard statistics", description = "Retrieve global system statistics")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = reportService.getDashboardStats();
        stats.put("claimStats", reportService.getClaimStatsByStatus());
        stats.put("campaignStats", reportService.getCampaignStatsByStatus());
        stats.put("vehicleStats", reportService.getVehicleStatsByStatus());
        return ResponseEntity.ok(stats);
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    @GetMapping("/users")
    @Operation(summary = "List users", description = "Retrieve a paginated list of all system users")
    public ResponseEntity<Page<User>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String search) {
        Page<User> users = userService.searchUsers(
                search != null ? search : "",
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @Operation(summary = "Create user", description = "Create a new system user with specified roles")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user,
                                        @RequestParam(required = false) List<String> roleNames) {
        Set<String> roles = roleNames != null ? new HashSet<>(roleNames) : new HashSet<>();
        User createdUser = userService.createUser(user, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details", description = "Retrieve a user by ID")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user", description = "Update user profile details")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{id}/toggle")
    @Operation(summary = "Toggle user active status", description = "Enable or disable a user account")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "User status toggled successfully"));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user", description = "Delete a user account by ID")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "User deleted successfully"));
    }

    // ==========================================
    // CAMPAIGN MANAGEMENT (/api/v1/admin/campaigns)
    // ==========================================

    @GetMapping("/campaigns")
    @Operation(summary = "List campaigns", description = "Retrieve paginated service campaigns")
    public ResponseEntity<Page<ServiceCampaign>> listCampaigns(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false) String search) {
        Page<ServiceCampaign> campaigns = campaignService.searchCampaigns(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(campaigns);
    }

    @PostMapping("/campaigns")
    @Operation(summary = "Create campaign", description = "Create a new service campaign / recall")
    public ResponseEntity<ServiceCampaign> createCampaign(@Valid @RequestBody ServiceCampaign campaign, Authentication auth) {
        Long createdById = userService.findByUsername(auth.getName()).map(User::getId).orElse(null);
        ServiceCampaign created = campaignService.createCampaign(campaign, createdById);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/campaigns/{id}")
    @Operation(summary = "Get campaign details", description = "Retrieve a campaign by ID")
    public ResponseEntity<ServiceCampaign> getCampaign(@PathVariable Long id) {
        ServiceCampaign campaign = campaignService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        return ResponseEntity.ok(campaign);
    }

    @PutMapping("/campaigns/{id}")
    @Operation(summary = "Update campaign", description = "Update an existing campaign")
    public ResponseEntity<ServiceCampaign> updateCampaign(@PathVariable Long id, @Valid @RequestBody ServiceCampaign campaign) {
        ServiceCampaign updated = campaignService.updateCampaign(id, campaign);
        return ResponseEntity.ok(updated);
    }

    // ==========================================
    // CLAIMS MANAGEMENT (/api/v1/admin/claims)
    // ==========================================

    @GetMapping("/claims")
    @Operation(summary = "List warranty claims", description = "Retrieve paginated warranty claims")
    public ResponseEntity<Page<WarrantyClaim>> listClaims(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Page<WarrantyClaim> claims = claimService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/claims/{id}")
    @Operation(summary = "Get claim details", description = "Retrieve a warranty claim by ID")
    public ResponseEntity<WarrantyClaim> getClaim(@PathVariable Long id) {
        WarrantyClaim claim = claimService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        return ResponseEntity.ok(claim);
    }

    @PostMapping("/claims/{id}/approve")
    @Operation(summary = "Approve warranty claim", description = "Approve a claim by ID")
    public ResponseEntity<WarrantyClaim> approveClaim(@PathVariable Long id, Authentication auth) {
        Long reviewerId = userService.findByUsername(auth.getName()).map(User::getId).orElse(null);
        WarrantyClaim claim = claimService.approveClaim(id, reviewerId);
        return ResponseEntity.ok(claim);
    }

    @PostMapping("/claims/{id}/reject")
    @Operation(summary = "Reject warranty claim", description = "Reject a claim by ID with reason")
    public ResponseEntity<WarrantyClaim> rejectClaim(@PathVariable Long id, @RequestParam String rejectionReason, Authentication auth) {
        Long reviewerId = userService.findByUsername(auth.getName()).map(User::getId).orElse(null);
        WarrantyClaim claim = claimService.rejectClaim(id, reviewerId, rejectionReason);
        return ResponseEntity.ok(claim);
    }

    // ==========================================
    // VEHICLE MANAGEMENT (/api/v1/admin/vehicles)
    // ==========================================

    @GetMapping("/vehicles")
    @Operation(summary = "List vehicles", description = "Retrieve paginated vehicles")
    public ResponseEntity<Page<Vehicle>> listVehicles(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) String search) {
        Page<Vehicle> vehicles = vehicleService.searchVehicles(
                search != null ? search : "",
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/{id}")
    @Operation(summary = "Get vehicle details", description = "Retrieve vehicle details by ID")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        return ResponseEntity.ok(vehicle);
    }

    // ==========================================
    // CUSTOMER MANAGEMENT (/api/v1/admin/customers)
    // ==========================================

    @GetMapping("/customers")
    @Operation(summary = "List customers", description = "Retrieve paginated customers")
    public ResponseEntity<Page<Customer>> listCustomers(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) String search) {
        Page<Customer> customers = customerService.searchCustomers(
                search != null ? search : "",
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(customers);
    }

    // ==========================================
    // PARTS & INVENTORY MANAGEMENT (/api/v1/admin/parts, /api/v1/admin/inventory)
    // ==========================================

    @GetMapping("/parts")
    @Operation(summary = "List parts catalog", description = "Retrieve master parts catalog")
    public ResponseEntity<Page<Part>> listParts(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String search) {
        Page<Part> parts = partService.searchParts(search, PageRequest.of(page, size, Sort.by("name")));
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/inventory")
    @Operation(summary = "List inventory stock", description = "Retrieve all service center inventory stock")
    public ResponseEntity<List<Inventory>> listInventory() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    // ==========================================
    // AUDIT LOGS (/api/v1/admin/audit-logs)
    // ==========================================

    @GetMapping("/audit-logs")
    @Operation(summary = "List system audit logs", description = "Retrieve paginated system audit logs")
    public ResponseEntity<Page<AuditLog>> listAuditLogs(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> auditLogs = auditLogService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(auditLogs);
    }
}
