package com.booklovers.app.controller;

import com.booklovers.app.dto.RegisterRequest;
import com.booklovers.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthWebController {

    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.registerUser(request);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}