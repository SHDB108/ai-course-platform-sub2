package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;
    private String filename;
    private String type; // VIDEO, DOCUMENT, etc.
    private String downloadUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}
