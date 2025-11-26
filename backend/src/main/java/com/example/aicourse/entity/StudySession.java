package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习会话实体 - 记录每次学习的详细信息
 */
@Data
@TableName("t_study_session")
public class StudySession {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;         // 学生ID
    private Long courseId;          // 课程ID
    private String sessionType;     // 学习类型: VIDEO, TASK, EXAM, READING, QUIZ
    private Long resourceId;        // 资源ID (可以是视频ID、任务ID、考试ID等)
    private String resourceTitle;   // 资源标题
    
    // 会话时间信息
    private LocalDateTime startTime;    // 开始时间
    private LocalDateTime endTime;      // 结束时间
    private Integer duration;           // 学习时长(分钟)
    private Integer effectiveTime;      // 有效学习时长(分钟，去除暂停时间)
    
    // 学习行为数据
    private String progressData;        // 进度数据(JSON格式)
    private Integer completionRate;     // 完成率 (0-100)
    private String interactionData;     // 交互数据(JSON格式，如点击、暂停、快进等)
    
    // 学习结果
    private String result;              // 学习结果: COMPLETED, PARTIAL, SKIPPED, FAILED
    private Integer score;              // 得分(如果适用)
    private String notes;               // 学习笔记
    private String feedback;            // 反馈信息
    
    // 设备和环境信息
    private String deviceType;          // 设备类型: PC, MOBILE, TABLET
    private String browserInfo;         // 浏览器信息
    private String ipAddress;           // IP地址
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}