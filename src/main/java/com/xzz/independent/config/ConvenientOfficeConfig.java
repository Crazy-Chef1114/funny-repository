package com.xzz.independent.config;

import com.xzz.independent.processor.ExcelFileProcessor;
import com.xzz.independent.processor.FileProcessor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @Author xzz_Cao
 * @Date 2025/9/24 22:48
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class ConvenientOfficeConfig {

    private FileConfig file;

    @Data
    public static class FileConfig {
        private UploadConfig upload;
        private ProcessingConfig processing;

        @Data
        public static class UploadConfig {
            private String maxSize;
            private String[] allowedExtensions;
        }

        @Data
        public static class ProcessingConfig {
            private int maxRows;
            private int maxColumns;
        }
    }

}
