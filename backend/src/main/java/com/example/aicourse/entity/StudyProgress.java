package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习进度实体 - 跟踪学生在课程中的整体学习进度
 */
@Data
@TableName("t_study_progress")
public class StudyProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;         // 学生ID
    private Long courseId;          // 课程ID
    private Integer totalProgress;  // 总体进度 (0-100)
    
    // 各模块进度
    private Integer videoProgress;      // 视频学习进度 (0-100)
    private Integer taskProgress;       // 任务完成进度 (0-100)
    private Integer examProgress;       // 考试完成进度 (0-100)
    private Integer knowledgeProgress;  // 知识点掌握进度 (0-100)
    
    // 学习统计
    private Integer totalStudyTime;     // 总学习时长(分钟)
    private Integer todayStudyTime;     // 今日学习时长(分钟)
    private Integer weekStudyTime;      // 本周学习时长(分钟)
    private Integer monthStudyTime;     // 本月学习时长(分钟)
    
    // 完成数量统计
    private Integer completedVideos;    // 已完成视频数
    private Integer totalVideos;        // 总视频数
    private Integer completedTasks;     // 已完成任务数
    private Integer totalTasks;         // 总任务数
    private Integer completedExams;     // 已完成考试数
    private Integer totalExams;         // 总考试数
    private Integer masteredKnowledge;  // 已掌握知识点数
    private Integer totalKnowledge;     // 总知识点数
    
    // 学习状态
    private String status;              // 学习状态: ACTIVE, COMPLETED, SUSPENDED, DROPPED
    private LocalDateTime lastStudyTime; // 最后学习时间
    private LocalDateTime startDate;    // 开始学习日期
    private LocalDateTime expectedEndDate; // 预期完成日期
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}