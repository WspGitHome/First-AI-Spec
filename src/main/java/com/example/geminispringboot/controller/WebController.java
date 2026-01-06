package com.example.geminispringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * Maps the root URL ("/") to the `index` view.
     * This allows Thymeleaf to process the `index.html` template.
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
