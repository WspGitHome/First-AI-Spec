package com.example.geminispringboot.service;

import com.example.geminispringboot.config.AppProperties;
import com.example.geminispringboot.model.ProcessingResult;
import com.example.geminispringboot.model.UpdateAttendanceRequest;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OrchestrationService {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ScheduleParsingService scheduleParsingService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AppProperties appProperties;

    // Private inner class to hold file-day pairs for sorting
    private static class RosterInput {
        final MultipartFile file;
        final int day;

        RosterInput(MultipartFile file, int day) {
            this.file = file;
            this.day = day;
        }

        int getDay() {
            return day;
        }

        MultipartFile getFile() {
            return file;
        }
    }

    public ProcessingResult processFiles(List<MultipartFile> dutyRosterFiles, MultipartFile attendanceFile, List<Integer> days) throws IOException {
        List<String> logs = new ArrayList<>();
        logs.add("--- 开始处理多个文件 (按日期排序) ---");

        if (dutyRosterFiles.size() != days.size()) {
            throw new IllegalArgumentException("值班表文件数量与日期数量不匹配。");
        }

        Workbook attendanceWorkbook = null;
        try (InputStream attendanceStream = attendanceFile.getInputStream()) {
            // 步骤 1: 将唯一的考勤表直接读入内存
            logs.add("步骤 1/5: 正在将考勤表加载到内存: " + attendanceFile.getOriginalFilename());
            attendanceWorkbook = WorkbookFactory.create(attendanceStream);
            logs.add("考勤表加载成功。");

            // 步骤 2: 从考勤表中动态提取员工姓名列表和获取最后一天
            logs.add("步骤 2/5: 正在从考勤表提取基础信息...");
            List<String> allEmployeeNames = attendanceService.extractEmployeeNames(attendanceWorkbook);
            int lastDayOfMonth = attendanceService.getLastDayOfMonth(attendanceWorkbook);
            logs.add("姓名列表提取完成 (共 " + allEmployeeNames.size() + " 人)，本月最后一天是: " + lastDayOfMonth);

            // 将文件和日期配对并按日期排序
            List<RosterInput> rosterInputs = IntStream.range(0, dutyRosterFiles.size())
                    .mapToObj(i -> new RosterInput(dutyRosterFiles.get(i), days.get(i)))
                    .sorted(Comparator.comparingInt(RosterInput::getDay))
                    .collect(Collectors.toList());

            logs.add("值班表已按日期从小到大排序。");

            // 步骤 3: 循环解析所有值班表并收集更新
            logs.add("步骤 3/5: 正在按排序后的顺序解析所有值班表并收集考勤更新...");
            List<UpdateAttendanceRequest> allStandardUpdates = new ArrayList<>();
            Map<Integer, Set<String>> rosteredNamesByDay = new HashMap<>();
            Pattern pattern = Pattern.compile(".*\\((.*)\\)");

            for (RosterInput rosterInput : rosterInputs) {
                MultipartFile dutyRosterFile = rosterInput.getFile();
                int day = rosterInput.getDay();
                logs.add("  -> 正在处理: " + dutyRosterFile.getOriginalFilename() + " (对应日期: " + day + "日)");

                List<List<String>> dutyRosterData = excelService.readExcelData(dutyRosterFile);
                Map<String, List<String>> parsedDutyRoster = scheduleParsingService.parse(dutyRosterData, allEmployeeNames, appProperties.getMappings());

                Map<String, Integer> chengShiftCount = new HashMap<>(); // "乘"班计数器，每个文件独立

                for (Map.Entry<String, List<String>> entry : parsedDutyRoster.entrySet()) {
                    String key = entry.getKey();
                    List<String> names = entry.getValue();

                    // 记录当天所有已排班的人员
                    rosteredNamesByDay.computeIfAbsent(day, k -> new HashSet<>()).addAll(names);

                    Matcher matcher = pattern.matcher(key);
                    if (matcher.find()) {
                        String shift = matcher.group(1);
                        logs.add("    - 处理班次: '" + shift + "'，包含 " + names.size() + " 人");

                        for (String name : names) {
                            UpdateAttendanceRequest request = new UpdateAttendanceRequest();
                            request.setName(name);
                            request.setShift(shift);

                            if ("乘".equals(shift)) {
                                int count = chengShiftCount.getOrDefault(name, 0);
                                int targetDay = (count == 0) ? day : day + 1;
                                if (targetDay <= lastDayOfMonth) {
                                    request.setDay(targetDay);
                                    allStandardUpdates.add(request);
                                    logs.add("      - [收集] 更新 " + name + " 第 " + targetDay + " 天为: " + shift);
                                } else {
                                    logs.add("      - **跳过**: " + name + " 第 " + targetDay + " 天的更新，超出本月范围。");
                                }
                                chengShiftCount.put(name, count + 1);
                            } else if ("下".equals(shift)) {
                                int targetDay = day + 1;
                                if (targetDay <= lastDayOfMonth) {
                                    request.setDay(targetDay);
                                    allStandardUpdates.add(request);
                                    logs.add("      - [收集] 更新 " + name + " 第 " + targetDay + " 天为: " + shift);
                                } else {
                                    logs.add("      - **跳过**: " + name + " 第 " + targetDay + " 天的更新，超出本月范围。");
                                }
                            } else {
                                request.setDay(day);
                                allStandardUpdates.add(request);
                                logs.add("      - [收集] 更新 " + name + " 第 " + day + " 天为: " + shift);
                            }
                        }
                    }
                }
            }
            logs.add("所有值班表解析完成。");

            // 步骤 4: 收集所有“休”的更新请求
            logs.add("步骤 4/5: 正在为所有涉及的日期收集'休'假更新...");
            List<UpdateAttendanceRequest> allIfEmptyUpdates = new ArrayList<>();
            for (Map.Entry<Integer, Set<String>> entry : rosteredNamesByDay.entrySet()) {
                int day = entry.getKey();
                Set<String> rosteredNamesForDay = entry.getValue();
                logs.add("  -> 检查日期: " + day + "日，当天已排班 " + rosteredNamesForDay.size() + " 人");

                for (String name : allEmployeeNames) {
                    if (!rosteredNamesForDay.contains(name)) {
                        UpdateAttendanceRequest request = new UpdateAttendanceRequest();
                        request.setName(name);
                        request.setDay(day);
                        request.setShift("休");
                        allIfEmptyUpdates.add(request);
                        logs.add("    - [收集] 为未排班员工 " + name + " 在第 " + day + " 天添加 '休' (如果单元格为空)");
                    }
                }
            }
            logs.add("考勤更新收集完成。");

            // 步骤 5: 一次性批量更新内存中的考勤表
            logs.add("步骤 5/5: 开始在内存中批量更新考勤表...");
            attendanceService.batchUpdateAttendance(attendanceWorkbook, allStandardUpdates, allIfEmptyUpdates);
            logs.add("批量更新完成。");

            // 将内存中的工作簿写入字节数组以便返回
            logs.add("正在生成最终文件...");
            byte[] fileContent;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                attendanceWorkbook.write(baos);
                fileContent = baos.toByteArray();
            }
            logs.add("--- 文件处理成功结束 ---");

            return new ProcessingResult(logs, fileContent, attendanceFile.getOriginalFilename());

        } finally {
            // 确保工作簿在处理结束时关闭以释放内存
            if (attendanceWorkbook != null) {
                try {
                    attendanceWorkbook.close();
                } catch (IOException e) {
                    logs.add("关闭内存中的工作簿时出错: " + e.getMessage());
                }
            }
        }
    }
}
