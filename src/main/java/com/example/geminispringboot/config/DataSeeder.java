package com.example.geminispringboot.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.geminispringboot.dao.service.UserDaoService;
import com.example.geminispringboot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    @Autowired
    private UserDaoService userDaoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userDaoService.count(new QueryWrapper<User>().eq("username", "admin")) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRoles("ROLE_ADMIN,ROLE_USER");
            userDaoService.save(admin);
        }
    }
}
