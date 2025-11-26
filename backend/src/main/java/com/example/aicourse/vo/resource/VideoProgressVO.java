package com.example.aicourse.vo.resource;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * API 7.8 视频学习进度响应
 */
@Data
public class VideoProgressVO {
    private Long id;
    private Long resourceId;
    private Long studentId;
    private String progress; // JSON string
    private Integer completion;
    private LocalDateTime gmtModified;
}