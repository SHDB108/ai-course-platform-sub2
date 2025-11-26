package com.example.aicourse.vo.study;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习会话响应VO
 */
@Data
public class StudySessionVO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    private String sessionType;
    private Long resourceId;
    private String resourceTitle;
    
    // 时间信息
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;           // 总时长(分钟)
    private Integer effectiveTime;      // 有效时长(分钟)
    
    // 学习结果
    private Integer completionRate;     // 完成率
    private String result;              // 学习结果
    private Integer score;              // 得分
    private String notes;               // 笔记
    
    // 设备信息
    private String deviceType;
    private String browserInfo;
}