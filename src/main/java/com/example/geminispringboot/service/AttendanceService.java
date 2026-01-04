package com.example.geminispringboot.service;

import cn.hutool.core.util.StrUtil;
import com.example.geminispringboot.model.UpdateAttendanceRequest;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList; // Added this import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AttendanceService {

    /**
     * [OPTIMIZED] Overloaded method to get the last day of the month from an in-memory Workbook.
     */
    public int getLastDayOfMonth(Workbook workbook) throws IOException {
        Sheet sheet = workbook.getSheetAt(0);
        int dateHeaderRowIndex = findRowContaining(sheet, "日     期", 0, 5);
        if (dateHeaderRowIndex == -1) throw new IOException("文件中找不到日期表头行(含'日     期')");

        int dayNumbersRowIndex = findRowContainingNumber(sheet, 1, dateHeaderRowIndex + 1, dateHeaderRowIndex + 3);
        if (dayNumbersRowIndex == -1) throw new IOException("在'日     期'行下方找不到包含数字'1'的日期行");

        Row dayNumbersRow = sheet.getRow(dayNumbersRowIndex);
        if (dayNumbersRow != null) {
            int lastCellNum = dayNumbersRow.getLastCellNum();
            for (int i = lastCellNum - 1; i >= 0; i--) {
                Cell cell = dayNumbersRow.getCell(i);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    return (int) cell.getNumericCellValue();
                }
            }
        }
        throw new IOException("无法确定月份的最后一天");
    }

    /**
     * [DEPRECATED - for reference] Original method reading from a file path.
     */
    public int getLastDayOfMonth(String filePath) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(filePath))) {
            return getLastDayOfMonth(workbook);
        }
    }

    /**
     * [OPTIMIZED] Performs all attendance updates in memory on the provided Workbook object.
     *
     * @param workbook         The workbook to update.
     * @param standardUpdates  A list of updates to be applied unconditionally.
     * @param ifEmptyUpdates   A list of updates to be applied only if the cell is blank.
     * @return Returns true if any cell was modified.
     */
    public boolean batchUpdateAttendance(Workbook workbook, List<UpdateAttendanceRequest> standardUpdates, List<UpdateAttendanceRequest> ifEmptyUpdates) throws IOException {
        Sheet sheet = workbook.getSheetAt(0);
        boolean hasUpdates = false;

        // --- Find header and date row/column indexes once ---
        int employeeHeaderRowIndex = findRowContaining(sheet, "姓名", 0, 5);
        if (employeeHeaderRowIndex == -1) throw new IOException("文件中找不到员工信息表头行(含'姓名')");

        int dateHeaderRowIndex = findRowContaining(sheet, "日     期", 0, 5);
        if (dateHeaderRowIndex == -1) throw new IOException("文件中找不到日期表头行(含'日     期')");

        int nameColumnIndex = findColumnIndex(sheet, "姓名", employeeHeaderRowIndex);
        if (nameColumnIndex == -1) throw new IOException("文件中找不到'姓名'列");

        int dayNumbersRowIndex = findRowContainingNumber(sheet, 1, dateHeaderRowIndex + 1, dateHeaderRowIndex + 3);
        if (dayNumbersRowIndex == -1) throw new IOException("在'日     期'行下方找不到包含数字'1'的日期行");

        int employeeDataStartRow = employeeHeaderRowIndex + 2;

        // --- Caches to avoid repeated lookups ---
        Map<String, Integer> nameRowCache = new HashMap<>();
        Map<Integer, Integer> dayColCache = new HashMap<>();

        // --- Process standard updates ---
        for (UpdateAttendanceRequest request : standardUpdates) {
            boolean updated = updateCell(sheet, request, nameColumnIndex, dayNumbersRowIndex, employeeDataStartRow, nameRowCache, dayColCache, false);
            if (updated) {
                hasUpdates = true;
            }
        }

        // --- Process "update if empty" updates ---
        for (UpdateAttendanceRequest request : ifEmptyUpdates) {
            boolean updated = updateCell(sheet, request, nameColumnIndex, dayNumbersRowIndex, employeeDataStartRow, nameRowCache, dayColCache, true);
            if (updated) {
                hasUpdates = true;
            }
        }

        // --- If any changes were made, re-evaluate all formulas once at the end ---
        if (hasUpdates) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();
        }

        return hasUpdates;
    }

    /**
     * [OPTIMIZED-INTERNAL] Private helper to update a single cell in memory.
     */
    private boolean updateCell(Sheet sheet, UpdateAttendanceRequest request, int nameColumnIndex, int dayNumbersRowIndex, int employeeDataStartRow, Map<String, Integer> nameRowCache, Map<Integer, Integer> dayColCache, boolean ifEmpty) throws IOException {
        // Find row index for the employee name (with cache)
        int targetRowIndex = nameRowCache.computeIfAbsent(request.getName(),
                name -> findRowIndexByCellValue(sheet, nameColumnIndex, name, employeeDataStartRow));

        // Find column index for the day (with cache)
        int targetColumnIndex = dayColCache.computeIfAbsent(request.getDay(),
                day -> findColumnIndexByCellValue(sheet, dayNumbersRowIndex, day));

        if (targetRowIndex == -1) {
            System.out.println("警告: 在考勤表中未找到员工 '" + request.getName() + "'，跳过更新。");
            return false;
        }
        if (targetColumnIndex == -1) {
            // This should ideally not happen if the day is validated before, but as a safeguard:
            throw new IOException("在考勤表中未找到日期: " + request.getDay());
        }

        Row targetRow = sheet.getRow(targetRowIndex);
        if (targetRow == null) targetRow = sheet.createRow(targetRowIndex);

        Cell targetCell = targetRow.getCell(targetColumnIndex);

        if (ifEmpty) {
            // Logic for "update only if empty"
            if (targetCell == null || targetCell.getCellType() == CellType.BLANK ||
                    (targetCell.getCellType() == CellType.STRING && StrUtil.isBlank(targetCell.getStringCellValue()))) {
                if (targetCell == null) {
                    targetCell = targetRow.createCell(targetColumnIndex);
                }
                targetCell.setCellValue(request.getShift());
                return true; // Cell was updated
            }
            return false; // Cell was not empty, so no update
        } else {
            // Logic for unconditional update
            if (targetCell == null) targetCell = targetRow.createCell(targetColumnIndex);
            targetCell.setCellValue(request.getShift());
            return true; // Cell was updated
        }
    }


    // ============================================================================================
    // LEGACY FILE-BASED METHODS (kept for reference or other potential uses, but not for batch)
    // ============================================================================================

    public void updateAttendance(UpdateAttendanceRequest request, String filePath) throws IOException {
        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = WorkbookFactory.create(fis);
        } catch (Exception e) {
            throw new IOException("读取Excel文件失败: " + e.getMessage(), e);
        }

        try {
            Sheet sheet = workbook.getSheetAt(0);
            // This logic is now inside batchUpdateAttendance and updateCell helpers
            // For simplicity, we'll just call a non-batch-optimized version here
            // In a real scenario, you might refactor this to reuse the new helpers
            int employeeHeaderRowIndex = findRowContaining(sheet, "姓名", 0, 5);
            int dateHeaderRowIndex = findRowContaining(sheet, "日     期", 0, 5);
            if (employeeHeaderRowIndex == -1) throw new IOException("文件中找不到员工信息表头行(含'姓名')");
            if (dateHeaderRowIndex == -1) throw new IOException("文件中找不到日期表头行(含'日     期')");
            int nameColumnIndex = findColumnIndex(sheet, "姓名", employeeHeaderRowIndex);
            int dayNumbersRowIndex = findRowContainingNumber(sheet, 1, dateHeaderRowIndex + 1, dateHeaderRowIndex + 3);
            if (dayNumbersRowIndex == -1) throw new IOException("在'日     期'行下方找不到包含数字'1'的日期行");
            if (nameColumnIndex == -1) throw new IOException("文件中找不到'姓名'列");
            int targetRowIndex = findRowIndexByCellValue(sheet, nameColumnIndex, request.getName(), employeeHeaderRowIndex + 2);
            int targetColumnIndex = findColumnIndexByCellValue(sheet, dayNumbersRowIndex, request.getDay());
            if (targetRowIndex == -1) throw new IOException("未找到员工: " + request.getName());
            if (targetColumnIndex == -1) throw new IOException("未找到日期: " + request.getDay());
            Row targetRow = sheet.getRow(targetRowIndex);
if (targetRow == null) targetRow = sheet.createRow(targetRowIndex);
            Cell targetCell = targetRow.getCell(targetColumnIndex);
            if (targetCell == null) targetCell = targetRow.createCell(targetColumnIndex);
            targetCell.setCellValue(request.getShift());

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            throw new IOException("修改或写回Excel文件时失败: " + e.getMessage(), e);
        } finally {
            workbook.close();
        }
    }

    public boolean updateCellIfEmpty(UpdateAttendanceRequest request, String filePath) throws IOException {
        // This method also performs full read/write for a single operation.
        // It's inefficient and should be replaced by the batch method in high-performance contexts.
        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = WorkbookFactory.create(fis);
        } catch (Exception e) {
            throw new IOException("读取Excel文件失败: " + e.getMessage(), e);
        }

        boolean updated = false;
        try {
            Sheet sheet = workbook.getSheetAt(0);
            // Replicating logic similar to the non-optimized path
            int employeeHeaderRowIndex = findRowContaining(sheet, "姓名", 0, 5);
            int dateHeaderRowIndex = findRowContaining(sheet, "日     期", 0, 5);
            if (employeeHeaderRowIndex == -1) throw new IOException("文件中找不到员工信息表头行(含'姓名')");
            if (dateHeaderRowIndex == -1) throw new IOException("文件中找不到日期表头行(含'日     期')");
            int nameColumnIndex = findColumnIndex(sheet, "姓名", employeeHeaderRowIndex);
            int dayNumbersRowIndex = findRowContainingNumber(sheet, 1, dateHeaderRowIndex + 1, dateHeaderRowIndex + 3);
            if (dayNumbersRowIndex == -1) throw new IOException("在'日     期'行下方找不到包含数字'1'的日期行");
            if (nameColumnIndex == -1) throw new IOException("文件中找不到'姓名'列");
            int targetRowIndex = findRowIndexByCellValue(sheet, nameColumnIndex, request.getName(), employeeHeaderRowIndex + 2);
            int targetColumnIndex = findColumnIndexByCellValue(sheet, dayNumbersRowIndex, request.getDay());

            if (targetRowIndex == -1) {
                System.out.println("未找到员工: " + request.getName() + "，跳过更新。");
                return false;
            }
            if (targetColumnIndex == -1) throw new IOException("未找到日期: " + request.getDay());

            Row targetRow = sheet.getRow(targetRowIndex);
            if (targetRow == null) targetRow = sheet.createRow(targetRowIndex);

            Cell targetCell = targetRow.getCell(targetColumnIndex);
            if (targetCell == null || targetCell.getCellType() == CellType.BLANK ||
                    (targetCell.getCellType() == CellType.STRING && StrUtil.isBlank(targetCell.getStringCellValue()))) {
                if (targetCell == null) {
                    targetCell = targetRow.createCell(targetColumnIndex);
                }
                targetCell.setCellValue(request.getShift());
                updated = true;
            }

            if (updated) {
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                evaluator.evaluateAll();

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                }
            }
        } catch (Exception e) {
            throw new IOException("修改或写回Excel文件时失败: " + e.getMessage(), e);
        } finally {
            workbook.close();
        }
        return updated;
    }


    // ============================================================================================
    // PRIVATE HELPERS (unchanged)
    // ============================================================================================

    private int findRowContaining(Sheet sheet, String searchText, int startRow, int endRow) {
        for (int i = startRow; i <= endRow && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (Cell cell : row) {
                    if (cell != null && cell.getCellType() == CellType.STRING && Objects.equals(searchText, cell.getStringCellValue().trim())) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int findColumnIndex(Sheet sheet, String columnName, int headerRowIndex) {
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING && Objects.equals(columnName, cell.getStringCellValue().trim())) {
                    return cell.getColumnIndex();
                }
            }
        }
        return -1;
    }

    private int findRowIndexByCellValue(Sheet sheet, int columnIndex, String value, int startRow) {
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null && cell.getCellType() == CellType.STRING && Objects.equals(value, cell.getStringCellValue().trim())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findColumnIndexByCellValue(Sheet sheet, int rowIndex, int value) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellType() == CellType.NUMERIC && value == (int) cell.getNumericCellValue()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findRowContainingNumber(Sheet sheet, int number, int startRow, int endRow) {
        for (int i = startRow; i <= endRow && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (Cell cell : row) {
                    if (cell != null && cell.getCellType() == CellType.NUMERIC && number == (int) cell.getNumericCellValue()) {
                        return i; // 返回包含该数字的行索引
                    }
                }
            }
        }
        return -1; // 未找到
    }

    /**
     * [NEW] Extracts all employee names from the attendance sheet.
     * @param workbook The attendance workbook.
     * @return A list of employee names.
     * @throws IOException If the required headers are not found.
     */
    public List<String> extractEmployeeNames(Workbook workbook) throws IOException {
        List<String> names = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);

        int employeeHeaderRowIndex = findRowContaining(sheet, "姓名", 0, 5);
        if (employeeHeaderRowIndex == -1) {
            throw new IOException("考勤表中找不到员工信息表头行(含'姓名')");
        }

        int nameColumnIndex = findColumnIndex(sheet, "姓名", employeeHeaderRowIndex);
        if (nameColumnIndex == -1) {
            throw new IOException("考勤表中找不到'姓名'列");
        }

        int startRow = employeeHeaderRowIndex + 1; // Start from the row right below the header
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(nameColumnIndex);
                // Check if the cell contains a non-blank string value
                if (cell != null && cell.getCellType() == CellType.STRING && !StrUtil.isBlank(cell.getStringCellValue())) {
                    String name = cell.getStringCellValue().trim();
                    // Heuristic to stop if we encounter something that's clearly not a name row, e.g., "备注"
                    if (name.contains("备注") || name.contains("说明")) {
                        break;
                    }
                    names.add(name);
                }
            }
        }
        return names;
    }
}
