package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.customer.Customer;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleService;
import com.oem.evwarranty.domain.customer.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for Vehicle management (Service Center).
 */
@Controller
@RequestMapping("/sc/vehicles")
@Tag(name = "Vehicle Management", description = "Operations for registering and managing electric vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final CustomerService customerService;

    public VehicleController(VehicleService vehicleService, CustomerService customerService) {
        this.vehicleService = vehicleService;
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "List vehicles", description = "View a paginated list of all vehicles in the system")
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<Vehicle> vehicles = vehicleService.searchVehicles(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("search", search);
        return "sc/vehicles/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("statuses", Vehicle.VehicleStatus.values());
        return "sc/vehicles/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Vehicle vehicle,
            BindingResult result,
            @RequestParam(required = false) Long customerId,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("statuses", Vehicle.VehicleStatus.values());
            return "sc/vehicles/form";
        }
        try {
            vehicleService.createVehicle(vehicle, customerId);
            redirectAttributes.addFlashAttribute("success", "Vehicle registered successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("statuses", Vehicle.VehicleStatus.values());
            return "sc/vehicles/form";
        }
        return "redirect:/sc/vehicles";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Vehicle vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("extraCss", "/css/pages/ai-prediction.css");
        model.addAttribute("extraJs", "/js/ai-prediction.js");
        return "sc/vehicles/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Vehicle vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("statuses", Vehicle.VehicleStatus.values());
        return "sc/vehicles/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute Vehicle vehicle,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("statuses", Vehicle.VehicleStatus.values());
            return "sc/vehicles/form";
        }
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            vehicleService.updateVehicle(id, vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/vehicles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            vehicleService.deleteVehicle(id);
            redirectAttributes.addFlashAttribute("success", "Vehicle deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete vehicle: " + e.getMessage());
        }
        return "redirect:/sc/vehicles";
    }

    @GetMapping("/search")
    @ResponseBody
    @Operation(summary = "Search by VIN API", description = "API endpoint to find vehicle details by VIN")
    public Vehicle searchByVin(@RequestParam String vin) {
        return vehicleService.findByVin(vin).orElse(null);
    }
}




