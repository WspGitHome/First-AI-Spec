package com.example.geminispringboot.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduleParsingService {

    public Map<String, List<String>> parse(List<List<String>> data, List<String> allowedNames, Map<String, String> shiftMappings) {
        Map<String, List<String>> results = new HashMap<>();
        Set<String> validShiftKeys = shiftMappings.keySet();

        for (List<String> row : data) {
            for (int i = 0; i < row.size(); i++) {
                String cellValue = row.get(i);
                if (cellValue == null) {
                    continue;
                }
                String trimmedCell = cellValue.trim();
                if (trimmedCell.isEmpty()) {
                    continue;
                }

                // 1. 检查单元格是否以任何一个班次关键字开头
                String detectedShift = null;
                for (String shiftKey : validShiftKeys) {
                    if (trimmedCell.startsWith(shiftKey)) {
                        detectedShift = shiftKey;
                        break;
                    }
                }

                if (detectedShift != null) {
                    String alias = shiftMappings.get(detectedShift);
                    String newKey = String.format("%s(%s)", detectedShift, alias != null ? alias : "");

                    results.putIfAbsent(newKey, new ArrayList<>());

                    // 2. 如果是班次，则检查该行后续的所有单元格
                    for (int j = i + 1; j < row.size(); j++) {
                        String subsequentCell = row.get(j);
                        if (subsequentCell == null) {
                            continue;
                        }
                        String trimmedSubsequent = subsequentCell.trim();
                        if (trimmedSubsequent.isEmpty()) {
                            continue;
                        }

                        // 检查后续单元格是否是另一个班次，如果是则停止收集
                        boolean isAnotherShift = false;
                        for (String shiftKey : validShiftKeys) {
                            if (trimmedSubsequent.startsWith(shiftKey)) {
                                isAnotherShift = true;
                                break;
                            }
                        }
                        if (isAnotherShift) {
                            break;
                        }

                        // 3. 检查单元格内容是否包含任何允许的人名
                        for (String name : allowedNames) {
                            if (trimmedSubsequent.contains(name)) {
                                results.get(newKey).add(name);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }
}
