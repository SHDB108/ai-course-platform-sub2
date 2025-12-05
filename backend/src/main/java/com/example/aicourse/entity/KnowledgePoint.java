package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_knowledge_point")
public class KnowledgePoint {
    @TableId
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Long courseId;
    private Integer level;

    /**
     * 能力标签 - 用于学生能力模型雷达图
     * 可选值: THEORY, PRACTICE, LOGIC, INNOVATION
     *
     * NOTE: Requires SQL DDL execution to add column:
     * ALTER TABLE t_knowledge_point ADD COLUMN ability_tag VARCHAR(20) DEFAULT 'THEORY' COMMENT '能力标签';
     */
    private String abilityTag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}