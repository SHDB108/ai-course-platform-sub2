package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 能力维度字典表
 */
@Data
@TableName("t_capability_dimension")
public class CapabilityDimension {

    @TableId
    private Long id;

    /** 维度编码 (如: LOGIC_THINKING, COLLABORATION, INNOVATION) */
    private String dimensionCode;

    /** 维度名称 (如: 逻辑思维, 协作能力, 创新能力) */
    private String dimensionName;

    /** 维度描述 */
    private String description;

    /** 权重 (可选，用于后续加权计算) */
    private Double weight;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}
