package com.example.geminispringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for login page.
 * Serves the custom login HTML page.
 */
@Controller
public class LoginController {

    /**
     * Display the login page.
     *
     * @param error optional error parameter from Spring Security on authentication failure
     * @param logout optional logout parameter when user has logged out
     * @param model the model to pass attributes to the view
     * @return the login page view name
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Add error message if authentication failed
        if (error != null) {
            model.addAttribute("errorMessage", "用户名或密码错误");
        }

        // Add logout message if user just logged out
        if (logout != null) {
            model.addAttribute("logoutMessage", "您已成功退出登录");
        }

        return "login";
    }
}
