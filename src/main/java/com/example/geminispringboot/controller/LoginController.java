package com.example.geminispringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession; // Import HttpSession

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
     * @param request HttpServletRequest to retrieve specific error message
     * @return the login page view name
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            HttpServletRequest request) {

        if (error != null) {
            HttpSession session = request.getSession(false);
            String specificErrorMessage = null;
            if (session != null) {
                specificErrorMessage = (String) session.getAttribute("loginErrorSpecificMessage");
                session.removeAttribute("loginErrorSpecificMessage"); // Clean up session
            }

            if (specificErrorMessage != null) {
                model.addAttribute("errorMessage", specificErrorMessage);
            } else {
                model.addAttribute("errorMessage", "用户名或密码错误"); // Fallback generic message
            }
        }

        // Add logout message if user just logged out
        if (logout != null) {
            model.addAttribute("logoutMessage", "您已成功退出登录");
        }

        return "login";
    }
}
