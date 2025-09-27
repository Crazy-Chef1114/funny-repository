package com.xzz.independent.processor;

import com.xzz.independent.enumeration.ErrorCode;
import com.xzz.independent.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel文件处理器实现
 * 重大修改：全面适配WebFlux响应式编程模型
 * @Author xzz_Cao
 * @Date 2025/9/25 10:21
 */
@Slf4j
@Component
public class ExcelFileProcessor implements FileProcessor{

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final int MAX_ROWS = 100000;
    private static final int MAX_COLUMNS = 100;

    @Override
    public boolean supports(String filename) {
        return filename != null &&
                (filename.toLowerCase().endsWith(".xlsx") ||
                        filename.toLowerCase().endsWith(".xls"));
    }

    /**
     * 优化说明：改进文件内容提取方法，使用更高效的流式处理
     * 修改点：重构getFileContentFromFilePart方法，提高大文件处理能力
     */
    private byte[] getFileContentFromFilePart(FilePart filePart) {
        try {
            // 优化：使用更高效的字节数组收集方式
            return filePart.content()
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        dataBuffer.readPosition(0); // 重置读取位置
                        return bytes;
                    })
                    .collectList()
                    .map(byteArrays -> {
                        // 计算总长度
                        int totalLength = byteArrays.stream().mapToInt(arr -> arr.length).sum();
                        byte[] result = new byte[totalLength];
                        int currentPosition = 0;

                        // 合并所有字节数组
                        for (byte[] array : byteArrays) {
                            System.arraycopy(array, 0, result, currentPosition, array.length);
                            currentPosition += array.length;
                        }
                        return result;
                    })
                    .block(); // 在fromCallable中允许阻塞操作
        } catch (Exception e) {
            throw new FileProcessingException(
                    ErrorCode.FILE_PARSE_ERROR,
                    "读取文件内容失败: " + e.getMessage()
            );
        }
    }

    /**
     * 解析文件内容
     */
    @Override
    public Mono<List<Map<String, Object>>> parseFile(FilePart filePart) {
        // 需修改项：使用Mono.fromCallable包装阻塞操作
        return Mono.fromCallable(() -> {
            validateFile(filePart);

            try {
                // 需修改项：将FilePart内容转换为字节数组进行处理
                byte[] fileContent = getFileContentFromFilePart(filePart);
                return parseExcelContent(fileContent, filePart.filename());
            } catch (IOException e) {
                throw new FileProcessingException(
                        ErrorCode.FILE_PARSE_ERROR,
                        "Excel文件解析失败: " + e.getMessage()
                );
            }
        });
    }

    @Override
    public String convertToAnalysisFormat(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "无数据";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t", data.get(0).keySet())).append("\n");

        for (Map<String, Object> row : data) {
            sb.append(String.join("\t",
                    row.values().stream()
                            .map(v -> v != null ? v.toString() : "")
                            .toArray(String[]::new)
            )).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String getFileType() {
        return "EXCEL";
    }

    /**
     * 需修改项：方法参数增加filename
     */
    private List<Map<String, Object>> parseExcelContent(byte[] fileContent, String filename) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);
             Workbook workbook = createWorkbook(inputStream, filename)) {

            Sheet sheet = workbook.getSheetAt(0);
            validateSheet(sheet);
            return parseSheetData(sheet);
        }
    }


    /**
     * 验证文件
     */
    private void validateFile(FilePart filePart) {
        if (filePart == null) {
            throw new FileProcessingException(ErrorCode.FILE_EMPTY, "请选择要上传的文件");
        }

        // 注意：FilePart没有直接的文件大小信息，需要在控制器层面验证
        // 文件大小验证移到控制器中进行

        if (!supports(filePart.filename())) {
            throw new FileProcessingException(ErrorCode.UNSUPPORTED_FILE_TYPE, "请上传Excel文件");
        }
    }

    private void validateSheet(Sheet sheet) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            throw new FileProcessingException(ErrorCode.FILE_PARSE_ERROR, "Excel文件为空");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new FileProcessingException(ErrorCode.FILE_PARSE_ERROR, "Excel文件缺少表头");
        }

        int columnCount = headerRow.getPhysicalNumberOfCells();
        if (columnCount > MAX_COLUMNS) {
            throw new FileProcessingException(ErrorCode.FILE_PARSE_ERROR,
                    "文件列数不能超过" + MAX_COLUMNS + "列");
        }
    }

    private List<Map<String, Object>> parseSheetData(Sheet sheet) {
        List<Map<String, Object>> result = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        List<String> headers = extractHeaders(headerRow);

        for (int i = 1; i <= sheet.getLastRowNum() && result.size() < MAX_ROWS; i++) {
            Row dataRow = sheet.getRow(i);
            if (dataRow == null) continue;

            try {
                Map<String, Object> rowData = parseRowData(headers, dataRow);
                result.add(rowData);
            } catch (Exception e) {
                log.warn("解析第{}行数据失败: {}", i + 1, e.getMessage());
            }
        }
        log.info("成功解析{}行数据", result.size());
        return result;
    }

    private List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValueAsString(cell));
        }
        return headers;
    }

    private Map<String, Object> parseRowData(List<String> headers, Row dataRow) {
        Map<String, Object> rowData = new LinkedHashMap<>();
        for (int j = 0; j < headers.size(); j++) {
            Cell cell = dataRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            rowData.put(headers.get(j), getCellValue(cell));
        }
        return rowData;
    }

    private Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ?
                    cell.getDateCellValue() : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> getFormulaCellValue(cell);
            default -> "";
        };
    }

    private Object getFormulaCellValue(Cell cell) {
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            try {
                return cell.getStringCellValue();
            } catch (Exception ex) {
                return cell.getCellFormula();
            }
        }
    }

    /**
     * 需修改项：参数改为InputStream和filename
     */
    private Workbook createWorkbook(ByteArrayInputStream inputStream, String filename) throws IOException {
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else {
            return new HSSFWorkbook(inputStream);
        }
    }

    private String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }

}
