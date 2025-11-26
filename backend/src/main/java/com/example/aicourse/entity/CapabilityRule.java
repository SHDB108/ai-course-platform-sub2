package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 能力评分规则表 - 将任务类型映射到能力维度
 */
@Data
@TableName("t_capability_rule")
public class CapabilityRule {

    @TableId
    private Long id;

    /** 任务类型 (ASSIGNMENT, QUIZ, PROJECT, EXAM) */
    private String taskType;

    /** 能力维度ID */
    private Long dimensionId;

    /** 规则描述 */
    private String description;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}
