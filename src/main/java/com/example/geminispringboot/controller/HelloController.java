package com.example.geminispringboot.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.geminispringboot.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class HelloController {

    @Autowired
    FileUploadService fileUploadService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Gemini! Current time is: " + DateUtil.now();
    }


    @PostMapping("/upload/file")
    public String index(String uploadUrl, MultipartFile file,
                        String enterpriseCode,
                        String signature,
                        String timestam) {
        Map<String, Object> stringObjectMap = fileUploadService.uploadFile(uploadUrl, file, enterpriseCode, signature, timestam);
        return JSONUtil.toJsonStr(stringObjectMap);

    }
}
