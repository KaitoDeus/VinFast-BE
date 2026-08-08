package com.oem.evwarranty.common.config;

import com.oem.evwarranty.domain.user.Role;
import com.oem.evwarranty.domain.inventory.Inventory;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global controller advice to populate common navigation and role attributes for views.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("userRoles")
    public Set<String> userRoles(Authentication auth) {
        if (auth == null) {
            return Collections.emptySet();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    @ModelAttribute
    public void addNavigationAttributes(Model model, HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.equals("/") || uri.startsWith("/dashboard")) {
            model.addAttribute("currentSection", "dashboard");
        } else if (uri.startsWith("/sc")) {
            model.addAttribute("currentSection", "sc");
            if (uri.contains("/vehicles")) {
                model.addAttribute("currentPage", "vehicles");
            } else if (uri.contains("/customers")) {
                model.addAttribute("currentPage", "customers");
            } else if (uri.contains("/claims")) {
                model.addAttribute("currentPage", "warranty-claims");
            } else if (uri.contains("/inventory")) {
                model.addAttribute("currentPage", "inventory");
            }
        } else if (uri.startsWith("/evm")) {
            model.addAttribute("currentSection", "evm");
            if (uri.contains("/claims")) {
                model.addAttribute("currentPage", "claims");
            } else if (uri.contains("/parts")) {
                model.addAttribute("currentPage", "parts");
            } else if (uri.contains("/campaigns")) {
                model.addAttribute("currentPage", "campaigns");
            }
        } else if (uri.startsWith("/admin")) {
            model.addAttribute("currentSection", "admin");
            if (uri.contains("/users")) {
                model.addAttribute("currentPage", "users");
            }
        }
    }
}



