package com.example.geminispringboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.geminispringboot.dao.service.UserDaoService;
import com.example.geminispringboot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserDaoService userDaoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String registerUser(@RequestBody Map<String, String> payload) {
        User newUser = new User();
        newUser.setUsername(payload.get("username"));
        newUser.setPassword(passwordEncoder.encode(payload.get("password")));
        newUser.setRoles("ROLE_USER");
        userDaoService.save(newUser);
        return "User registered successfully";
    }

    @GetMapping("/profile")
    public User getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userDaoService.getOne(new QueryWrapper<User>().eq("username", username));
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestBody Map<String, String> payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDaoService.getOne(new QueryWrapper<User>().eq("username", username));

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userDaoService.updateById(user);
            return "Password changed successfully";
        } else {
            return "Old password is not correct";
        }
    }
}
