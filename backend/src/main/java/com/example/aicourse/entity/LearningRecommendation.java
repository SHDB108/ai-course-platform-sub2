package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_learning_recommendation")
public class LearningRecommendation {
    @TableId
    private Long id;
    private Long studentId;
    private Long courseId;
    private String recommendationType; // e.g., 'KNOWLEDGE_POINT', 'REVIEW_MATERIAL'
    private Long targetId;
    private String reason;
    private Integer isDismissed; // 0=活跃, 1=用户已忽略, 2=系统判定为过时

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}