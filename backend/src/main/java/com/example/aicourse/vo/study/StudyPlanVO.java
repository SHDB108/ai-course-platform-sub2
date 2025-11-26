package com.example.aicourse.vo.study;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习计划响应VO
 */
@Data
public class StudyPlanVO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    private String planName;
    private String description;
    private String planType;
    
    // 时间信息
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime targetDate;
    private Integer estimatedHours;
    
    // 计划状态
    private String status;
    private Integer progress;
    private String priority;
    private Integer completedTasks;
    private Integer totalTasks;
    
    // 学习目标和里程碑
    private List<String> goals;
    private List<Milestone> milestones;
    
    // 提醒设置
    private Boolean reminderEnabled;
    private String reminderFrequency;
    private String reminderTime;
    
    // AI推荐信息
    private Boolean isAiGenerated;
    private String aiRecommendReason;
    
    // 统计信息
    private Integer daysRemaining;      // 剩余天数
    private Double progressRate;        // 进度速率
    private String progressStatus;      // 进度状态: ON_TRACK, BEHIND, AHEAD
    
    @Data
    public static class Milestone {
        private String title;
        private String description;
        private LocalDateTime targetDate;
        private Boolean completed;
        private LocalDateTime completedDate;
    }
}