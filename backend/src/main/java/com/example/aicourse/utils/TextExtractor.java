package com.example.aicourse.utils;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public class TextExtractor {

    /**
     * 从上传的文件中提取纯文本内容
     * @param file 用户上传的 MultipartFile (PDF, DOCX, TXT等)
     * @return 提取出的文本内容
     */
    public static String extract(MultipartFile file) {
        Tika tika = new Tika();
        try (InputStream stream = file.getInputStream()) {
            return tika.parseToString(stream);
        } catch (Exception e) {
            // 在真实应用中，这里应该使用日志库记录错误
            System.err.println("从文件提取文本失败: " + e.getMessage());
            // 返回空字符串或抛出自定义异常
            return "";
        }
    }
    /**
     * 从文件路径提取纯文本内容
     * @param path 文件的路径
     * @return 提取出的文本内容
     */
    public static String extract(java.nio.file.Path path) {
        Tika tika = new Tika();
        try {
            return tika.parseToString(path);
        } catch (Exception e) {
            System.err.println("从文件提取文本失败: " + e.getMessage());
            return "";
        }
    }
}