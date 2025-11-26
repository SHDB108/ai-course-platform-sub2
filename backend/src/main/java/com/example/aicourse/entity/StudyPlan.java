package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习计划实体
 */
@Data
@TableName("t_study_plan")
public class StudyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;         // 学生ID
    private Long courseId;          // 课程ID(可选，为null表示跨课程计划)
    private String planName;        // 计划名称
    private String description;     // 计划描述
    private String planType;        // 计划类型: DAILY, WEEKLY, MONTHLY, CUSTOM
    
    // 计划时间
    private LocalDateTime startDate;    // 开始日期
    private LocalDateTime endDate;      // 结束日期
    private LocalDateTime targetDate;   // 目标完成日期
    private String scheduleData;        // 学习安排(JSON格式)
    
    // 计划内容
    private String goals;               // 学习目标(JSON数组)
    private String milestones;          // 里程碑(JSON数组)
    private Integer estimatedHours;     // 预计学习时长(小时)
    private String priority;            // 优先级: HIGH, MEDIUM, LOW
    
    // 计划状态
    private String status;              // 状态: DRAFT, ACTIVE, COMPLETED, PAUSED, CANCELLED
    private Integer progress;           // 完成进度 (0-100)
    private Integer completedTasks;     // 已完成任务数
    private Integer totalTasks;         // 总任务数
    
    // 提醒设置
    private Boolean reminderEnabled;    // 是否启用提醒
    private String reminderFrequency;   // 提醒频率: DAILY, WEEKLY, CUSTOM
    private String reminderTime;        // 提醒时间
    
    // 智能推荐
    private Boolean isAiGenerated;      // 是否AI生成
    private String aiRecommendReason;   // AI推荐理由
    private LocalDateTime lastAiUpdate; // 最后AI更新时间
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}