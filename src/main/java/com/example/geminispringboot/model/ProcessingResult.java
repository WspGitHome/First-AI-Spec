package com.example.geminispringboot.model;

import java.util.List;

public class ProcessingResult {

    private final List<String> logs;
    private final byte[] fileContent;
    private final String originalFilename;

    public ProcessingResult(List<String> logs, byte[] fileContent, String originalFilename) {
        this.logs = logs;
        this.fileContent = fileContent;
        this.originalFilename = originalFilename;
    }

    public List<String> getLogs() {
        return logs;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }
}
