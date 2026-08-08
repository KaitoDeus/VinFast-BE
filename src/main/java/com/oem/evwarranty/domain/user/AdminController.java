package com.oem.evwarranty.domain.user;

import com.oem.evwarranty.domain.audit.AuditLogService;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.user.UserService;
import com.oem.evwarranty.domain.user.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for Admin operations (User management, system settings).
 */
@Controller
@RequestMapping("/admin")
@Tag(name = "Admin Operations", description = "System administration, user management, and security settings")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String dashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping("/users")
    @Operation(summary = "List users", description = "Retrieve a paginated list of all system users with search functionality")
    public String listUsers(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<User> users = userService.searchUsers(
                search != null ? search : "",
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/users/form";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute User user,
            BindingResult result,
            @RequestParam(required = false) List<String> roleNames,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/users/form";
        }
        try {
            Set<String> roles = roleNames != null ? new HashSet<>(roleNames) : new HashSet<>();
            userService.createUser(user, roles);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/users/form";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        return "admin/users/view";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/users/form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
            @Valid @ModelAttribute User user,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/users/form";
        }
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("success", "User status updated");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            userService.updatePassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password reset successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        return "redirect:/admin/users";
    }
}



