package com.example.aicourse.vo.task;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 学生任务视图对象 - 包含任务信息和学生提交状态
 * 用于学生查看自己的任务列表，区别于管理员/教师视角的TaskVO
 */
@Data
public class StudentTaskVO {
    
    // ========== 任务基本信息 ==========
    private Long taskId;
    private String taskTitle;
    private String taskDescription;
    private String taskType; // 'ASSIGNMENT', 'QUIZ', 'PROJECT', 'EXAM'
    private LocalDateTime deadline;
    private BigDecimal maxScore;
    private String submitType; // 提交类型：'ONLINE', 'FILE', 'LINK'
    
    // ========== 课程信息 ==========
    private Long courseId;
    private String courseName;
    private String teacherName;
    
    // ========== 学生提交状态 ==========
    private Long submissionId; // 如果已提交，则有提交ID
    private String submissionStatus; // 'NOT_SUBMITTED', 'SUBMITTED', 'GRADED', 'LATE'
    private LocalDateTime submittedAt; // 提交时间
    private BigDecimal score; // 得分（如果已批改）
    private String feedback; // 教师反馈（如果已批改）
    
    // ========== 状态计算字段 ==========
    private Boolean isOverdue; // 是否逾期
    private Boolean isSubmitted; // 是否已提交
    private Boolean isGraded; // 是否已批改
    private Integer daysUntilDeadline; // 距离截止日期的天数
    
    // ========== 时间信息 ==========
    private LocalDateTime taskCreatedAt; // 任务创建时间
    private LocalDateTime taskUpdatedAt; // 任务更新时间
}