package com.oem.evwarranty.domain.customer;

import com.oem.evwarranty.domain.vehicle.Vehicle;


import com.oem.evwarranty.domain.customer.Customer;
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
 * Controller for Customer management (Service Center).
 */
@Controller
@RequestMapping("/sc/customers")
@Tag(name = "Customer Management", description = "Operations for managing vehicle owners and customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "List customers", description = "Retrieve a paginated list of customers with search filtering")
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<Customer> customers = customerService.searchCustomers(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        return "sc/customers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "sc/customers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Customer customer,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "sc/customers/form";
        }
        try {
            customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("success", "Customer created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "sc/customers/form";
        }
        return "redirect:/sc/customers";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Customer customer = customerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        model.addAttribute("customer", customer);
        return "sc/customers/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Customer customer = customerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        model.addAttribute("customer", customer);
        return "sc/customers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute Customer customer,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "sc/customers/form";
        }
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            customerService.updateCustomer(id, customer);
            redirectAttributes.addFlashAttribute("success", "Customer updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete customer: " + e.getMessage());
        }
        return "redirect:/sc/customers";
    }
}


