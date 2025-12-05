package com.example.aicourse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC Configuration for serving local video files.
 *
 * This configuration enables the application to serve video files from a local directory.
 * Videos can be accessed via the URL pattern: http://localhost:8080/files/{filename}
 *
 * Configuration:
 * Set the video storage path in application.yml:
 *   video:
 *     storage-path: D:/ai_course_videos/  # Windows
 *     # OR
 *     storage-path: /Users/yourname/videos/  # Mac/Linux
 *
 * Example usage in database:
 * INSERT INTO t_resource (filename, download_url)
 * VALUES ('ResNet.mp4', 'http://localhost:8080/files/ResNet.mp4');
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${video.storage-path:#{null}}")
    private String videoStoragePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve local video files
        if (videoStoragePath != null && !videoStoragePath.isEmpty()) {
            // Ensure path ends with separator
            String normalizedPath = videoStoragePath;
            if (!normalizedPath.endsWith(File.separator)) {
                normalizedPath += File.separator;
            }

            // Convert to file URL format
            String fileUrl = "file:" + normalizedPath;

            registry.addResourceHandler("/files/**")
                    .addResourceLocations(fileUrl)
                    .setCachePeriod(3600); // Cache for 1 hour

            System.out.println("Video files will be served from: " + fileUrl);
            System.out.println("Access via: http://localhost:8080/files/{filename}");
        } else {
            System.out.println("Warning: video.storage-path not configured in application.yml");
            System.out.println("Local video file serving is disabled.");
        }

        // Call super to preserve default resource handling
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
