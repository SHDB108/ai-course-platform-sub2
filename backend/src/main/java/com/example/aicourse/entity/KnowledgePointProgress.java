package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点掌握进度实体
 */
@Data
@TableName("t_knowledge_point_progress")
public class KnowledgePointProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;         // 学生ID
    private Long knowledgePointId;  // 知识点ID
    private Long courseId;          // 课程ID
    
    // 掌握程度
    private String masteryLevel;    // 掌握等级: UNKNOWN, LEARNING, PARTIAL, MASTERED, EXPERT
    private Integer masteryScore;   // 掌握分数 (0-100)
    private Integer confidence;     // 置信度 (0-100)
    
    // 学习统计
    private Integer studyCount;     // 学习次数
    private Integer testCount;      // 测试次数
    private Integer correctCount;   // 正确次数
    private Integer wrongCount;     // 错误次数
    private Double accuracy;        // 正确率
    
    // 时间记录
    private Integer totalStudyTime; // 总学习时长(分钟)
    private LocalDateTime firstStudyTime;   // 首次学习时间
    private LocalDateTime lastStudyTime;    // 最后学习时间
    private LocalDateTime masteredTime;     // 掌握时间
    
    // 学习路径
    private String learningPath;    // 学习路径(JSON数组)
    private String weaknessAreas;   // 薄弱环节(JSON数组)
    private String relatedResources; // 相关资源(JSON数组)
    
    // 复习计划
    private LocalDateTime nextReviewTime;   // 下次复习时间
    private Integer reviewInterval;         // 复习间隔(天)
    private Integer reviewCount;            // 复习次数
    private String reviewStatus;            // 复习状态: SCHEDULED, OVERDUE, COMPLETED
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}