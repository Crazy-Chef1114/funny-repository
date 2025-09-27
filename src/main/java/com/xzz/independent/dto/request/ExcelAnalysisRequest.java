package com.xzz.independent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/25 19:24
 */
@Data
public class ExcelAnalysisRequest {

    @NotNull(message = "Excel文件不能为空")
    private FilePart file;  // 改为FilePart

    @NotBlank(message = "分析类型不能为空")
    private String analysisType;

    private String additionalInstructions;

    /**
     * 验证是否为有效的Excel文件
     */
    public boolean isValidExcelFile() {
        return file != null &&
                file.filename() != null &&  // 需修改项：file.filename()替代file.getOriginalFilename()
                (file.filename().toLowerCase().endsWith(".xlsx") ||
                        file.filename().toLowerCase().endsWith(".xls"));
    }

}
