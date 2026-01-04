package com.example.geminispringboot.controller;

import com.example.geminispringboot.model.ProcessingResult;
import com.example.geminispringboot.service.OrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class OrchestrationController {

    @Autowired
    private OrchestrationService orchestrationService;

    // 使用一个简单的Map作为内存缓存来存储处理结果
    private static final Map<String, ProcessingResult> resultCache = new ConcurrentHashMap<>();

    @PostMapping("/process-roster")
    public ResponseEntity<?> processRoster(
            @RequestParam("dutyRosterFiles") List<MultipartFile> dutyRosterFiles,
            @RequestParam("attendanceFile") MultipartFile attendanceFile,
            @RequestParam("days") List<Integer> days) {

        try {
            ProcessingResult result = orchestrationService.processFiles(dutyRosterFiles, attendanceFile, days);
            String transactionId = UUID.randomUUID().toString();
            resultCache.put(transactionId, result);

            // 使用 Java 8 兼容的方式创建 Map
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("transactionId", transactionId);
            responseBody.put("logs", result.getLogs());

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("处理失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{transactionId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String transactionId) {
        ProcessingResult result = resultCache.get(transactionId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileContent = result.getFileContent();
        String originalFilename = result.getOriginalFilename();

        ByteArrayResource resource = new ByteArrayResource(fileContent);

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(originalFilename, StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        // 成功获取后可以从缓存中移除，避免内存一直增长
        resultCache.remove(transactionId);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileContent.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
