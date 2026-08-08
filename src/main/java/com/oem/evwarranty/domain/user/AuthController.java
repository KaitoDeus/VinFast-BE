package com.oem.evwarranty.domain.user;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for authentication pages.
 */
@Controller
@Tag(name = "Authentication", description = "Endpoints for user login and access control")
public class AuthController {

    @GetMapping("/login")
    @Operation(summary = "Login page", description = "Standard login form for all system users")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound() {
        return "error/404";
    }

    @GetMapping("/error/500")
    public String serverError() {
        return "error/500";
    }

    @GetMapping("/error/400")
    public String badRequest() {
        return "error/400";
    }
}


