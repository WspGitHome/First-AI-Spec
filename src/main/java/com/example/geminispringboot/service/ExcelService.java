package com.example.geminispringboot.service;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    private static final int CONSECUTIVE_EMPTY_ROW_LIMIT = 100;

    /**
     * 读取Excel文件的第一个sheet页，并把里面的数据按行提取到List<List<String>>集合里
     * @param file Excel文件
     * @return List<List<String>> 集合，包含Excel数据
     * @throws IOException 如果文件读取失败
     */
    public List<List<String>> readExcelData(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("文件为空");
        }

        List<List<String>> result = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            Sheet sheet = reader.getSheet();

            int lastRowNum = sheet.getLastRowNum();
            int consecutiveEmptyRowCount = 0;

            for (int i = 0; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) {
                    consecutiveEmptyRowCount++;
                    if (consecutiveEmptyRowCount >= CONSECUTIVE_EMPTY_ROW_LIMIT) {
                        // 连续100个空行，停止读取
                        break;
                    }
                    result.add(new ArrayList<>()); // 添加空行以保持行号对应
                    continue;
                }

                // 如果当前行不为空，重置计数器
                consecutiveEmptyRowCount = 0;

                List<String> rowData = new ArrayList<>();
                short lastCellNum = row.getLastCellNum();
                for (int j = 0; j < lastCellNum; j++) {
                    Object cellValue = reader.readCellValue(j, i);
                    rowData.add(cellValue != null ? cellValue.toString() : "");
                }
                result.add(rowData);
            }
        }
        return result;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
