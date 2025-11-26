package com.example.aicourse.dto.study;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习会话创建DTO
 */
@Data
public class StudySessionCreateDTO {
    private Long courseId;
    private String sessionType;        // VIDEO, TASK, EXAM, READING, QUIZ
    private Long resourceId;
    private String resourceTitle;
    
    // 可选的设备信息
    private String deviceType;         // PC, MOBILE, TABLET
    private String browserInfo;
    private String ipAddress;
    
    // 会话开始时的状态
    private String notes;              // 学习前的笔记
}