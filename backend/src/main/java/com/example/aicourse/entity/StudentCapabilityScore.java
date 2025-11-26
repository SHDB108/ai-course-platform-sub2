package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生能力分数缓存表
 */
@Data
@TableName("t_student_capability_score")
public class StudentCapabilityScore {

    @TableId
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 能力维度ID */
    private Long dimensionId;

    /** 当前分数 (平均分) */
    private Double score;

    /** 任务数量 (参与计算的任务数) */
    private Integer taskCount;

    /** 最后计算时间 */
    private LocalDateTime lastCalculatedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}
