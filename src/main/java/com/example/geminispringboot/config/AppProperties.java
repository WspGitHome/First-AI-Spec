package com.example.geminispringboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> allowedNames = new ArrayList<>();
    private final Map<String, String> mappings;
    private String testProperty;

    public AppProperties() {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("小夜", "上");
        tempMap.put("白班", "白");
        tempMap.put("大夜", "下");
        tempMap.put("夜班", "夜");
        tempMap.put("42054", "乘");
        tempMap.put("42051", "乘");
        this.mappings = Collections.unmodifiableMap(tempMap);
    }

    public List<String> getAllowedNames() {
        return allowedNames;
    }

    public void setAllowedNames(List<String> allowedNames) {
        this.allowedNames = allowedNames;
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    // No setter for mappings as it's hardcoded

    public String getTestProperty() {
        return testProperty;
    }

    public void setTestProperty(String testProperty) {
        this.testProperty = testProperty;
    }
}
