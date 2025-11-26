package com.example.aicourse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties("storage")
@Data
public class StorageProperties {
    private String type = "local";
    private Path   localPath = Paths.get("upload");
    private Minio  minio = new Minio();
    @Data public static class Minio {
        private String endpoint;
        private String bucket;
        private String accessKey;
        private String secretKey;
    }
}