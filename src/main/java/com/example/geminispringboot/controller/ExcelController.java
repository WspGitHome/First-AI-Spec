package com.example.geminispringboot.controller;

import com.example.geminispringboot.config.AppProperties;
import com.example.geminispringboot.service.ExcelService;
import com.example.geminispringboot.service.ScheduleParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ScheduleParsingService parsingService;

    @Autowired // Autoinject AppProperties
    private AppProperties appProperties;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, List<String>>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 从Excel读取原始数据
            List<List<String>> rawData = excelService.readExcelData(file);
            // 2. 解析数据, 使用AppProperties中的静态配置
            Map<String, List<String>> parsedData = parsingService.parse(rawData, appProperties.getAllowedNames(), appProperties.getMappings());
            // 3. 返回解析结果
            return ResponseEntity.ok(parsedData);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
