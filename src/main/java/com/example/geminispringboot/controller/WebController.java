package com.example.geminispringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * 将根路径的请求转发到静态的 index.html 文件。
     * 使用 "forward:" 可以确保在任何 context-path 下都能正确工作。
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
