package com.example.aicourse.dto.study;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习计划创建DTO
 */
@Data
public class StudyPlanCreateDTO {
    private Long courseId;              // 课程ID(可选)
    private String planName;            // 计划名称
    private String description;         // 计划描述
    private String planType;            // 计划类型: DAILY, WEEKLY, MONTHLY, CUSTOM
    
    // 时间安排
    private LocalDateTime startDate;    // 开始日期
    private LocalDateTime endDate;      // 结束日期
    private LocalDateTime targetDate;   // 目标完成日期
    private Integer estimatedHours;     // 预计学习时长
    
    // 计划内容
    private List<String> goals;         // 学习目标
    private List<MilestoneDTO> milestones; // 里程碑
    private String priority;            // 优先级: HIGH, MEDIUM, LOW
    
    // 提醒设置
    private Boolean reminderEnabled;    // 是否启用提醒
    private String reminderFrequency;   // 提醒频率
    private String reminderTime;        // 提醒时间
    
    @Data
    public static class MilestoneDTO {
        private String title;
        private String description;
        private LocalDateTime targetDate;
    }
}