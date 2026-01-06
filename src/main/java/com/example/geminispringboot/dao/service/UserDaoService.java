package com.example.geminispringboot.dao.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.geminispringboot.dao.UserDao;
import com.example.geminispringboot.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserDaoService extends ServiceImpl<UserDao, User> {
}
